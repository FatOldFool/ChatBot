let stompClient = null;
let currentRoomId = null;

let sessionId = localStorage.getItem('sessionId');
if (!sessionId) {
    sessionId = crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2);
    localStorage.setItem('sessionId', sessionId);
}

let displayName = localStorage.getItem('displayName') || 'User_' + sessionId.substring(0,6);
document.getElementById('display-name').value = displayName;

// ========== Управление темой ==========
function initTheme() {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        document.body.classList.add('dark-mode');
        document.getElementById('theme-btn').innerHTML = '<i class="fas fa-sun"></i> Светлая тема';
    } else {
        document.body.classList.remove('dark-mode');
        document.getElementById('theme-btn').innerHTML = '<i class="fas fa-moon"></i> Тёмная тема';
    }
}

function toggleTheme() {
    if (document.body.classList.contains('dark-mode')) {
        document.body.classList.remove('dark-mode');
        localStorage.setItem('theme', 'light');
        document.getElementById('theme-btn').innerHTML = '<i class="fas fa-moon"></i> Тёмная тема';
    } else {
        document.body.classList.add('dark-mode');
        localStorage.setItem('theme', 'dark');
        document.getElementById('theme-btn').innerHTML = '<i class="fas fa-sun"></i> Светлая тема';
    }
}

// DOM элементы
const roomsListDiv = document.getElementById('rooms-list');
const messagesContainer = document.getElementById('messages-container');
const currentRoomTitleSpan = document.getElementById('current-room-title');
const messageInput = document.getElementById('message-input');
const sendBtn = document.getElementById('send-btn');

// API вызовы
async function fetchRooms() {
    try {
        const response = await fetch('/api/rooms');
        if (response.ok) {
            const rooms = await response.json();
            renderRoomsList(rooms);
        }
    } catch (error) {
        console.error(error);
    }
}

async function createRoom(roomName) {
    try {
        const response = await fetch('/api/rooms', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ roomName, creatorSessionId: sessionId })
        });
        if (response.ok) await fetchRooms();
        else alert('Ошибка создания комнаты');
    } catch (error) {
        console.error(error);
    }
}

async function fetchMessages(roomId) {
    try {
        const response = await fetch(`/api/rooms/${roomId}/messages`);
        if (response.ok) {
            const messages = await response.json();
            renderMessages(messages);
        }
    } catch (error) {
        console.error(error);
    }
}

// ========== WebSocket и typing ==========
let typingTimeout = null;

function connectToRoom(roomId) {
    if (stompClient) stompClient.disconnect();
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, () => {
        stompClient.subscribe(`/topic/room/${roomId}`, (message) => {
            const msg = JSON.parse(message.body);
            displayMessage(msg);
        });
        // Отправляем join-событие с именем пользователя
        stompClient.send(`/app/chat.join/${roomId}`, {}, JSON.stringify({
            userId: sessionId,
            userName: displayName
        }));
    }, (error) => console.error(error));
}

function sendMessage(content) {
    if (!stompClient || !currentRoomId) return;
    stompClient.send(`/app/chat.send/${currentRoomId}`, {}, JSON.stringify({
        userId: sessionId,
        userName: displayName,
        content
    }));
    messageInput.value = '';
}

function sendTyping() {
    if (!stompClient || !currentRoomId) return;
    stompClient.send(`/app/chat.typing/${currentRoomId}`, {}, JSON.stringify({
        userId: sessionId,
        userName: displayName
    }));
}

function startTyping() {
    sendTyping();
    if (typingTimeout) clearTimeout(typingTimeout);
    typingTimeout = setTimeout(() => {
        // можно отправить событие остановки, но пока не надо
    }, 2000);
}

// ========== Рендеринг ==========
function renderRoomsList(rooms) {
    roomsListDiv.innerHTML = '';
    rooms.forEach(room => {
        const div = document.createElement('div');
        div.className = 'room-item';
        if (currentRoomId === room.id) div.classList.add('active');
        div.innerHTML = `<i class="fas fa-hashtag"></i><span>${escapeHtml(room.name)}</span>`;
        div.onclick = () => {
            currentRoomId = room.id;
            currentRoomTitleSpan.textContent = room.name;
            fetchMessages(room.id);
            connectToRoom(room.id);
            messageInput.disabled = false;
            sendBtn.disabled = false;
            document.querySelectorAll('.room-item').forEach(el => el.classList.remove('active'));
            div.classList.add('active');
        };
        roomsListDiv.appendChild(div);
    });
}

function renderMessages(messages) {
    messagesContainer.innerHTML = '';
    if (!messages || messages.length === 0) {
        messagesContainer.innerHTML = '<div class="welcome-message">Нет сообщений. Напишите что-нибудь!</div>';
        return;
    }
    messages.forEach(msg => displayMessage(msg));
}

function displayMessage(msg) {
    if (messagesContainer.querySelector('.welcome-message')) messagesContainer.innerHTML = '';

    if (msg.type === 'system') {
        const sysDiv = document.createElement('div');
        sysDiv.className = 'system-message';
        sysDiv.innerHTML = `<div class="system-content"><i class="fas fa-info-circle"></i> ${escapeHtml(msg.content)}</div>`;
        messagesContainer.appendChild(sysDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
        return;
    }

    if (msg.type === 'typing') {
        // Удаляем старый индикатор, если есть
        const oldIndicator = document.querySelector('.typing-indicator');
        if (oldIndicator) oldIndicator.remove();
        const typingDiv = document.createElement('div');
        typingDiv.className = 'typing-indicator';
        typingDiv.textContent = `${msg.userName} печатает...`;
        messagesContainer.appendChild(typingDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
        setTimeout(() => {
            if (typingDiv.parentNode) typingDiv.remove();
        }, 2000);
        return;
    }

    const messageDiv = document.createElement('div');
    const isOwn = (msg.userId === sessionId);
    messageDiv.className = `message ${isOwn ? 'own' : 'other'}`;

    const avatarDiv = document.createElement('div');
    avatarDiv.className = 'avatar';
    const userName = msg.userName || 'U';
    avatarDiv.textContent = userName.charAt(0).toUpperCase();

    const contentDiv = document.createElement('div');
    contentDiv.className = 'message-content';
    contentDiv.innerHTML = `
        <div class="message-sender">${escapeHtml(userName)}</div>
        <div class="message-text">${escapeHtml(msg.content)}</div>
        <div class="message-time">${new Date(msg.createdAt).toLocaleTimeString()}</div>
    `;
    messageDiv.appendChild(avatarDiv);
    messageDiv.appendChild(contentDiv);
    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;' }[m]));
}

// ========== Обработчики UI ==========
document.getElementById('create-room-btn').onclick = () => {
    const name = document.getElementById('new-room-name').value.trim();
    if (name) createRoom(name);
    document.getElementById('new-room-name').value = '';
};

sendBtn.onclick = () => {
    const content = messageInput.value.trim();
    if (content && stompClient && currentRoomId) sendMessage(content);
};

messageInput.onkeypress = (e) => {
    if (e.key === 'Enter') sendBtn.click();
    else startTyping();
};

document.getElementById('display-name').onchange = (e) => {
    displayName = e.target.value.trim() || 'User_' + sessionId.substring(0,6);
    localStorage.setItem('displayName', displayName);
    if (currentRoomId) connectToRoom(currentRoomId);
};

document.getElementById('theme-btn').onclick = toggleTheme;

// Инициализация
initTheme();
fetchRooms();