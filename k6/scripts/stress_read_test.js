import {check, group, sleep} from 'k6';
import http from 'k6/http';
import {Rate, Trend} from 'k6/metrics';

// Кастомные метрики
const chatLoadTime = new Trend('chat_load_time');
const messageLoadRate = new Rate('message_load_rate');

// Конфигурация из переменных окружения
const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8000';
const USERNAME = __ENV.USERNAME || 'pumba4';
const PASSWORD = __ENV.PASSWORD || 'qwerty';
const TARGET_VUS = parseInt(__ENV.TARGET_VUS) || 10;
const STAGE_DURATION = __ENV.STAGE_DURATION || '3m';
const TIMEOUT_DURATION = __ENV.TIMEOUT_DURATION || '1s';
const MESSAGES_PER_CHAT = parseInt(__ENV.MESSAGES_PER_CHAT) || 20;

export const options = {
    stages: [
        {duration: '30s', target: TARGET_VUS},
        {duration: STAGE_DURATION, target: TARGET_VUS},
        {duration: '30s', target: 0}
    ],
    thresholds: {
        'http_req_failed': ['rate<0.05'],
        'chat_load_time': ['p(95)<1000']
    }
};

export default function () {
    // 1. Аутентификация
    let token, userId, chats;

    group('Initialization', function () {
        const loginRes = http.post(
            `${BASE_URL}/api/v1/users/sign-in`,
            JSON.stringify({username: USERNAME, password: PASSWORD}),
            {headers: {'Content-Type': 'application/json'}}
        );

        check(loginRes, {
            'Auth successful': (r) => r.status === 200
        });

        token = loginRes.json('access_token');

        // Получаем ID пользователя
        const userRes = http.get(
            `${BASE_URL}/api/v1/users/username/${USERNAME}`,
            {headers: {Authorization: `Bearer ${token}`}}
        );
        userId = userRes.json('id');

        // Получаем список чатов
        const chatsRes = http.get(
            `${BASE_URL}/api/v1/chats/username/${USERNAME}`,
            {headers: {Authorization: `Bearer ${token}`}}
        );
        chats = chatsRes.json('content');
    });

    // 2. Поочередная загрузка сообщений из чатов
    group('Load messages', function () {
        const startTime = Date.now();
        const testDuration = parseDuration(STAGE_DURATION) * 1000;

        while (Date.now() - startTime < testDuration) {
            for (const chat of chats) {
                const loadStart = Date.now();

                // Загружаем сообщения чата
                const messagesRes = http.get(
                    `${BASE_URL}/api/v1/chats/${chat.chatId}/messages`,
                    {
                        headers: {Authorization: `Bearer ${token}`},
                        params: {size: MESSAGES_PER_CHAT}
                    }
                );

                check(messagesRes, {
                    [`Messages loaded for chat ${chat.chatId}`]: (r) => r.status === 200
                });

                chatLoadTime.add(Date.now() - loadStart);
                messageLoadRate.add(1);

                sleep(parseDuration(TIMEOUT_DURATION) * 1000);
            }
        }
    });
}

function parseDuration(duration) {
    const unit = duration.slice(-1);
    const value = parseInt(duration.slice(0, -1));
    switch (unit) {
        case 's':
            return value;
        case 'm':
            return value * 60;
        case 'h':
            return value * 3600;
        default:
            return 1;
    }
}