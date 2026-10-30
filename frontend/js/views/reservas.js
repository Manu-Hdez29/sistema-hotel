// Listado de reservas (huésped: solo las suyas; los demás: todas)
Views.reservas = async function (el) {
  const opcionesEstado = ['<option value="">Todos</option>']
    .concat(ENUMS.estadoReserva.map(e => `<option value="${e}">${e}</option>`)).join('');

  el.innerHTML = `
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
      <h3 class="mb-0">Reservas</h3>
      <a class="btn btn-outline-primary btn-sm" href="#/disponibilidad">+ Nueva reserva</a>
    </div>
    <div class="card shadow-sm mb-3">
      <div class="card-body">
        <form id="formFiltro" class="row g-2 align-items-end">
          <div class="col-sm-4 col-md-3">
            <label class="form-label small">Estado</label>
            <select name="estado" class="form-select">${opcionesEstado}</select>
          </div>
          <div class="col-sm-4 col-md-3">
            <label class="form-label small">Entrada desde</label>
            <input type="date" name="desde" class="form-control">
          </div>
          <div class="col-sm-4 col-md-3">
            <label class="form-label small">Entrada hasta</label>
            <input type="date" name="hasta" class="form-control">
          </div>
          <div class="col-md-3">
            <button class="btn btn-primary w-100" type="submit">Filtrar</button>
          </div>
        </form>
      </div>
    </div>
    <div id="lista"></div>`;

  const form = document.getElementById('formFiltro');
  const lista = document.getElementById('lista');

  async function cargar() {
    UI.cargando(lista);
    try {
      const datos = await Api.get('/reservas', {
        estado: form.estado.value,
        desde: form.desde.value,
        hasta: form.hasta.value
      });
      pintar(datos);
    } catch (e) {
      UI.mostrarError(lista, e);
    }
  }

  function pintar(datos) {
    if (!datos.length) {
      UI.aviso(lista, 'No hay reservas con esos filtros.', 'light');
      return;
    }
    const filas = datos.map(r => `
      <tr>
        <td><a href="#/reserva/${r.id}">#${r.id}</a></td>
        <td>${UI.esc(r.hotelNombre)}<div class="small text-muted">${UI.esc(r.sucursalNombre)} · hab. ${UI.esc(r.numeroHabitacion)}</div></td>
        <td>${UI.esc(r.titular ? r.titular.nombreCompleto : '—')}</td>
        <td>${UI.esc(r.fechaEntrada)} → ${UI.esc(r.fechaSalida)}<div class="small text-muted">${UI.esc(r.noches)} noche(s)</div></td>
        <td>${UI.dinero(r.precioTotal)}</td>
        <td>${UI.dinero(r.saldo)}</td>
        <td>${UI.badge(r.estado)}</td>
        <td class="text-end"><a class="btn btn-sm btn-outline-secondary" href="#/reserva/${r.id}">Ver</a></td>
      </tr>`).join('');

    lista.innerHTML = `
      <div class="card shadow-sm">
        <div class="card-body p-0">
          <div class="tabla-scroll">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light">
                <tr><th>ID</th><th>Hotel y habitación</th><th>Titular</th><th>Fechas</th>
                    <th>Total</th><th>Saldo</th><th>Estado</th><th></th></tr>
              </thead>
              <tbody>${filas}</tbody>
            </table>
          </div>
        </div>
      </div>`;
  }

  form.addEventListener('submit', (e) => {
    e.preventDefault();
    if (form.desde.value && form.hasta.value && form.desde.value > form.hasta.value) {
      UI.aviso(lista, 'La fecha inicial no puede ser mayor que la final.', 'warning');
      return;
    }
    cargar();
  });
  cargar();
};

// Detalle de una reserva con sus acciones (pagar, cambiar fechas, cancelar, finalizar)
Views.reservaDetalle = async function (el, id) {
  const esStaff = ['ADMIN', 'RECEPCIONISTA'].includes(Session.rol());
  UI.cargando(el);

  let r;
  try {
    r = await Api.get('/reservas/' + id);
  } catch (e) {
    UI.mostrarError(el, e);
    return;
  }

  const activa = r.estado === 'PENDIENTE' || r.estado === 'CONFIRMADA';
  const conSaldo = Number(r.saldo) > 0;

  const servicios = r.servicios && r.servicios.length
    ? `<table class="table table-sm mb-0"><thead><tr><th>Servicio</th><th>Cant.</th><th class="text-end">Subtotal</th></tr></thead><tbody>
        ${r.servicios.map(s => `<tr><td>${UI.esc(s.nombre)}</td><td>${UI.esc(s.cantidad)}</td><td class="text-end">${UI.dinero(s.subtotal)}</td></tr>`).join('')}
       </tbody></table>`
    : '<p class="small text-muted mb-0">Sin servicios adicionales.</p>';

  const pagos = r.pagos && r.pagos.length
    ? `<table class="table table-sm mb-0"><thead><tr><th>Fecha</th><th>Método</th><th class="text-end">Monto</th><th>Estado</th></tr></thead><tbody>
        ${r.pagos.map(p => `<tr><td class="small">${UI.esc(p.fecha)}</td><td>${UI.esc(p.metodo)}</td><td class="text-end">${UI.dinero(p.monto)}</td><td>${UI.badge(p.estado)}</td></tr>`).join('')}
       </tbody></table>`
    : '<p class="small text-muted mb-0">Aún no hay pagos registrados.</p>';

  const huespedes = (r.huespedes || []).map(h => UI.esc(h.nombreCompleto)).join(', ') || '—';

  // Acciones según estado y rol. El backend también valida; esto solo evita botones que no aplican.
  let acciones = '';
  if (activa && conSaldo) {
    acciones += `
      <form id="formPago" class="mb-4" novalidate>
        <h6>Registrar pago</h6>
        <div class="mb-2">
          <select name="metodo" class="form-select form-select-sm">${UI.opciones(UI.enumAOpciones(ENUMS.metodoPago), 'TARJETA')}</select>
        </div>
        <div class="input-group input-group-sm mb-2">
          <span class="input-group-text">$</span>
          <input type="number" name="monto" class="form-control" min="0.01" step="0.01" max="${Number(r.saldo)}" value="${Number(r.saldo).toFixed(2)}" required>
        </div>
        <button class="btn btn-sm btn-success w-100" type="submit">Pagar</button>
        <div class="form-text">Pago simulado, sin pasarela. Si cubre todo el saldo, la reserva queda CONFIRMADA.</div>
      </form>`;
  }
  if (activa) {
    acciones += `
      <form id="formFechas" class="mb-4" novalidate>
        <h6>Cambiar fechas</h6>
        <div class="row g-2 mb-2">
          <div class="col-6"><input type="date" name="fechaEntrada" class="form-control form-control-sm" value="${UI.esc(r.fechaEntrada)}" required></div>
          <div class="col-6"><input type="date" name="fechaSalida" class="form-control form-control-sm" value="${UI.esc(r.fechaSalida)}" required></div>
        </div>
        <button class="btn btn-sm btn-outline-primary w-100" type="submit">Actualizar fechas</button>
      </form>
      <form id="formCancelar" class="mb-2" novalidate>
        <h6>Cancelar reserva</h6>
        <textarea name="motivo" class="form-control form-control-sm mb-2" rows="2" maxlength="200" placeholder="Motivo (opcional)"></textarea>
        <button class="btn btn-sm btn-outline-danger w-100" type="submit">Cancelar reserva</button>
        <div class="form-text">Aplica la política de cancelación del hotel según los días que faltan para la entrada.</div>
      </form>`;
  }
  if (r.estado === 'CONFIRMADA' && esStaff) {
    acciones += `
      <hr>
      <button class="btn btn-sm btn-primary w-100" id="btnFinalizar">Finalizar estadía</button>
      <div class="form-text">Solo se finaliza si el saldo está en cero.</div>`;
  }
  if (!acciones) {
    acciones = '<p class="small text-muted mb-0">No hay acciones disponibles para esta reserva en su estado actual.</p>';
  }

  el.innerHTML = `
    <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-3">
      <div>
        <a href="#/reservas" class="small">← Volver a reservas</a>
        <h3 class="mb-0">Reserva #${UI.esc(r.id)}</h3>
      </div>
      <div>${UI.badge(r.estado)}</div>
    </div>

    <div class="row g-3">
      <div class="col-lg-7">
        <div class="card shadow-sm mb-3"><div class="card-body">
          <h5>Estadía</h5>
          <dl class="row mb-0 small">
            <dt class="col-sm-4">Hotel</dt><dd class="col-sm-8">${UI.esc(r.hotelNombre)} · ${UI.esc(r.sucursalNombre)}</dd>
            <dt class="col-sm-4">Habitación</dt><dd class="col-sm-8">${UI.esc(r.numeroHabitacion)} · ${UI.esc(r.tipoHabitacion)}</dd>
            <dt class="col-sm-4">Entrada</dt><dd class="col-sm-8">${UI.esc(r.fechaEntrada)}</dd>
            <dt class="col-sm-4">Salida</dt><dd class="col-sm-8">${UI.esc(r.fechaSalida)}</dd>
            <dt class="col-sm-4">Noches</dt><dd class="col-sm-8">${UI.esc(r.noches)}</dd>
            <dt class="col-sm-4">Titular</dt><dd class="col-sm-8">${UI.esc(r.titular ? r.titular.nombreCompleto : '—')}</dd>
            <dt class="col-sm-4">Huéspedes</dt><dd class="col-sm-8">${huespedes}</dd>
            <dt class="col-sm-4">Solicitudes</dt><dd class="col-sm-8">${UI.esc(r.solicitudesEspeciales || '—')}</dd>
          </dl>
        </div></div>

        <div class="card shadow-sm mb-3"><div class="card-body">
          <h5>Servicios</h5>
          ${servicios}
        </div></div>

        <div class="card shadow-sm"><div class="card-body">
          <h5>Pagos</h5>
          ${pagos}
        </div></div>
      </div>

      <div class="col-lg-5">
        <div class="card shadow-sm mb-3"><div class="card-body">
          <h5>Resumen</h5>
          <div class="d-flex justify-content-between"><span>Total</span><strong>${UI.dinero(r.precioTotal)}</strong></div>
          <div class="d-flex justify-content-between"><span>Pagado</span><span>${UI.dinero(r.totalPagado)}</span></div>
          <hr class="my-2">
          <div class="d-flex justify-content-between fs-5"><span>Saldo</span><strong>${UI.dinero(r.saldo)}</strong></div>
        </div></div>

        <div class="card shadow-sm"><div class="card-body">
          <h5>Acciones</h5>
          <div id="accionError" class="mb-2"></div>
          ${acciones}
        </div></div>
      </div>
    </div>`;

  const errorEl = document.getElementById('accionError');
  const recargar = () => Views.reservaDetalle(el, id);

  const formPago = document.getElementById('formPago');
  if (formPago) {
    formPago.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!formPago.reportValidity()) return;
      UI.limpiarError(errorEl);
      try {
        await Api.post(`/reservas/${id}/pagos`, {
          metodo: formPago.metodo.value,
          monto: Number(formPago.monto.value)
        });
        UI.toast('Pago registrado.');
        recargar();
      } catch (err) {
        UI.mostrarError(errorEl, err);
      }
    });
  }

  const formFechas = document.getElementById('formFechas');
  if (formFechas) {
    formFechas.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!formFechas.reportValidity()) return;
      if (formFechas.fechaSalida.value <= formFechas.fechaEntrada.value) {
        UI.mostrarError(errorEl, new ApiError(400, 'La fecha de salida debe ser posterior a la de entrada.'));
        return;
      }
      UI.limpiarError(errorEl);
      try {
        await Api.put(`/reservas/${id}/fechas`, {
          fechaEntrada: formFechas.fechaEntrada.value,
          fechaSalida: formFechas.fechaSalida.value
        });
        UI.toast('Fechas actualizadas. El precio se recalculó con la tarifa vigente.');
        recargar();
      } catch (err) {
        UI.mostrarError(errorEl, err);
      }
    });
  }

  const formCancelar = document.getElementById('formCancelar');
  if (formCancelar) {
    formCancelar.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!confirm('¿Seguro que desea cancelar esta reserva?')) return;
      UI.limpiarError(errorEl);
      try {
        const res = await Api.post(`/reservas/${id}/cancelar`, {
          motivo: formCancelar.motivo.value.trim() || null
        });
        UI.toast(`Reserva cancelada. Cargo ${res.porcentajeCargo}% (${UI.dinero(res.cargo)}). Reembolso: ${UI.dinero(res.montoReembolso)}.`);
        recargar();
      } catch (err) {
        UI.mostrarError(errorEl, err);
      }
    });
  }

  const btnFinalizar = document.getElementById('btnFinalizar');
  if (btnFinalizar) {
    btnFinalizar.addEventListener('click', async () => {
      if (!confirm('¿Finalizar la estadía?')) return;
      UI.limpiarError(errorEl);
      try {
        await Api.patch(`/reservas/${id}/finalizar`);
        UI.toast('Reserva finalizada.');
        recargar();
      } catch (err) {
        UI.mostrarError(errorEl, err);
      }
    });
  }
};
