import { useEffect, useState } from 'react';
import ProjectList from './components/ProjectList';
import TaskBoard from './components/TaskBoard';
import Login from './components/Login';
import { getCurrentUser, logout } from './api/client';

export default function App() {
  const [selectedProjectId, setSelectedProjectId] = useState(null);
  const [user, setUser] = useState(null);
  const [isCheckingAuth, setIsCheckingAuth] = useState(true);

  useEffect(() => {
    const handleUnauthorized = () => {
      setUser(null);
      setSelectedProjectId(null);
    };

    window.addEventListener('auth:unauthorized', handleUnauthorized);
    getCurrentUser()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setIsCheckingAuth(false));

    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
  }, []);

  if (isCheckingAuth) {
    return <div className="auth-loading">Checking your session...</div>;
  }

  if (!user) {
    return <Login onLogin={setUser} />;
  }

  const handleLogout = async () => {
    try {
      await logout();
    } finally {
      setUser(null);
      setSelectedProjectId(null);
    }
  };

  return (
    <div className="layout">
      <header className="topbar">
        <span className="brand">✓ TaskFlow</span>
        <span className="brand-sub">Project &amp; task manager</span>
        <span className="user-session">
          <span>{user.username}</span>
          <button type="button" onClick={handleLogout}>Log out</button>
        </span>
      </header>

      <div className="app">
        <aside className="sidebar">
          <h2 className="sidebar-title">Projects</h2>
          <ProjectList
            selectedProjectId={selectedProjectId}
            onSelect={setSelectedProjectId}
          />
        </aside>

        <main className="board">
          {selectedProjectId ? (
            <TaskBoard projectId={selectedProjectId} />
          ) : (
            <div className="empty-state">
              <p>Select a project from the left to view its tasks.</p>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
