import { useEffect, useState } from 'react';
import httpClient from '../services/httpClient';

export default function ReportsPage() {
  const [kpis, setKpis] = useState({});
  const [raw, setRaw] = useState([]);
  const [error, setError] = useState('');
  useEffect(() => { Promise.all([httpClient.get('/api/report/kpis'), httpClient.get('/api/report/kpis/raw')]).then(([summary, detail]) => { setKpis(summary.data || {}); setRaw(detail.data || []); }).catch(() => setError('No fue posible cargar la reportería.')); }, []);
  return <main className="page"><section className="page-heading"><div><p className="eyebrow">Administración / Análisis</p><h2>Reportería</h2><p className="page-description">Indicadores operativos registrados por DigitalFix.</p></div></section>{error && <p className="alert alert-error">{error}</p>}<section className="metric-grid">{Object.entries(kpis).map(([name, value]) => <div className="metric-card" key={name}><span>{name.replaceAll('_', ' ')}</span><strong>{value}</strong></div>)}</section><section className="orders-card"><div className="card-header"><div><h3>Detalle de métricas</h3><p>Eventos utilizados para construir los indicadores.</p></div></div><div className="table-wrapper"><table><thead><tr><th>Métrica</th><th>Orden</th><th>Estado</th><th>Valor</th></tr></thead><tbody>{raw.map((item) => <tr key={item.id}><td>{item.metricName}</td><td>#{item.workOrderId}</td><td>{item.status || '—'}</td><td>{item.metricValue}</td></tr>)}</tbody></table></div></section></main>;
}
