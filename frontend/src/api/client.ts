const BASE_URL = '/api';

async function request(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    credentials: 'include',
  });

  const data = res.status === 204 ? null : await res.json().catch(() => null);

  if (res.status === 401) {
    window.dispatchEvent(new Event('auth:unauthorized'));
  }

  if (!res.ok) {
    const error = new Error(data?.message || 'Request failed');
    throw error;
  }

  return data;
}

export function login(username, password) {
  return request('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
}

export function getCurrentUser() {
  return request('/auth/me');
}

export function logout() {
  return request('/auth/logout', { method: 'POST' });
}

export function getProjects() {
  return request('/projects');
}

export function getTasks(projectId) {
  return request(`/projects/${projectId}/tasks`);
}

export function updateTaskStatus(taskId, status) {
  return request(`/tasks/${taskId}/status?status=${status}`, { method: 'PUT' });
}

export function createTask(projectId, task) {
  return request(`/projects/${projectId}/tasks`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(task),
  });
}
