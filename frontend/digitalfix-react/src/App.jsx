import { Link, Navigate, Route, Routes } from 'react-router-dom';
import { useState } from 'react';
import { useIsAuthenticated } from '@azure/msal-react';
import ProtectedRoute from './components/ProtectedRoute';
import WorkOrdersPage from './pages/WorkOrdersPage';
import ForbiddenPage from './pages/ForbiddenPage';
import { login, logout } from './services/authService';

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
    <div>
      <nav>
        <Link to="/workorders">Órdenes de trabajo</Link>
        {isAuthenticated ? (
          <button onClick={logout}>Cerrar sesión</button>
        ) : (
          <button onClick={handleLogin}>Iniciar sesión</button>
        )}
      </nav>
      {loginError && <p role="alert">Error de autenticación: {loginError}</p>}

      <Routes>
        <Route path="/" element={<Navigate to="/workorders" replace />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/workorders" element={<WorkOrdersPage />} />
        </Route>
        <Route path="/forbidden" element={<ForbiddenPage />} />
        <Route path="*" element={<Navigate to="/workorders" replace />} />
      </Routes>
    </div>
  );
}
