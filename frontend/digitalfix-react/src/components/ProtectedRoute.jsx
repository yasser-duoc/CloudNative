import { Outlet } from 'react-router-dom';
import { useMsal, useIsAuthenticated } from '@azure/msal-react';

export default function ProtectedRoute() {
  const isAuthenticated = useIsAuthenticated();

  if (!isAuthenticated) {
    return <div>Inicia sesión para continuar.</div>;
  }

  return <Outlet />;
}
