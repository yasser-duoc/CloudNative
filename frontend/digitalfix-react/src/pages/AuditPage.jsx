import { useEffect, useState } from 'react';
import httpClient from '../services/httpClient';

export default function AuditPage() {
  const [events, setEvents] = useState([]);
  const [orderId, setOrderId] = useState('');
  const [error, setError] = useState('');
  const load = (path = '/api/audit') => httpClient.get(path).then((response) => setEvents(response.data || [])).catch(() => setError('No fue posible cargar la auditoría.'));
  useEffect(() => { load(); }, []);
  const search = (event) => { event.preventDefault(); load(orderId ? `/api/audit/workorders/${orderId}` : '/api/audit'); };
  return <main className="page"><section className="page-heading"><div><p className="eyebrow">Cumplimiento / Trazabilidad</p><h2>Auditoría</h2><p className="page-description">Timeline de acciones realizadas sobre las órdenes.</p></div></section><form className="filter-bar" onSubmit={search}><input type="number" min="1" placeholder="ID de orden" value={orderId} onChange={(e) => setOrderId(e.target.value)} /><button className="button button-primary" type="submit">Buscar timeline</button><button className="button button-outline" type="button" onClick={() => { setOrderId(''); load(); }}>Todas</button></form>{error && <p className="alert alert-error">{error}</p>}<section className="orders-card"><div className="table-wrapper"><table><thead><tr><th>Evento</th><th>Orden</th><th>Estado</th><th>Actor</th><th>Fecha</th></tr></thead><tbody>{events.map((event) => <tr key={event.id}><td>{event.eventType}</td><td>#{event.workOrderId}</td><td>{event.status || '—'}</td><td>{event.actor || '—'}</td><td>{event.eventTimestamp ? new Date(event.eventTimestamp).toLocaleString() : '—'}</td></tr>)}</tbody></table></div>{events.length === 0 && <div className="empty-state">No hay eventos para mostrar.</div>}</section></main>;
}
