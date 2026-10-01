import { Link, Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { useIsAuthenticated, useMsal } from '@azure/msal-react';
import ProtectedRoute from './components/ProtectedRoute';
import WorkOrdersPage from './pages/WorkOrdersPage';
import ForbiddenPage from './pages/ForbiddenPage';
import DashboardPage from './pages/DashboardPage';
import CatalogPage from './pages/CatalogPage';
import ReportsPage from './pages/ReportsPage';
import AuditPage from './pages/AuditPage';
import { canAccess, getAccessTokenRoles, getRoles, login, logout } from './services/authService';
import './styles.css';

export default function App() {
  const isAuthenticated = useIsAuthenticated();
  const { accounts } = useMsal();
  const location = useLocation();
  const [roles, setRoles] = useState([]);

  useEffect(() => {
    if (!isAuthenticated) {
      setRoles([]);
      return;
    }

    getAccessTokenRoles(accounts[0])
      .then(setRoles)
      .catch((error) => {
        console.error('No fue posible obtener los roles del usuario:', error);
        setRoles(getRoles(accounts[0]));
      });
  }, [accounts, isAuthenticated]);
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
          {isAuthenticated && <NavItem to="/dashboard" icon="⌂" label="Dashboard" active={location.pathname === '/dashboard'} />}
          {isAuthenticated && canAccess(roles, ['Admin', 'Supervisor', 'Cliente']) && <NavItem to="/workorders" icon="▦" label="Órdenes de trabajo" active={location.pathname.startsWith('/workorders')} />}
          {isAuthenticated && canAccess(roles, ['Admin', 'Supervisor']) && <NavItem to="/catalog" icon="◈" label="Catálogo técnico" active={location.pathname.startsWith('/catalog')} />}
          {isAuthenticated && canAccess(roles, ['Admin', 'Auditor']) && <NavItem to="/reports" icon="▤" label="Reportería" active={location.pathname.startsWith('/reports')} />}
          {isAuthenticated && canAccess(roles, ['Admin', 'Auditor']) && <NavItem to="/audit" icon="◌" label="Auditoría" active={location.pathname.startsWith('/audit')} />}
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
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route element={<ProtectedRoute />}>
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route element={<ProtectedRoute allowedRoles={['Admin', 'Supervisor', 'Cliente']} />}>
                <Route path="/workorders" element={<WorkOrdersPage />} />
              </Route>
              <Route element={<ProtectedRoute allowedRoles={['Admin', 'Supervisor']} />}>
                <Route path="/catalog" element={<CatalogPage />} />
              </Route>
              <Route element={<ProtectedRoute allowedRoles={['Admin', 'Auditor']} />}>
                <Route path="/reports" element={<ReportsPage />} />
                <Route path="/audit" element={<AuditPage />} />
              </Route>
            </Route>
            <Route path="/forbidden" element={<ForbiddenPage />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </div>
      </div>
    </div>
  );
}

function NavItem({ to, icon, label, active }) {
  return <Link className={`nav-link${active ? ' nav-link-active' : ''}`} to={to}>
    <span aria-hidden="true">{icon}</span>
    {label}
  </Link>;
}
