import { check, group, sleep } from 'k6';
import http from 'k6/http';
import { WebSocket } from 'k6/experimental/websockets';
import { Trend, Rate } from 'k6/metrics';

// Кастомные метрики
const wsConnectTime = new Trend('ws_connect_time');
const messageRate = new Rate('message_rate');

export const options = {
    stages: [
        { duration: '30s', target: 20 },  // Плавный рост до 20 пользователей
        { duration: '2m', target: 20 },   // Стабильная нагрузка
        { duration: '30s', target: 0 }    // Постепенное снижение
    ],
    thresholds: {
        'http_req_failed': ['rate<0.05'],     // Менее 5% ошибок HTTP
        'ws_connect_time': ['p(95)<500'],     // 95% подключений < 500ms (в миллисекундах)
    }
};

// Глобальные константы
const BASE_URL = 'http://host.docker.internal:8000';
const USERNAME = 'pumba4';
const PASSWORD = 'qwerty';

function sendStompMessage(ws, sharedId, userId, content) {
    const frame = [
        'SEND',
        `destination:/app/chat.send.${sharedId}`,
        'content-type:application/json',
        '',
        JSON.stringify({
            content: content,
            sender: userId,
            sharedId: sharedId,
            timestamp: new Date().toISOString()
        }),
        '\x00'
    ].join('\n');
    ws.send(frame);
}

export default function () {
    // 1. Аутентификация и получение данных
    let token, userId, sharedId;

    group('Initialization', function () {
        const loginStart = Date.now();
        const loginRes = http.post(
            `${BASE_URL}/api/v1/users/sign-in`,
            JSON.stringify({ username: USERNAME, password: PASSWORD }),
            { headers: { 'Content-Type': 'application/json' } }
        );

        check(loginRes, {
            'Auth successful': (r) => r.status === 200
        });

        token = loginRes.json('access_token');

        // Получаем ID пользователя
        const userRes = http.get(
            `${BASE_URL}/api/v1/users/username/${USERNAME}`,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        userId = userRes.json('id');

        // Получаем первый чат
        const chatsRes = http.get(
            `${BASE_URL}/api/v1/chats/username/${USERNAME}`,
            { headers: { Authorization: `Bearer ${token}` } }
        );
        sharedId = chatsRes.json('content.0.sharedId');
    });

    // 2. WebSocket-сессия
    const wsStart = Date.now();
    const ws = new WebSocket(
        `${BASE_URL.replace('http', 'ws')}/ws-chat`,
        null,
        { headers: { Authorization: `Bearer ${token}` } }
    );

    ws.onerror = (e) => console.error(`[VU ${__VU}] WS error:`, e.error);

    ws.onopen = function () {
        // Фиксируем время подключения
        wsConnectTime.add(Date.now() - wsStart);

        // Подписка на топик
        ws.send(`SUBSCRIBE\nid:sub-${__VU}\ndestination:/topic/chat.${sharedId}\n\n\x00`);

        let counter = 1;
        // Отправка сообщений каждую секунду
        const interval = setInterval(() => {
            sendStompMessage(
                ws,
                sharedId,
                userId,
                `Message ${counter++} from VU ${__VU}`
            );
            messageRate.add(1);
        }, 1000);

        // Автоматическое завершение через 2 минуты
        setTimeout(() => {
            clearInterval(interval);
            ws.close();
        }, 300000);
    };

    sleep(150);
}