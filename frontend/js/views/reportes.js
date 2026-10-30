// Estadísticas del periodo y listado de pagos (ADMIN y RECEPCIONISTA)
Views.reportes = async function (el) {
  el.innerHTML = `
    <h3 class="mb-3">Estadísticas y pagos</h3>
    <div class="card shadow-sm mb-4">
      <div class="card-body">
        <form id="formRango" class="row g-2 align-items-end" novalidate>
          <div class="col-sm-4 col-md-3">
            <label class="form-label small">Desde *</label>
            <input type="date" name="desde" class="form-control" value="${UI.primerDiaMes()}" required>
          </div>
          <div class="col-sm-4 col-md-3">
            <label class="form-label small">Hasta *</label>
            <input type="date" name="hasta" class="form-control" value="${UI.fechaHoy()}" required>
          </div>
          <div class="col-md-3">
            <button class="btn btn-primary w-100" type="submit">Consultar</button>
          </div>
        </form>
      </div>
    </div>
    <div id="estadisticas"></div>
    <h5 class="mt-4">Pagos del periodo</h5>
    <div class="card shadow-sm"><div class="card-body p-0"><div class="tabla-scroll" id="pagos"></div></div></div>`;

  const form = document.getElementById('formRango');
  const estadisticas = document.getElementById('estadisticas');
  const pagos = document.getElementById('pagos');

  function tarjeta(titulo, valor) {
    return `<div class="col-6 col-md-3">
      <div class="card shadow-sm h-100 tarjeta-stat"><div class="card-body">
        <div class="text-muted small">${titulo}</div>
        <div class="fs-4 fw-semibold">${valor}</div>
      </div></div>
    </div>`;
  }

  function pintarEstadisticas(e) {
    const porEstado = e.reservasPorEstado || {};
    const filasEstado = Object.keys(porEstado).map(k =>
      `<tr><td>${UI.badge(k)}</td><td class="text-end">${UI.esc(porEstado[k])}</td></tr>`).join('');

    estadisticas.innerHTML = `
      <div class="row g-3 mb-3">
        ${tarjeta('Reservas', UI.esc(e.reservasTotales))}
        ${tarjeta('Cancelaciones', UI.esc(e.cancelaciones))}
        ${tarjeta('Ingreso neto', UI.dinero(e.ingresosTotales))}
        ${tarjeta('Reembolsos', UI.dinero(e.reembolsosTotales))}
        ${tarjeta('Noches ocupadas', UI.esc(e.nochesOcupadas))}
        ${tarjeta('Noches disponibles', UI.esc(e.nochesDisponibles))}
        ${tarjeta('Ocupación', Number(e.ocupacionPorcentaje || 0).toFixed(2) + '%')}
      </div>
      <div class="card shadow-sm mb-2"><div class="card-body">
        <h6>Reservas por estado</h6>
        <table class="table table-sm mb-0"><tbody>${filasEstado || '<tr><td class="text-muted">Sin datos.</td></tr>'}</tbody></table>
      </div></div>`;
  }

  function pintarPagos(datos) {
    const lista = Array.isArray(datos) ? datos : (datos.pagos || []);
    if (!lista.length) {
      pagos.innerHTML = '<div class="p-3 text-muted">No hay pagos en el periodo.</div>';
      return;
    }
    const filas = lista.map(p => `
      <tr>
        <td>${UI.esc(p.id)}</td>
        <td>${p.reservaId ? '<a href="#/reserva/' + UI.esc(p.reservaId) + '">#' + UI.esc(p.reservaId) + '</a>' : '—'}</td>
        <td>${UI.esc(p.metodo)}</td>
        <td>${UI.dinero(p.monto)}</td>
        <td class="small">${UI.esc(p.fecha)}</td>
        <td>${UI.badge(p.estado || p.estadoPago)}</td>
      </tr>`).join('');
    pagos.innerHTML = `
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light"><tr><th>ID</th><th>Reserva</th><th>Método</th><th>Monto</th><th>Fecha</th><th>Estado</th></tr></thead>
        <tbody>${filas}</tbody>
      </table>`;
  }

  async function cargar() {
    if (form.desde.value > form.hasta.value) {
      UI.aviso(estadisticas, 'La fecha inicial no puede ser mayor que la final.', 'warning');
      return;
    }
    const rango = { desde: form.desde.value, hasta: form.hasta.value };
    UI.cargando(estadisticas);
    UI.cargando(pagos);
    try {
      const [est, lista] = await Promise.all([
        Api.get('/estadisticas', rango),
        Api.get('/pagos', rango)
      ]);
      pintarEstadisticas(est);
      pintarPagos(lista);
    } catch (e) {
      UI.mostrarError(estadisticas, e);
      pagos.innerHTML = '';
    }
  }

  form.addEventListener('submit', (e) => {
    e.preventDefault();
    if (!form.reportValidity()) return;
    cargar();
  });
  cargar();
};
