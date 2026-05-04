const API_BASE = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' ? 'http://localhost:8080/api' : '/api';
let token = localStorage.getItem('token');
let currentUser = localStorage.getItem('username');
let currentRole = localStorage.getItem('role');

const authOverlay = document.getElementById('auth-overlay');
const authForm = document.getElementById('auth-form');
const authToggle = document.getElementById('auth-toggle');
const authTitle = document.getElementById('auth-title');
const roleSelect = document.getElementById('role');
const welcomeMsg = document.getElementById('welcome-msg');
const logoutBtn = document.getElementById('logout-btn');

const views = document.querySelectorAll('.view');
const navItems = document.querySelectorAll('.sidebar .nav-item[data-target]');

let isLogin = true;

// Init
function initUI() {
    if (token) {
        authOverlay.style.display = 'none';
        
        // Custom greeting
        if (currentRole === 'ROLE_ADMIN') {
            welcomeMsg.innerText = `Admin Dashboard - Welcome, ${currentUser}`;
            document.getElementById('new-project-btn').style.display = 'block';
            document.querySelector('.sidebar .nav-item[data-target="projects"]').style.display = 'block';
            document.querySelector('.sidebar .nav-item[data-target="timeline"]').style.display = 'block';
        } else {
            welcomeMsg.innerText = `Member Portal - Welcome, ${currentUser}`;
            document.getElementById('new-project-btn').style.display = 'none';
            document.getElementById('new-task-btn').style.display = 'none';
            document.querySelector('.sidebar .nav-item[data-target="projects"]').style.display = 'none';
            document.querySelector('.sidebar .nav-item[data-target="timeline"]').style.display = 'none';
            // Hide total projects and completion rate cards for members on dashboard
            document.getElementById('stat-projects').parentElement.style.display = 'none';
            document.getElementById('stat-completion').parentElement.style.display = 'none';
        }
        
        loadDashboard();
    } else {
        authOverlay.style.display = 'flex';
    }
}

initUI();

// Navigation
navItems.forEach(item => {
    item.addEventListener('click', () => {
        navItems.forEach(n => n.classList.remove('active'));
        item.classList.add('active');
        
        views.forEach(v => v.style.display = 'none');
        document.getElementById(`view-${item.dataset.target}`).style.display = 'block';

        if (item.dataset.target === 'dashboard') loadDashboard();
        if (item.dataset.target === 'projects') loadProjects();
        if (item.dataset.target === 'tasks') loadTasks();
    });
});

authToggle.addEventListener('click', () => {
    isLogin = !isLogin;
    authTitle.innerText = isLogin ? 'Login to Your Account' : 'Register an Account';
    authToggle.innerText = isLogin ? 'Need an account? Register' : 'Already have an account? Login';
    roleSelect.style.display = isLogin ? 'none' : 'block';
});

authForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const role = roleSelect.value;

    const endpoint = isLogin ? '/auth/login' : '/auth/register';
    const payload = isLogin ? { username, password } : { username, password, role };

    try {
        const res = await fetch(`${API_BASE}${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) throw new Error('Auth failed');
        const data = await res.json();
        
        token = data.token;
        currentUser = data.username;
        currentRole = data.role;
        
        localStorage.setItem('token', token);
        localStorage.setItem('username', currentUser);
        localStorage.setItem('role', currentRole);

        initUI();
    } catch (err) {
        alert(err.message);
    }
});

logoutBtn.addEventListener('click', () => {
    localStorage.clear();
    location.reload();
});

// Fetch utilities
async function apiGet(path) {
    const res = await fetch(`${API_BASE}${path}`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    if(!res.ok) throw new Error('API GET failed');
    return res.json();
}

async function apiPost(path, body) {
    const res = await fetch(`${API_BASE}${path}`, {
        method: 'POST',
        headers: { 
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}` 
        },
        body: JSON.stringify(body)
    });
    if(!res.ok) throw new Error('API POST failed');
    return res.json();
}

// Logic
async function loadDashboard() {
    try {
        const projects = await apiGet('/projects');
        const tasks = await apiGet('/tasks');

        document.getElementById('stat-projects').innerText = projects.length;
        document.getElementById('stat-tasks').innerText = tasks.filter(t => t.status !== 'DONE').length;

        const done = tasks.filter(t => t.status === 'DONE').length;
        const rate = tasks.length ? Math.round((done / tasks.length) * 100) : 0;
        document.getElementById('stat-completion').innerText = `${rate}%`;

        const dashTasks = document.getElementById('dashboard-tasks');
        dashTasks.innerHTML = '';
        tasks.slice(0, 5).forEach(t => {
            dashTasks.innerHTML += `
                <div class="task-item">
                    <span>${t.title}</span>
                    <span class="status-${t.status.toLowerCase()}">${t.status}</span>
                </div>
            `;
        });
    } catch (e) {
        console.error(e);
    }
}

async function loadProjects() {
    try {
        const projects = await apiGet('/projects');
        const list = document.getElementById('projects-list');
        list.innerHTML = '';
        projects.forEach(p => {
            list.innerHTML += `
                <div class="card">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <div class="card-title">${p.name}</div>
                        <button onclick="openProjectSettings(${p.id}, \`${p.name}\`, \`${p.description || ''}\`, \`${p.deadline || ''}\`)" style="background: none; border: none; cursor: pointer; font-size: 1.2rem;">⚙️</button>
                    </div>
                    <div style="font-size: 0.875rem; color: var(--text-muted); margin-top: 1rem;">${p.description || 'No description'}</div>
                    ${p.deadline ? `<div style="font-size: 0.75rem; color: var(--primary-color); margin-top: 0.5rem;">Deadline: ${p.deadline}</div>` : ''}
                </div>
            `;
        });
    } catch(e) { console.error(e); }
}

async function loadTasks() {
    try {
        const tasks = await apiGet('/tasks');
        const list = document.getElementById('tasks-list');
        list.innerHTML = '';
        tasks.forEach(t => {
            list.innerHTML += `
                <div class="task-item">
                    <div>
                        <strong>${t.title}</strong>
                    </div>
                    <div>
                        <select onchange="updateTaskStatus(${t.id}, this.value)" ${currentRole !== 'ROLE_ADMIN' && currentRole !== 'ROLE_MEMBER' ? 'disabled' : ''}>
                            <option value="TODO" ${t.status === 'TODO' ? 'selected' : ''}>TODO</option>
                            <option value="DOING" ${t.status === 'DOING' ? 'selected' : ''}>DOING</option>
                            <option value="DONE" ${t.status === 'DONE' ? 'selected' : ''}>DONE</option>
                        </select>
                    </div>
                </div>
            `;
        });
    } catch(e) { console.error(e); }
}

window.updateTaskStatus = async function(id, status) {
    try {
        await fetch(`${API_BASE}/tasks/${id}/status?status=${status}`, {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        loadDashboard();
    } catch(e) { console.error(e); }
}

// Modal handling
const modalOverlay = document.getElementById('modal-overlay');
const modalForm = document.getElementById('modal-form');
let modalMode = ''; // 'project' or 'task'

document.getElementById('new-project-btn').addEventListener('click', () => {
    if(currentRole !== 'ROLE_ADMIN') return alert('Only admins can create projects');
    modalMode = 'project';
    document.getElementById('modal-title').innerText = 'New Project';
    document.getElementById('modal-input1').placeholder = 'Project Name';
    document.getElementById('modal-input2').style.display = 'block';
    document.getElementById('modal-select').style.display = 'none';
    modalOverlay.style.display = 'flex';
});

document.getElementById('new-task-btn').addEventListener('click', async () => {
    if(currentRole !== 'ROLE_ADMIN') return alert('Only admins can create tasks');
    modalMode = 'task';
    document.getElementById('modal-title').innerText = 'New Task';
    document.getElementById('modal-input1').placeholder = 'Task Title';
    document.getElementById('modal-input2').style.display = 'none';
    
    // Load projects into select
    const select = document.getElementById('modal-select');
    select.style.display = 'block';
    const projects = await apiGet('/projects');
    select.innerHTML = projects.map(p => `<option value="${p.id}">${p.name}</option>`).join('');

    modalOverlay.style.display = 'flex';
});

document.getElementById('modal-close').addEventListener('click', () => {
    modalOverlay.style.display = 'none';
});

modalForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        if (modalMode === 'project') {
            await apiPost('/projects', {
                name: document.getElementById('modal-input1').value,
                description: document.getElementById('modal-input2').value
            });
            loadProjects();
        } else {
            await apiPost(`/tasks/project/${document.getElementById('modal-select').value}`, {
                title: document.getElementById('modal-input1').value
            });
            loadTasks();
        }
        modalOverlay.style.display = 'none';
        modalForm.reset();
        loadDashboard();
    } catch(e) { console.error(e); }
});

// WebSocket Config
let stompClient = null;

function connectWebSocket() {
    if(!token) return;
    const socket = new SockJS(window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' ? 'http://localhost:8080/ws' : '/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/tasks', function (taskUpdate) {
            console.log("Live task update received!", JSON.parse(taskUpdate.body));
            loadTasks();
            loadDashboard();
            showToast("Task updated live: " + JSON.parse(taskUpdate.body).title);
        });
        
        // Also subscribe to a specific project if needed, or broad project broadcast.
        // We'll just listen to a global broadcast for demo purposes, but normally it's per project
    });
}

function showToast(message) {
    const t = document.createElement('div');
    t.innerText = message;
    t.style.position = 'fixed';
    t.style.bottom = '20px';
    t.style.right = '20px';
    t.style.background = 'var(--primary-color)';
    t.style.color = '#fff';
    t.style.padding = '1rem';
    t.style.borderRadius = '0.5rem';
    t.style.zIndex = '9999';
    document.body.appendChild(t);
    setTimeout(() => t.remove(), 4000);
}

// Call connectWebSocket after login
if(token) connectWebSocket();

// Timeline Loading
async function loadTimeline() {
    try {
        const projects = await apiGet('/projects');
        if(projects.length === 0) return;
        
        // Load timeline for the first project just for demo
        const projectId = projects[0].id; 
        const logs = await apiGet(`/projects/${projectId}/activity`);
        
        const feed = document.getElementById('timeline-feed');
        if(feed) {
            feed.innerHTML = '';
            logs.forEach(log => {
                feed.innerHTML += `
                    <div class="card" style="padding: 1rem; margin-bottom: 0.5rem; border-left: 4px solid var(--primary-color);">
                        <div>${log.message}</div>
                        <div style="font-size: 0.75rem; color: #888; margin-top: 0.5rem;">${new Date(log.timestamp).toLocaleString()}</div>
                    </div>
                `;
            });
        }
    } catch(e) { console.error(e); }
}

const timelineNavItem = document.querySelector('.sidebar .nav-item[data-target="timeline"]');
if(timelineNavItem) {
    timelineNavItem.addEventListener('click', () => {
        navItems.forEach(n => n.classList.remove('active'));
        timelineNavItem.classList.add('active');
        views.forEach(v => v.style.display = 'none');
        document.getElementById('view-timeline').style.display = 'block';
        loadTimeline();
    });
}

const originalAuthSuccess = authForm.onsubmit; // Actually we added listener above. We'll modify it slightly using string replace if needed, or just rely on page reload.



// Project Settings Logic
const settingsModal = document.getElementById('settings-modal');
const settingsForm = document.getElementById('settings-form');
const settingsDeleteBtn = document.getElementById('settings-delete-btn');

document.getElementById('settings-close').addEventListener('click', () => {
    settingsModal.style.display = 'none';
});

window.openProjectSettings = function(id, name, desc, deadline) {
    document.getElementById('settings-id').value = id;
    document.getElementById('settings-name').value = name;
    document.getElementById('settings-desc').value = desc || '';
    document.getElementById('settings-deadline').value = deadline || '';
    settingsModal.style.display = 'flex';
};

settingsForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('settings-id').value;
    const payload = {
        name: document.getElementById('settings-name').value,
        description: document.getElementById('settings-desc').value,
        deadline: document.getElementById('settings-deadline').value || null
    };
    
    try {
        const res = await fetch(API_BASE + '/projects/' + id, {
            method: 'PUT',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify(payload)
        });
        if(!res.ok) {
            const err = await res.text();
            throw new Error(err || 'Failed to update project');
        }
        
        settingsModal.style.display = 'none';
        showToast("Project updated successfully!");
        loadProjects();
        loadDashboard();
    } catch(err) {
        alert(err.message);
    }
});

settingsDeleteBtn.addEventListener('click', async () => {
    const id = document.getElementById('settings-id').value;
    if(!confirm("DANGER ZONE: Are you sure you want to soft-delete this project? This cannot be undone from the UI!")) return;
    
    try {
        const res = await fetch(API_BASE + '/projects/' + id, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + token }
        });
        
        if(!res.ok) {
            const err = await res.text();
            throw new Error(err || 'Failed to delete project. You might not have permission.');
        }
        
        settingsModal.style.display = 'none';
        showToast("Project deleted successfully!");
        loadProjects();
        loadDashboard();
    } catch(err) {
        alert(err.message);
    }
});

