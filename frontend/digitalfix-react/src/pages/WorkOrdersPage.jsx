import { useEffect, useState } from 'react';
import httpClient from '../services/httpClient';

export default function WorkOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    httpClient
      .get('/api/workorders')
      .then((res) => setOrders(Array.isArray(res.data) ? res.data : []))
      .catch((err) => {
        console.error('No fue posible cargar las órdenes:', err);
        setError('No fue posible cargar las órdenes de trabajo.');
      })
      .finally(() => setIsLoading(false));
  }, []);

  return (
    <main className="page">
      <section className="page-heading">
        <div>
          <p className="eyebrow">Operaciones / Seguimiento</p>
          <h2>Órdenes de trabajo</h2>
          <p className="page-description">Supervisa y consulta el estado de las solicitudes técnicas.</p>
        </div>
        <div className="summary-card">
          <span className="summary-label">Total registradas</span>
          <strong>{orders.length}</strong>
        </div>
      </section>

      <section className="orders-card">
        <div className="card-header">
          <div>
            <h3>Listado de órdenes</h3>
            <p>Información actualizada desde DigitalFix.</p>
          </div>
          <span className="live-badge"><span className="status-dot" /> En línea</span>
        </div>

        {isLoading && <div className="empty-state"><span className="spinner" />Cargando órdenes...</div>}
        {error && <p className="alert alert-error">{error}</p>}
        {!isLoading && !error && orders.length === 0 && (
          <div className="empty-state">
            <span className="empty-icon">✓</span>
            <strong>No hay órdenes registradas</strong>
            <span>Las nuevas solicitudes aparecerán aquí.</span>
          </div>
        )}
        {!isLoading && !error && orders.length > 0 && (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Cliente</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((o) => (
                  <tr key={o.id}>
                    <td className="order-id">#{o.id}</td>
                    <td>{o.customerName || 'Sin cliente asignado'}</td>
                    <td><span className={`status status-${String(o.status || 'pendiente').toLowerCase()}`}>{o.status || 'Pendiente'}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  );
}
