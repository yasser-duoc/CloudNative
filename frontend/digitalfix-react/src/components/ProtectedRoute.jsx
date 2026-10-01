import { Navigate, Outlet } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { useMsal, useIsAuthenticated } from '@azure/msal-react';
import { getAccessTokenRoles, getRoles } from '../services/authService';

export default function ProtectedRoute({ allowedRoles }) {
  const isAuthenticated = useIsAuthenticated();
  const { accounts } = useMsal();
  const [roles, setRoles] = useState([]);
  const [rolesLoaded, setRolesLoaded] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      setRoles([]);
      setRolesLoaded(false);
      return;
    }

    setRolesLoaded(false);
    getAccessTokenRoles(accounts[0])
      .then((resolvedRoles) => {
        setRoles(resolvedRoles);
        setRolesLoaded(true);
      })
      .catch((error) => {
        console.error('No fue posible obtener los roles para la ruta:', error);
        setRoles(getRoles(accounts[0]));
        setRolesLoaded(true);
      });
  }, [accounts, isAuthenticated]);

  if (!isAuthenticated) {
    return <div>Inicia sesión para continuar.</div>;
  }

  if (allowedRoles && !rolesLoaded) {
    return <div>Cargando permisos...</div>;
  }

  if (allowedRoles && !roles.some((role) => allowedRoles.some((allowedRole) => normalizeRole(role) === normalizeRole(allowedRole)))) {
    return <Navigate to="/forbidden" replace />;
  }

  return <Outlet />;
}

function normalizeRole(role) {
  return String(role)
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/\s+del dominio$/, '')
    .trim();
}
