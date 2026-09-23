import { useEffect, useState } from 'react';
import httpClient from '../services/httpClient';

export default function WorkOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    httpClient
      .get('/api/workorders')
      .then((res) => setOrders(res.data))
      .catch((err) => {
        console.error(err);
        setError('No fue posible cargar las órdenes de trabajo.');
      });
  }, []);

  return (
    <main>
      <h2>Órdenes de trabajo</h2>
      {error && <p role="alert">{error}</p>}
      <ul>
        {orders.map((o) => (
          <li key={o.id}>
            #{o.id} — {o.customerName} [{o.status}]
          </li>
        ))}
      </ul>
    </main>
  );
}
