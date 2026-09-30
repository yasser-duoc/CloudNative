import { Link, Navigate, Route, Routes } from 'react-router-dom';
import { useState } from 'react';
import { useIsAuthenticated } from '@azure/msal-react';
import ProtectedRoute from './components/ProtectedRoute';
import WorkOrdersPage from './pages/WorkOrdersPage';
import ForbiddenPage from './pages/ForbiddenPage';
import { login, logout } from './services/authService';
import './styles.css';

export default function App() {
  const isAuthenticated = useIsAuthenticated();
  const [loginError, setLoginError] = useState('');

  const handleLogin = () => {
    setLoginError('');
    login()
      .catch((error) => {
        console.error('Error de inicio de sesión con Microsoft:', error);
        setLoginError(error?.errorMessage || error?.message || 'No fue posible iniciar sesión con Microsoft.');
      });
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">D</span>
          <div>
            <strong>DigitalFix</strong>
            <small>Gestión técnica</small>
          </div>
        </div>
        <nav className="sidebar-nav" aria-label="Navegación principal">
          <Link className="nav-link nav-link-active" to="/workorders">
            <span aria-hidden="true">▦</span>
            Órdenes de trabajo
          </Link>
        </nav>
        <div className="sidebar-footer">
          <span className="status-dot" />
          Servicios operativos
        </div>
      </aside>

      <div className="main-shell">
        <header className="topbar">
          <div>
            <p className="eyebrow">Panel de control</p>
            <h1>Centro de operaciones</h1>
          </div>
          <div className="topbar-actions">
            {isAuthenticated ? (
              <button className="button button-outline" onClick={logout}>Cerrar sesión</button>
            ) : (
              <button className="button button-primary" onClick={handleLogin}>Iniciar sesión</button>
            )}
          </div>
        </header>
        <div className="content-area">
          {loginError && <p className="alert alert-error" role="alert">Error de autenticación: {loginError}</p>}
          <Routes>
            <Route path="/" element={<Navigate to="/workorders" replace />} />
            <Route element={<ProtectedRoute />}>
              <Route path="/workorders" element={<WorkOrdersPage />} />
            </Route>
            <Route path="/forbidden" element={<ForbiddenPage />} />
            <Route path="*" element={<Navigate to="/workorders" replace />} />
          </Routes>
        </div>
      </div>
    </div>
  );
}
