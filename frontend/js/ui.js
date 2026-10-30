// Contenedor global de vistas. Cada archivo de views/ agrega su función aquí.
window.Views = {};

// Valores de los enums tal como los devuelve la API
const ENUMS = {
  rol: ['ADMIN', 'RECEPCIONISTA', 'HUESPED'],
  estadoGeneral: ['ACTIVO', 'INACTIVO'],
  tipoHabitacion: ['INDIVIDUAL', 'DOBLE', 'SUITE', 'FAMILIAR'],
  estadoHabitacion: ['DISPONIBLE', 'MANTENIMIENTO', 'FUERA_DE_SERVICIO'],
  tipoServicio: ['ALIMENTACION', 'TRANSPORTE', 'LAVANDERIA', 'SPA', 'OTRO'],
  estadoReserva: ['PENDIENTE', 'CONFIRMADA', 'CANCELADA', 'FINALIZADA'],
  metodoPago: ['TARJETA', 'EFECTIVO', 'TRANSFERENCIA'],
  estadoPago: ['PENDIENTE', 'COMPLETADO', 'RECHAZADO'],
  estadoReclamo: ['ABIERTO', 'EN_PROCESO', 'CERRADO'],
  estadoDisponibilidad: ['DISPONIBLE', 'OCUPADA', 'BLOQUEADA']
};

const UI = {
  // Escapa texto para meterlo en HTML sin riesgo de inyección
  esc(valor) {
    return String(valor ?? '').replace(/[&<>"']/g, c => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
  },

  dinero(n) {
    return '$' + Number(n || 0).toFixed(2);
  },

  // Fecha de hoy en zona local, formato YYYY-MM-DD
  fechaDe(d) {
    const desfase = d.getTimezoneOffset() * 60000;
    return new Date(d.getTime() - desfase).toISOString().slice(0, 10);
  },
  fechaHoy() {
    return UI.fechaDe(new Date());
  },
  primerDiaMes() {
    const d = new Date();
    return UI.fechaDe(new Date(d.getFullYear(), d.getMonth(), 1));
  },

  // Cantidad de noches entre dos fechas YYYY-MM-DD
  noches(entrada, salida) {
    const a = new Date(entrada + 'T00:00:00');
    const b = new Date(salida + 'T00:00:00');
    return Math.round((b - a) / 86400000);
  },

  badge(estado) {
    const colores = {
      PENDIENTE: 'warning', CONFIRMADA: 'primary', CANCELADA: 'danger', FINALIZADA: 'success',
      ACTIVO: 'success', INACTIVO: 'secondary',
      DISPONIBLE: 'success', MANTENIMIENTO: 'warning', FUERA_DE_SERVICIO: 'danger',
      ABIERTO: 'warning', EN_PROCESO: 'info', CERRADO: 'secondary',
      COMPLETADO: 'success', RECHAZADO: 'danger',
      OCUPADA: 'danger', BLOQUEADA: 'secondary'
    };
    return `<span class="badge text-bg-${colores[estado] || 'light'}">${UI.esc(estado)}</span>`;
  },

  // Notificación flotante. Usa textContent, así que el mensaje nunca se interpreta como HTML.
  toast(mensaje, tipo = 'success') {
    const cont = document.getElementById('toasts');
    const el = document.createElement('div');
    el.className = `alert alert-${tipo} shadow-sm toast-item`;
    el.textContent = mensaje;
    cont.appendChild(el);
    setTimeout(() => el.remove(), 4500);
  },

  // Texto de error con la lista de detalles de validación si los hay
  mensajeError(e) {
    let html = UI.esc(e.message || 'Error inesperado.');
    if (e.detalles && e.detalles.length) {
      html += '<ul class="mb-0 mt-1 ps-3">' + e.detalles.map(d => `<li>${UI.esc(d)}</li>`).join('') + '</ul>';
    }
    return html;
  },

  mostrarError(elemento, e) {
    elemento.innerHTML = `<div class="alert alert-danger py-2 mb-2">${UI.mensajeError(e)}</div>`;
  },

  limpiarError(elemento) {
    if (elemento) elemento.innerHTML = '';
  },

  aviso(elemento, texto, tipo = 'info') {
    elemento.innerHTML = `<div class="alert alert-${tipo}">${UI.esc(texto)}</div>`;
  },

  cargando(elemento) {
    elemento.innerHTML = '<div class="text-center py-5"><div class="spinner-border text-primary" role="status"></div></div>';
  },

  opciones(lista, seleccionado, { vacia = null } = {}) {
    let html = vacia !== null ? `<option value="">${UI.esc(vacia)}</option>` : '';
    html += lista.map(o => {
      const marcada = String(o.v) === String(seleccionado ?? '') ? 'selected' : '';
      return `<option value="${UI.esc(o.v)}" ${marcada}>${UI.esc(o.t)}</option>`;
    }).join('');
    return html;
  },

  enumAOpciones(lista) {
    return lista.map(x => ({ v: x, t: x }));
  },

  abrirModal(titulo, html) {
    document.getElementById('modalTitulo').textContent = titulo;
    document.getElementById('modalCuerpo').innerHTML = html;
    bootstrap.Modal.getOrCreateInstance(document.getElementById('modal')).show();
  },

  cerrarModal() {
    bootstrap.Modal.getOrCreateInstance(document.getElementById('modal')).hide();
  }
};
