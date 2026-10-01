const statusLabels = {
  PENDING: 'Creada',
  CREADA: 'Creada',
  ASIGNADA: 'Asignada',
  EN_DESPLAZAMIENTO: 'En desplazamiento',
  'EN_EJECUCIÓN': 'En ejecución',
  CERRADA: 'Cerrada',
  CANCELADA: 'Cancelada',
};

export function formatStatus(value) {
  const normalizedValue = value === 'PENDING' ? 'CREADA' : value;
  return statusLabels[normalizedValue] || String(normalizedValue || '—').replaceAll('_', ' ');
}

export function formatChileanPesos(value) {
  return new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency: 'CLP',
    maximumFractionDigits: 0,
  }).format(Number(value) || 0);
}
