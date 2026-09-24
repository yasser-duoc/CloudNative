import { Link, Navigate, Route, Routes } from 'react-router-dom';
import { useIsAuthenticated } from '@azure/msal-react';
import ProtectedRoute from './components/ProtectedRoute';
import WorkOrdersPage from './pages/WorkOrdersPage';
import ForbiddenPage from './pages/ForbiddenPage';
import { login, logout } from './services/authService';

export default function App() {
  const isAuthenticated = useIsAuthenticated();

  return (
    <div>
      <nav>
        <Link to="/workorders">Órdenes de trabajo</Link>
        {isAuthenticated ? (
          <button onClick={logout}>Cerrar sesión</button>
        ) : (
          <button onClick={login}>Iniciar sesión</button>
        )}
      </nav>

      <Routes>
        <Route path="/" element={<Navigate to="/workorders" replace />} />
        <Route element={<ProtectedRoute roles={['Admin', 'Supervisor', 'Cliente']} />}>
          <Route path="/workorders" element={<WorkOrdersPage />} />
        </Route>
        <Route path="/forbidden" element={<ForbiddenPage />} />
        <Route path="*" element={<Navigate to="/workorders" replace />} />
      </Routes>
    </div>
  );
}
