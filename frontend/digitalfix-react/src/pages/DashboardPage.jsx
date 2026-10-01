import { useEffect, useState } from 'react';
import httpClient from '../services/httpClient';
import { formatStatus } from '../utils/formatters';

export default function DashboardPage() {
  const [orders, setOrders] = useState([]);
  const [kpis, setKpis] = useState({});
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([httpClient.get('/api/workorders'), httpClient.get('/api/report/kpis')])
      .then(([ordersResponse, kpiResponse]) => {
        setOrders(Array.isArray(ordersResponse.data) ? ordersResponse.data : []);
        setKpis(kpiResponse.data || {});
      })
      .catch(() => setError('No fue posible cargar el resumen operativo.'));
  }, []);

  const active = orders.filter((order) => !['CERRADA', 'CANCELADA', 'COMPLETED', 'CANCELLED'].includes(order.status)).length;
  return <main className="page">
    <section className="page-heading">
      <div><p className="eyebrow">Operaciones / Resumen</p><h2>Dashboard operativo</h2><p className="page-description">Visión general de las órdenes y actividad de DigitalFix.</p></div>
    </section>
    {error && <p className="alert alert-error">{error}</p>}
    <section className="metric-grid">
      <Metric label="Órdenes registradas" value={orders.length} />
      <Metric label="Órdenes activas" value={active} />
      <Metric label="Asignadas" value={orders.filter((o) => ['ASIGNADA', 'ASSIGNED'].includes(o.status)).length} />
      <Metric label="Eventos registrados" value={Object.values(kpis).reduce((sum, value) => sum + Number(value || 0), 0)} />
    </section>
    <section className="orders-card dashboard-card">
      <div className="card-header"><div><h3>Actividad reciente</h3><p>Últimas órdenes registradas en la plataforma.</p></div></div>
      {orders.length === 0 ? <div className="empty-state">No hay actividad registrada.</div> : <div className="table-wrapper"><table><thead><tr><th>ID</th><th>Cliente</th><th>Estado</th><th>Creada por</th></tr></thead><tbody>{orders.slice(-8).reverse().map((order) => <tr key={order.id}><td className="order-id">#{order.id}</td><td>{order.customerName}</td><td><Status value={order.status} /></td><td>{order.createdBy || '—'}</td></tr>)}</tbody></table></div>}
    </section>
  </main>;
}

function Metric({ label, value }) { return <div className="metric-card"><span>{label}</span><strong>{value}</strong></div>; }
export function Status({ value }) { return <span className={`status status-${String(value || '').toLowerCase()}`}>{formatStatus(value)}</span>; }
