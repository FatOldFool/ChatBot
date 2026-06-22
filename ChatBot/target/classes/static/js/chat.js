let stompClient = null;
let currentRoomId = null;
let messagesOffset = 0;
const MESSAGES_LIMIT = 20;

let authToken = localStorage.getItem('authToken');
let currentUsername = localStorage.getItem('username') || '';

/* ========== DOM-элементы ========== */
const loginOverlay = document.getElementById('login-overlay');
const loginInput = document.getElementById('login-username');
const loginBtn = document.getElementById('login-btn');
const loginError = document.getElementById('login-error');
const chatApp = document.getElementById('chat-app');

const createRoomOverlay = document.getElementById('create-room-overlay');
const newRoomNameInput = document.getElementById('new-room-name-input');
const createRoomSubmitBtn = document.getElementById('create-room-submit-btn');
const createRoomError = document.getElementById('create-room-error');

const roomsListDiv = document.getElementById('rooms-list');
const messagesContainer = document.getElementById('messages-container');
const currentRoomTitleSpan = document.getElementById('current-room-title');
const messageInput = document.getElementById('message-input');
const sendBtn = document.getElementById('send-btn');
const loadMoreBtn = document.getElementById('load-more-btn');
const openCreateRoomBtn = document.getElementById('open-create-room-btn');

/* ========== Тема ========== */
function initTheme() {
    const saved = localStorage.getItem('theme');
    const btn = document.getElementById('theme-btn');
    if (saved === 'dark') {
        document.body.classList.add('dark-mode');
        btn.innerHTML = '<i class="fas fa-sun"></i> Светлая тема';
    } else {
        document.body.classList.remove('dark-mode');
        btn.innerHTML = '<i class="fas fa-moon"></i> Тёмная тема';
    }
}

function toggleTheme() {
    const btn = document.getElementById('theme-btn');
    if (document.body.classList.contains('dark-mode')) {
        document.body.classList.remove('dark-mode');
        localStorage.setItem('theme', 'light');
        btn.innerHTML = '<i class="fas fa-moon"></i> Тёмная тема';
    } else {
        document.body.classList.add('dark-mode');
        localStorage.setItem('theme', 'dark');
        btn.innerHTML = '<i class="fas fa-sun"></i> Светлая тема';
    }
}

/* ========== Аутентификация ========== */
async function login(username) {
    try {
        const resp = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username })
        });
        if (resp.ok) {
            const data = await resp.json();
            authToken = data.token;
            currentUsername = data.username;
            localStorage.setItem('authToken', authToken);
            localStorage.setItem('username', currentUsername);
            document.getElementById('display-name').value = currentUsername;
            return true;
        } else {
            const err = await resp.json();
            loginError.textContent = err.error || 'Ошибка входа';
            return false;
        }
    } catch (e) {
        loginError.textContent = 'Ошибка сети. Попробуйте позже.';
        return false;
    }
}

function showLogin(message = '') {
    loginOverlay.style.display = 'flex';
    chatApp.style.display = 'none';
    loginInput.value = currentUsername || '';
    loginError.textContent = message || '';
    loginInput.focus();
}

function hideLogin() {
    loginOverlay.style.display = 'none';
    chatApp.style.display = 'flex';
}

function handleAuthFailure() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('username');
    authToken = null;
    currentUsername = '';
    document.getElementById('display-name').value = '';
    if (stompClient) {
        stompClient.disconnect();
        stompClient = null;
    }
    currentRoomId = null;
    currentRoomTitleSpan.textContent = 'Выберите комнату';
    messageInput.disabled = true;
    sendBtn.disabled = true;
    messagesContainer.innerHTML = '<div class="welcome-message">Войдите, чтобы продолжить</div>';
    roomsListDiv.innerHTML = '';
    showLogin('Сессия устарела. Войдите заново.');
}

async function handleResponse(response) {
    if (response.status === 403 || response.status === 401) {
        handleAuthFailure();
        return null;
    }
    return response;
}

/* ========== Модальное окно создания комнаты ========== */
function showCreateRoomModal() {
    createRoomOverlay.style.display = 'flex';
    newRoomNameInput.value = '';
    createRoomError.textContent = '';
    newRoomNameInput.focus();
}

function hideCreateRoomModal() {
    createRoomOverlay.style.display = 'none';
}

async function submitCreateRoom() {
    const name = newRoomNameInput.value.trim();
    if (!name) {
        createRoomError.textContent = 'Название не может быть пустым';
        return;
    }
    if (name.length > 50) {
        createRoomError.textContent = 'Название не может быть длиннее 50 символов';
        return;
    }

    createRoomSubmitBtn.disabled = true;
    try {
        const resp = await fetch('/api/rooms', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({ roomName: name })
        });
        if (!resp.ok) {
            const safeResp = await handleResponse(resp);
            if (!safeResp) return;
            const err = await safeResp.json();
            createRoomError.textContent = err.error || 'Ошибка создания комнаты';
        } else {
            hideCreateRoomModal();
            await fetchRooms();
        }
    } catch (e) {
        createRoomError.textContent = 'Ошибка сети';
    } finally {
        createRoomSubmitBtn.disabled = false;
    }
}

/* ========== REST API ========== */
async function fetchRooms() {
    try {
        const resp = await fetch('/api/rooms', {
            headers: { 'Authorization': `Bearer ${authToken}` }
        });
        if (!resp.ok) {
            const safeResp = await handleResponse(resp);
            if (!safeResp) return;
        }
        renderRoomsList(await resp.json());
    } catch (e) { console.error(e); }
}

async function fetchMessages(roomId, append = false) {
    try {
        const limit = MESSAGES_LIMIT;
        const offset = append ? messagesOffset : 0;
        const resp = await fetch(`/api/rooms/${roomId}/messages?limit=${limit}&offset=${offset}`, {
            headers: { 'Authorization': `Bearer ${authToken}` }
        });
        if (!resp.ok) {
            const safeResp = await handleResponse(resp);
            if (!safeResp) return;
        }
        const data = await resp.json();
        if (append) {
            const prevScrollHeight = messagesContainer.scrollHeight;
            data.reverse().forEach(msg => displayMessage(msg, true));
            messagesContainer.scrollTop = messagesContainer.scrollHeight - prevScrollHeight;
        } else {
            messagesOffset = 0;
            renderMessages(data);
        }
        messagesOffset = offset + data.length;
        if (loadMoreBtn) {
            loadMoreBtn.style.display = data.length < limit ? 'none' : 'block';
        }
    } catch (e) { console.error(e); }
}

/* ========== WebSocket ========== */
let typingTimer = null;

function connectToRoom(roomId) {
    if (stompClient) stompClient.disconnect();
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({ Authorization: `Bearer ${authToken}` }, () => {
        stompClient.subscribe(`/topic/room/${roomId}`, msg => displayMessage(JSON.parse(msg.body)));
        stompClient.send(`/app/chat.join/${roomId}`, {}, JSON.stringify({}));
    }, err => {
        console.error('WebSocket connection error:', err);
        alert('Не удалось подключиться к чату. Обновите страницу.');
    });
}

function sendMessage(text) {
    if (!stompClient || !currentRoomId) return;
    stompClient.send(`/app/chat.send/${currentRoomId}`, {}, JSON.stringify({ content: text }));
    messageInput.value = '';
}

function sendTyping() {
    if (!stompClient || !currentRoomId) return;
    stompClient.send(`/app/chat.typing/${currentRoomId}`, {}, JSON.stringify({}));
}

function startTyping() {
    sendTyping();
    clearTimeout(typingTimer);
    typingTimer = setTimeout(() => {}, 2000);
}

/* ========== Рендеринг ========== */
function renderRoomsList(rooms) {
    roomsListDiv.innerHTML = '';
    rooms.forEach(r => {
        const div = document.createElement('div');
        div.className = 'room-item' + (currentRoomId === r.id ? ' active' : '');
        div.innerHTML = `<i class="fas fa-hashtag"></i><span>${escapeHtml(r.name)}</span>`;
        div.onclick = () => {
            currentRoomId = r.id;
            currentRoomTitleSpan.textContent = r.name;
            messagesOffset = 0;
            fetchMessages(r.id, false);
            connectToRoom(r.id);
            messageInput.disabled = false;
            sendBtn.disabled = false;
            document.querySelectorAll('.room-item').forEach(el => el.classList.remove('active'));
            div.classList.add('active');
        };
        roomsListDiv.appendChild(div);
    });
}

function renderMessages(msgs) {
    messagesContainer.innerHTML = '';
    if (!msgs || msgs.length === 0) {
        messagesContainer.innerHTML = '<div class="welcome-message">Нет сообщений. Напишите что-нибудь!</div>';
        return;
    }
    msgs.forEach(msg => displayMessage(msg, false));
}

function displayMessage(msg, prepend = false) {
    const welcome = messagesContainer.querySelector('.welcome-message');
    if (welcome) welcome.remove();

    if (msg.type === 'system') {
        const d = document.createElement('div');
        d.className = 'system-message';
        d.innerHTML = `<div class="system-content"><i class="fas fa-info-circle"></i> ${escapeHtml(msg.content)}</div>`;
        if (prepend) messagesContainer.prepend(d);
        else messagesContainer.appendChild(d);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
        return;
    }

    if (msg.type === 'typing') {
        const old = document.querySelector('.typing-indicator');
        if (old) old.remove();
        const typingDiv = document.createElement('div');
        typingDiv.className = 'typing-indicator';
        typingDiv.textContent = `${escapeHtml(msg.userName)} печатает...`;
        messagesContainer.appendChild(typingDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
        setTimeout(() => typingDiv.remove(), 2000);
        return;
    }

    const isOwn = (msg.userId === currentUsername || msg.userName === currentUsername);
    const div = document.createElement('div');
    div.className = `message ${isOwn ? 'own' : 'other'}`;

    const avatar = document.createElement('div');
    avatar.className = 'avatar';
    avatar.textContent = (msg.userName || msg.userId || 'U').charAt(0).toUpperCase();

    const content = document.createElement('div');
    content.className = 'message-content';
    content.innerHTML = `
        <div class="message-sender">${escapeHtml(msg.userName || msg.userId || 'U')}</div>
        <div class="message-text">${escapeHtml(msg.content)}</div>
        <div class="message-time">${new Date(msg.createdAt).toLocaleTimeString()}</div>
    `;
    div.appendChild(avatar);
    div.appendChild(content);

    if (prepend) {
        messagesContainer.prepend(div);
    } else {
        messagesContainer.appendChild(div);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
}

function escapeHtml(str) {
    if (!str) return '';
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#x27;',
        '/': '&#x2F;',
        '`': '&#x60;',
        '=': '&#x3D;'
    };
    return String(str).replace(/[&<>"'/`=]/g, c => map[c]);
}

/* ========== Обработчики UI ========== */
sendBtn.onclick = () => {
    const text = messageInput.value.trim();
    if (text) sendMessage(text);
};

messageInput.onkeypress = e => {
    if (e.key === 'Enter') sendBtn.click();
    else startTyping();
};

document.getElementById('theme-btn').onclick = toggleTheme;

if (loadMoreBtn) {
    loadMoreBtn.onclick = () => {
        if (currentRoomId) fetchMessages(currentRoomId, true);
    };
}

/* Обработчик входа */
loginBtn.onclick = async () => {
    const name = loginInput.value.trim();
    if (!name) {
        loginError.textContent = 'Имя не может быть пустым';
        return;
    }
    loginBtn.disabled = true;
    const success = await login(name);
    loginBtn.disabled = false;
    if (success) {
        hideLogin();
        fetchRooms();
    }
};

loginInput.onkeypress = e => {
    if (e.key === 'Enter') loginBtn.click();
};

/* Кнопка открытия модального окна создания комнаты */
openCreateRoomBtn.onclick = () => {
    showCreateRoomModal();
};

/* Обработчики в модальном окне создания */
createRoomSubmitBtn.onclick = submitCreateRoom;
newRoomNameInput.onkeypress = e => {
    if (e.key === 'Enter') createRoomSubmitBtn.click();
};

/* ========== Инициализация ========== */
initTheme();
if (!authToken) {
    showLogin();
} else {
    loginOverlay.style.display = 'none';
    chatApp.style.display = 'none';
    fetch('/api/rooms', { headers: { 'Authorization': `Bearer ${authToken}` } })
        .then(resp => {
            if (resp.status === 403 || resp.status === 401) {
                handleAuthFailure();
            } else {
                hideLogin();
                document.getElementById('display-name').value = currentUsername;
                fetchRooms();
            }
        })
        .catch(() => {
            showLogin('Нет соединения с сервером');
        });
}