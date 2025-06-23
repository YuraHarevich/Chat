import { check, group, sleep } from 'k6';
import http from 'k6/http';
import { WebSocket } from 'k6/experimental/websockets';

export const options = {
    vus: 1,
    iterations: 1,
    thresholds: {
        http_req_failed: ['rate<0.1'],
        checks: ['rate>0.9']
    }
};

export default function () {
    const BASE_URL = 'http://host.docker.internal:8000';
    const USERNAME = 'pumba4'; // Ваши данные
    const PASSWORD = 'qwerty';

    // 1. Аутентификация (как во фронтенде)
    let token;
    group('Authentication', function () {
        const loginRes = http.post(
            `${BASE_URL}/api/v1/users/sign-in`,
            JSON.stringify({ username: USERNAME, password: PASSWORD }),
            { headers: { 'Content-Type': 'application/json' } }
        );

        check(loginRes, {
            'Login successful': (r) => r.status === 200,
            'Received token': (r) => r.json().access_token
        });

        token = loginRes.json('access_token');
    });

    // 2. Получение списка чатов (как в loadChats() фронтенда)
    let sharedId, chatId;
    group('Load chats', function () {
        const chatsRes = http.get(
            `${BASE_URL}/api/v1/chats/username/${USERNAME}`,
            { headers: { Authorization: `Bearer ${token}` } }
        );

        check(chatsRes, {
            'Chats loaded': (r) => r.status === 200
        });

        const firstChat = chatsRes.json('content.0');
        sharedId = firstChat.sharedId; // Берем первый доступный чат
        chatId = firstChat.chatId;
        console.log(`Selected chat: ${sharedId}`);
    });

    let userId;
    group('Load user', function () {
        const chatsRes = http.get(
            `${BASE_URL}/api/v1/users/username/${USERNAME}`,
            { headers: { Authorization: `Bearer ${token}` } }
        );

        check(chatsRes, {
            'Users loaded': (r) => r.status === 200
        });

        userId = chatsRes.json('id')
        console.log(`Selected user: ${sharedId}`);
    });

    // 3. WebSocket-подключение и отправка сообщений
    group('WebSocket messaging', function () {
        const ws = new WebSocket(
            `${BASE_URL.replace('http', 'ws')}/ws-chat`,
            null,
            {
                headers: { Authorization: `Bearer ${token}` }
            }
        );

        ws.onerror = (e) => console.error('WS error:', e.error);

        // Подписка на ответы сервера (как во фронтенде)
        ws.onmessage = (msg) => {
            console.log('Received:', msg.data);
        };

        // Отправка 10 сообщений
        ws.onopen = () => {
            console.log('WebSocket connected');

            // Подписка на топик (аналог stompClient.subscribe())
            ws.send('SUBSCRIBE\nid:sub-0\ndestination:/topic/chat.' + sharedId + '\n\n\x00');

            for (let i = 1; i <= 10; i++) {
                const message = {
                    content: `Test ${i}`,
                    sender: userId,
                    sharedId: sharedId
                };

                // Формируем STOMP-фрейм вручную
                const stompFrame = [
                    'SEND',
                    `destination:/app/chat.send.${sharedId}`,
                    'content-type:application/json',
                    '',
                    JSON.stringify(message),
                    '\x00' // Null-byte как окончание фрейма
                ].join('\n');

                ws.send(stompFrame);
                console.log(`Sent STOMP frame ${i}`);
                sleep(1);
            }
            ws.close();
        };

        sleep(15); // Ждем завершения
    });
}