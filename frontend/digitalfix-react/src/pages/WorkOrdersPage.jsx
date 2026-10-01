import { useEffect, useState } from 'react';
import httpClient from '../services/httpClient';
import { Status } from './DashboardPage';

const statuses = ['', 'CREADA', 'ASIGNADA', 'EN_DESPLAZAMIENTO', 'EN_EJECUCIÓN', 'CERRADA', 'CANCELADA'];

export default function WorkOrdersPage() {
  const [orders, setOrders] = useState([]);
  const [services, setServices] = useState([]);
  const [filters, setFilters] = useState({ status: '', from: '', to: '' });
  const [form, setForm] = useState({ customerName: '', serviceId: '', description: '' });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = () => {
    setLoading(true);
    const queryFilters = { ...filters };
    if (queryFilters.from) queryFilters.from = `${queryFilters.from}T00:00:00Z`;
    if (queryFilters.to) queryFilters.to = `${queryFilters.to}T23:59:59Z`;
    const query = new URLSearchParams(Object.entries(queryFilters).filter(([, value]) => value));
    Promise.all([httpClient.get(`/api/workorders${query.toString() ? `?${query}` : ''}`), httpClient.get('/api/catalog/services')])
      .then(([ordersResponse, servicesResponse]) => { setOrders(Array.isArray(ordersResponse.data) ? ordersResponse.data : []); setServices(servicesResponse.data || []); })
      .catch(() => setError('No fue posible cargar las órdenes.'))
      .finally(() => setLoading(false));
  };
  useEffect(() => { load(); }, []);
  const create = (event) => {
    event.preventDefault();
    httpClient.post('/api/workorders', { ...form, serviceId: Number(form.serviceId) })
      .then(() => { setForm({ customerName: '', serviceId: '', description: '' }); load(); })
      .catch(() => setError('No fue posible crear la orden.'));
  };
  const changeStatus = (order) => {
    const next = window.prompt(`Nuevo estado para la orden #${order.id}`, order.status);
    if (!next || !statuses.includes(next)) return;
    httpClient.put(`/api/workorders/${order.id}/status`, { status: next }).then(load).catch(() => setError('La transición de estado no es válida.'));
  };
  return <main className="page">
    <section className="page-heading"><div><p className="eyebrow">Operaciones / Seguimiento</p><h2>Órdenes de trabajo</h2><p className="page-description">Crea, consulta y actualiza solicitudes técnicas.</p></div><div className="summary-card"><span className="summary-label">Total filtradas</span><strong>{orders.length}</strong></div></section>
    {error && <p className="alert alert-error">{error}</p>}
    <section className="orders-card create-card"><div className="card-header"><div><h3>Nueva orden</h3><p>Registra una solicitud de mantención.</p></div></div><form className="form-grid form-grid-wide" onSubmit={create}><input required placeholder="Cliente" value={form.customerName} onChange={(e) => setForm({ ...form, customerName: e.target.value })} /><select required value={form.serviceId} onChange={(e) => setForm({ ...form, serviceId: e.target.value })}><option value="">Selecciona un servicio</option>{services.map((service) => <option key={service.id} value={service.id}>{service.name}</option>)}</select><textarea placeholder="Descripción del problema" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /><button className="button button-primary" type="submit">Crear orden</button></form></section>
    <section className="filter-bar"><select value={filters.status} onChange={(e) => setFilters({ ...filters, status: e.target.value })}>{statuses.map((status) => <option key={status} value={status}>{status || 'Todos los estados'}</option>)}</select><input type="date" value={filters.from} onChange={(e) => setFilters({ ...filters, from: e.target.value })} /><input type="date" value={filters.to} onChange={(e) => setFilters({ ...filters, to: e.target.value })} /><button className="button button-primary" onClick={load}>Aplicar filtros</button><button className="button button-outline" onClick={() => { setFilters({ status: '', from: '', to: '' }); setTimeout(load, 0); }}>Limpiar</button></section>
    <section className="orders-card"><div className="card-header"><div><h3>Listado de órdenes</h3><p>Actualizado desde DigitalFix.</p></div><span className="live-badge"><span className="status-dot" /> En línea</span></div>{loading ? <div className="empty-state"><span className="spinner" />Cargando órdenes...</div> : orders.length === 0 ? <div className="empty-state">No hay órdenes para los filtros seleccionados.</div> : <div className="table-wrapper"><table><thead><tr><th>ID</th><th>Cliente</th><th>Servicio</th><th>Estado</th><th>Creada por</th><th /></tr></thead><tbody>{orders.map((order) => <tr key={order.id}><td className="order-id">#{order.id}</td><td>{order.customerName}</td><td>{services.find((service) => service.id === order.serviceId)?.name || `Servicio #${order.serviceId}`}</td><td><Status value={order.status} /></td><td>{order.createdBy || '—'}</td><td><button className="button button-outline" onClick={() => changeStatus(order)}>Cambiar estado</button></td></tr>)}</tbody></table></div>}</section>
  </main>;
}
