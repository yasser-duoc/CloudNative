import { useEffect, useState } from 'react';
import { useMsal } from '@azure/msal-react';
import { getAccessTokenRoles } from '../services/authService';

export default function ForbiddenPage() {
  const { accounts } = useMsal();
  const [roles, setRoles] = useState([]);

  useEffect(() => {
    getAccessTokenRoles()
      .then(setRoles)
      .catch((error) => {
        console.error('No fue posible verificar los roles:', error);
        setRoles([]);
      });
  }, []);

  return (
    <main>
      <h2>403 — No tienes permisos para ver esta sección</h2>
      <p>Usuario detectado: {accounts[0]?.username || 'No disponible'}</p>
      <p>Roles detectados: {roles.length > 0 ? roles.join(', ') : 'Ninguno'}</p>
      <p>Se requiere uno de estos roles: Admin, Supervisor o Cliente.</p>
    </main>
  );
}
