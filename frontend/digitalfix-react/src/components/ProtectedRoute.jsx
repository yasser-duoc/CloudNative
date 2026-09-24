import { useEffect } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useMsal, useIsAuthenticated } from '@azure/msal-react';
import { loginRequest } from '../config/authConfig';
import { getRoles } from '../services/authService';

export default function ProtectedRoute({ roles = [] }) {
  const { instance, accounts } = useMsal();
  const isAuthenticated = useIsAuthenticated();

  useEffect(() => {
    if (!isAuthenticated) {
      instance.loginRedirect(loginRequest).catch(() => {});
    }
  }, [isAuthenticated, instance]);

  if (!isAuthenticated) {
    return <div>Redirigiendo a Azure AD…</div>;
  }

  const userRoles = getRoles(accounts[0]);

  if (roles.length > 0 && !roles.some((r) => userRoles.includes(r))) {
    return <Navigate to="/forbidden" replace />;
  }

  return <Outlet />;
}
