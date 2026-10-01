import { useEffect, useState } from 'react';
import httpClient from '../services/httpClient';
import { formatChileanPesos } from '../utils/formatters';

export default function CatalogPage() {
  const [services, setServices] = useState([]);
  const [parts, setParts] = useState([]);
  const [error, setError] = useState('');
  const [service, setService] = useState({ name: '', category: '', unitPrice: '', description: '' });

  const load = () => Promise.all([httpClient.get('/api/catalog/services'), httpClient.get('/api/catalog/spareparts')])
    .then(([servicesResponse, partsResponse]) => { setServices(servicesResponse.data || []); setParts(partsResponse.data || []); })
    .catch(() => setError('No fue posible cargar el catálogo.'));
  useEffect(() => { load(); }, []);

  const createService = (event) => {
    event.preventDefault();
    httpClient.post('/api/catalog/services', { ...service, unitPrice: Number(service.unitPrice) || 0 })
      .then(() => { setService({ name: '', category: '', unitPrice: '', description: '' }); load(); })
      .catch(() => setError('No fue posible crear el servicio.'));
  };
  const updateStock = (part) => {
    const stock = window.prompt(`Nuevo stock para ${part.name}`, part.stock);
    if (stock === null || Number.isNaN(Number(stock)) || Number(stock) < 0) return;
    httpClient.patch(`/api/catalog/spareparts/${part.id}/stock`, { stock: Number(stock) }).then(load).catch(() => setError('No fue posible actualizar el stock.'));
  };
  return <main className="page">
    <section className="page-heading"><div><p className="eyebrow">Administración / Catálogo</p><h2>Catálogo técnico</h2><p className="page-description">Servicios, tarifas y disponibilidad de repuestos.</p></div></section>
    {error && <p className="alert alert-error">{error}</p>}
    <section className="catalog-grid">
      <div className="orders-card"><div className="card-header"><div><h3>Nuevo servicio</h3><p>Registra una prestación técnica.</p></div></div><form className="form-grid" onSubmit={createService}><input required placeholder="Nombre" value={service.name} onChange={(e) => setService({ ...service, name: e.target.value })} /><input placeholder="Categoría" value={service.category} onChange={(e) => setService({ ...service, category: e.target.value })} /><label className="field-label">Tarifa en pesos chilenos<input type="number" min="0" step="1" placeholder="Ej: 50000" value={service.unitPrice} onChange={(e) => setService({ ...service, unitPrice: e.target.value })} /></label><textarea placeholder="Descripción" value={service.description} onChange={(e) => setService({ ...service, description: e.target.value })} /><button className="button button-primary" type="submit">Crear servicio</button></form></div>
      <div className="orders-card"><div className="card-header"><div><h3>Servicios ({services.length})</h3><p>Servicios disponibles para las órdenes.</p></div></div><div className="table-wrapper"><table><thead><tr><th>Nombre</th><th>Categoría</th><th>Tarifa (CLP)</th></tr></thead><tbody>{services.map((item) => <tr key={item.id}><td>{item.name}</td><td>{item.category || '—'}</td><td>{formatChileanPesos(item.unitPrice)}</td></tr>)}</tbody></table></div></div>
    </section>
    <section className="orders-card"><div className="card-header"><div><h3>Repuestos ({parts.length})</h3><p>Actualiza el stock disponible.</p></div></div><div className="table-wrapper"><table><thead><tr><th>Nombre</th><th>SKU</th><th>Stock</th><th /></tr></thead><tbody>{parts.map((part) => <tr key={part.id}><td>{part.name}</td><td>{part.sku}</td><td><span className={part.stock < 5 ? 'stock-low' : ''}>{part.stock}</span></td><td><button className="button button-outline" onClick={() => updateStock(part)}>Actualizar</button></td></tr>)}</tbody></table></div></section>
  </main>;
}
