// La reserva se crea al pasar de Datos a Pago, porque el pago necesita su id.

// ---------- Selección guardada entre pantallas ----------
const Seleccion = {
  clave: 'hotel_seleccion',
  guardar(valor) {
    try { sessionStorage.setItem(this.clave, JSON.stringify(valor)); } catch (e) {  }
  },
  leer() {
    try { return JSON.parse(sessionStorage.getItem(this.clave)) || null; } catch (e) { return null; }
  },
  borrar() {
    try { sessionStorage.removeItem(this.clave); } catch (e) {  }
  }
};

// Indicador de pasos
function stepper(actual) {
  const pasos = ['Datos', 'Pago', 'Confirmación'];
  const idx = { datos: 0, pago: 1, confirmacion: 2 }[actual];
  return `<ol class="stepper" aria-label="Pasos de la reserva">
    ${pasos.map((t, i) => {
    const clase = i < idx ? 'hecho' : (i === idx ? 'actual' : '');
    const marca = i < idx ? '✓' : String(i + 1);
    return `<li class="${clase}"><span class="stepper-num">${marca}</span><span class="stepper-txt">${t}</span></li>`;
  }).join('')}
  </ol>`;
}

function avisoSinSeleccion(el) {
  el.innerHTML = `
    <div class="card"><div class="card-body text-center py-5">
      <h5>Aún no ha elegido una habitación</h5>
      <p class="text-muted">Busque fechas y seleccione la habitación que le guste.</p>
      <a class="btn btn-primary" href="#/disponibilidad">Buscar habitaciones</a>
    </div></div>`;
}

// Foto de la habitación según su tipo
function imagenHabitacion(tipo) {
  const fotos = { INDIVIDUAL: 'individual.jpg', DOBLE: 'doble.jpg', SUITE: 'suite.jpg', FAMILIAR: 'family.jpg' };
  return `<div class="mb-3" style="overflow:hidden;border-radius:10px;">
    <img src="assets/habitaciones/${fotos[tipo] || 'doble.jpg'}" alt="Habitación ${UI.esc(tipo)}"
         style="display:block;width:100%;height:auto;"></div>`;
}

// Resumen lateral
function resumenHtml(r) {
  return `
    <div class="card resumen">
      <div class="card-body">
        <h6 class="text-uppercase small text-muted mb-3">Resumen de su reserva</h6>
        ${imagenHabitacion(r.tipoHabitacion)}
        <div class="fw-semibold">${UI.esc(r.hotelNombre)}</div>
        <div class="small text-muted mb-3">${UI.esc(r.sucursalNombre)} · Hab. ${UI.esc(r.numeroHabitacion)} · ${UI.esc(r.tipoHabitacion)}</div>
        <div class="d-flex justify-content-between small"><span>Llegada</span><span>${UI.esc(r.fechaEntrada)}</span></div>
        <div class="d-flex justify-content-between small"><span>Salida</span><span>${UI.esc(r.fechaSalida)}</span></div>
        <div class="d-flex justify-content-between small"><span>Noches</span><span>${UI.esc(r.noches)}</span></div>
        <hr>
        <div class="d-flex justify-content-between"><span>Total</span><strong>${UI.dinero(r.precioTotal)}</strong></div>
        <div class="d-flex justify-content-between small text-muted"><span>Pagado</span><span>${UI.dinero(r.totalPagado)}</span></div>
        <div class="d-flex justify-content-between fs-5 mt-1"><span>Saldo</span><strong>${UI.dinero(r.saldo)}</strong></div>
      </div>
    </div>`;
}

// Paso 1: datos y servicios
async function pasoDatos(el) {
  const sel = Seleccion.leer();
  if (!sel || !sel.hab) { avisoSinSeleccion(el); return; }
  const { hab, busqueda } = sel;
  const esStaff = ['ADMIN', 'RECEPCIONISTA'].includes(Session.rol());
  const usuario = Session.usuario() || {};

  let servicios = [];
  try { servicios = await Api.get('/servicios'); } catch (e) { servicios = []; }

  const noches = UI.noches(busqueda.fechaEntrada, busqueda.fechaSalida);
  const base = Number(hab.precioNoche) * noches;

  el.innerHTML = `
    ${stepper('datos')}
    <div class="row g-4">
      <div class="col-lg-8">
        <div class="card mb-4"><div class="card-body p-4">
          <h5 class="mb-3">Datos del titular</h5>
          <form id="formDatos" novalidate>
            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label">Nombre completo *</label>
                <input name="nombreCompleto" class="form-control" maxlength="100" required value="${UI.esc(usuario.nombreCompleto || '')}">
              </div>
              <div class="col-md-6">
                <label class="form-label">Fecha de nacimiento *</label>
                <input type="date" name="fechaNacimiento" class="form-control" max="${UI.fechaHoy()}" required>
              </div>
              <div class="col-md-6">
                <label class="form-label">DUI o pasaporte *</label>
                <input name="documentoIdentidad" class="form-control" required
                       pattern="(\\d{8}-\\d|[A-Za-z0-9]{6,20})"
                       title="DUI ########-# o pasaporte de 6 a 20 caracteres alfanuméricos">
              </div>
              <div class="col-md-6">
                <label class="form-label">Teléfono *</label>
                <input name="telefono" class="form-control" required pattern="\\d{4}-\\d{4}" title="Formato ####-####" placeholder="7777-8888">
              </div>
              <div class="col-12">
                <label class="form-label">Correo electrónico *</label>
                <input type="email" name="email" class="form-control" maxlength="100" required value="${UI.esc(usuario.email || '')}">
              </div>
              ${esStaff ? `
                <div class="col-12">
                  <label class="form-label">IDs de huéspedes adicionales (separados por coma)</label>
                  <input name="huespedesIds" class="form-control" placeholder="Ej: 4, 5">
                </div>` : ''}
            </div>

            <h5 class="mt-4 mb-2">Servicios adicionales</h5>
            <p class="small text-muted mb-3">Opcional. Se suman al total de la estadía.</p>
            ${servicios.length ? `<div class="row g-2">${servicios.map(s => `
              <div class="col-12 col-md-6">
                <label class="servicio-opcion w-100">
                  <input type="checkbox" class="form-check-input me-2" data-servicio="${s.id}" data-precio="${s.precio}">
                  <span class="flex-grow-1">${UI.esc(s.nombre)}</span>
                  <span class="small text-muted me-2">${UI.dinero(s.precio)}</span>
                  <input type="number" min="1" value="1" class="form-control form-control-sm cantidad" data-cantidad="${s.id}" aria-label="Cantidad">
                </label>
              </div>`).join('')}</div>`
      : '<p class="small text-muted">No hay servicios disponibles.</p>'}

            <label class="form-label mt-4">Solicitudes especiales</label>
            <textarea name="solicitudesEspeciales" class="form-control" rows="2" maxlength="250" placeholder="Ej: llegada tarde, cuna para bebé"></textarea>

            <div id="formError" class="mt-3"></div>

            <div class="d-flex justify-content-between align-items-center gap-2 mt-4 flex-wrap">
              <a class="btn btn-outline-secondary" href="#/disponibilidad">Volver a resultados</a>
              <button class="btn btn-primary px-4" type="submit">Continuar al pago</button>
            </div>
          </form>
        </div></div>
      </div>

      <aside class="col-lg-4">
        <div class="card resumen"><div class="card-body">
          <h6 class="text-uppercase small text-muted mb-3">Resumen de su reserva</h6>
          ${imagenHabitacion(hab.tipoHabitacion)}
          <div class="fw-semibold">${UI.esc(hab.hotelNombre)}</div>
          <div class="small text-muted mb-3">${UI.esc(hab.sucursalNombre)} · Hab. ${UI.esc(hab.numeroHabitacion)} · ${UI.esc(hab.tipoHabitacion)}</div>
          <div class="d-flex justify-content-between small"><span>Llegada</span><span>${UI.esc(busqueda.fechaEntrada)}</span></div>
          <div class="d-flex justify-content-between small"><span>Salida</span><span>${UI.esc(busqueda.fechaSalida)}</span></div>
          <div class="d-flex justify-content-between small"><span>Noches</span><span>${noches}</span></div>
          <div class="d-flex justify-content-between small"><span>Huéspedes</span><span>${UI.esc(busqueda.huespedes)}</span></div>
          <hr>
          <div class="d-flex justify-content-between small"><span>Habitación (${noches} × ${UI.dinero(hab.precioNoche)})</span><span>${UI.dinero(base)}</span></div>
          <div class="d-flex justify-content-between small"><span>Servicios</span><span id="totalServicios">$0.00</span></div>
          <hr>
          <div class="d-flex justify-content-between fs-5"><span>Total</span><strong id="totalGeneral">${UI.dinero(base)}</strong></div>
        </div></div>
      </aside>
    </div>`;

  const form = document.getElementById('formDatos');
  form.fechaNacimiento.max = UI.fechaHoy();

  // Total en vivo: habitación + servicios marcados x cantidad
  function recalcular() {
    let extra = 0;
    form.querySelectorAll('[data-servicio]').forEach(c => {
      if (!c.checked) return;
      const cant = Number(form.querySelector(`[data-cantidad="${c.dataset.servicio}"]`).value) || 1;
      extra += Number(c.dataset.precio) * cant;
    });
    document.getElementById('totalServicios').textContent = UI.dinero(extra);
    document.getElementById('totalGeneral').textContent = UI.dinero(base + extra);
  }
  form.addEventListener('change', recalcular);
  form.addEventListener('input', recalcular);

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!form.reportValidity()) return;
    const errorEl = document.getElementById('formError');
    UI.limpiarError(errorEl);

    const v = nombre => form.elements[nombre].value.trim();
    const servicios = [...form.querySelectorAll('[data-servicio]')]
        .filter(c => c.checked)
        .map(c => ({
          servicioId: Number(c.dataset.servicio),
          cantidad: Number(form.querySelector(`[data-cantidad="${c.dataset.servicio}"]`).value) || 1
        }));
    const huespedesIds = esStaff && v('huespedesIds')
        ? v('huespedesIds').split(',').map(x => Number(x.trim())).filter(n => n > 0)
        : [];

    const body = {
      habitacionId: hab.habitacionId,
      fechaEntrada: busqueda.fechaEntrada,
      fechaSalida: busqueda.fechaSalida,
      solicitudesEspeciales: v('solicitudesEspeciales') || null,
      titular: {
        nombreCompleto: v('nombreCompleto'),
        fechaNacimiento: v('fechaNacimiento'),
        documentoIdentidad: v('documentoIdentidad'),
        telefono: v('telefono'),
        email: v('email')
      },
      huespedesIds,
      servicios
    };

    const boton = form.querySelector('button[type=submit]');
    boton.disabled = true;
    try {
      const reserva = await Api.post('/reservas', body);
      ir('#/reservar/pago/' + reserva.id);
    } catch (err) {
      UI.mostrarError(errorEl, err);
      boton.disabled = false;
    }
  });
}

// Paso 2: pago simulado
const METODOS_PAGO = [
  { v: 'TARJETA', t: 'Tarjeta', ayuda: 'Crédito o débito' },
  { v: 'TRANSFERENCIA', t: 'Transferencia', ayuda: 'Banco o billetera' },
  { v: 'EFECTIVO', t: 'Efectivo', ayuda: 'Pago en recepción' }
];

// Algoritmo de Luhn: verifica que el número de tarjeta tenga forma válida
function luhnValido(numero) {
  let suma = 0;
  let doble = false;
  for (let i = numero.length - 1; i >= 0; i--) {
    let d = Number(numero[i]);
    if (doble) { d *= 2; if (d > 9) d -= 9; }
    suma += d;
    doble = !doble;
  }
  return suma % 10 === 0;
}

// Vencimiento en formato MM/AA y que no esté vencido
function vencimientoValido(texto) {
  const m = /^(0[1-9]|1[0-2])\/(\d{2})$/.exec(texto);
  if (!m) return false;
  const inicioSiguienteMes = new Date(2000 + Number(m[2]), Number(m[1]), 1);
  return inicioSiguienteMes > new Date();
}

// Campos del metodo elegido
function panelPago(metodo, r) {
  const referencia = 'RES-' + String(r.id).padStart(6, '0');
  if (metodo === 'TARJETA') return `
    <div class="row g-3">
      <div class="col-12">
        <label class="form-label">Nombre en la tarjeta *</label>
        <input name="titularTarjeta" class="form-control" maxlength="60" autocomplete="cc-name" required>
      </div>
      <div class="col-12">
        <label class="form-label">Número de tarjeta *</label>
        <input name="numeroTarjeta" class="form-control" inputmode="numeric" autocomplete="cc-number" placeholder="0000 0000 0000 0000" required>
      </div>
      <div class="col-6">
        <label class="form-label">Vencimiento *</label>
        <input name="vencimiento" class="form-control" placeholder="MM/AA" maxlength="5" autocomplete="cc-exp" required>
      </div>
      <div class="col-6">
        <label class="form-label">CVV *</label>
        <input name="cvv" class="form-control" inputmode="numeric" maxlength="4" autocomplete="cc-csc" placeholder="123" required>
      </div>
    </div>
   `;

  if (metodo === 'TRANSFERENCIA') return `
    <div class="alert alert-light border small mb-3">
      <div><strong>Banco:</strong> Banco Simulado S.A.</div>
      <div><strong>Cuenta:</strong> 000-123-456-7</div>
      <div><strong>A nombre de:</strong> Hotelera Don Bosco</div>
      <div><strong>Referencia:</strong> ${referencia}</div>
    </div>
    <label class="form-label">Número de comprobante *</label>
    <input name="comprobante" class="form-control" maxlength="30" minlength="4" placeholder="Ej: 88421093" required>
    `;

  return `
    <div class="alert alert-light border small mb-0">
      Pague en recepción del hotel indicando la referencia <strong>${referencia}</strong>.
      El cobro queda registrado al confirmar.
    </div>`;
}

function etiquetaMetodo(v) {
  const m = METODOS_PAGO.find(x => x.v === v);
  return m ? m.t : v;
}

async function pasoPago(el, id) {
  UI.cargando(el);
  let r;
  try {
    r = await Api.get('/reservas/' + id);
  } catch (e) {
    UI.mostrarError(el, e);
    return;
  }

  // Si ya no hay saldo o la reserva no está activa, no tiene sentido pagar
  if (Number(r.saldo) <= 0 || !['PENDIENTE', 'CONFIRMADA'].includes(r.estado)) {
    ir('#/reservar/confirmacion/' + id);
    return;
  }
  const saldo = Number(r.saldo);

  el.innerHTML = `
    ${stepper('pago')}
    <div class="row g-4">
      <div class="col-lg-8">
        <div class="card"><div class="card-body p-4">

          <form id="formPago" novalidate>
            <div id="pasoMetodo">
              <h5 class="mb-3">Método de pago</h5>
              <div class="row g-2 mb-4">
                ${METODOS_PAGO.map((m, i) => `
                  <div class="col-12 col-md-4">
                    <label class="metodo-opcion w-100 ${i === 0 ? 'activo' : ''}">
                      <input type="radio" class="visually-hidden" name="metodo" value="${m.v}" ${i === 0 ? 'checked' : ''}>
                      <span class="fw-semibold d-block">${m.t}</span>
                      <span class="small text-muted">${m.ayuda}</span>
                    </label>
                  </div>`).join('')}
              </div>

              <div id="detallePago" class="mb-4"></div>

              <label class="form-label" for="montoPago">Monto a pagar *</label>
              <div class="input-group mb-1">
                <span class="input-group-text">$</span>
                <input id="montoPago" type="number" name="monto" class="form-control" min="0.01" step="0.01"
                       max="${saldo}" value="${saldo.toFixed(2)}" required>
              </div>
              <div class="form-text mb-4">
                Puede pagar el total o una parte. Saldo después de este pago: <strong id="saldoDespues"></strong>
              </div>

              <div id="formError"></div>
              <div class="d-flex justify-content-between align-items-center gap-2 flex-wrap">
                <a class="btn btn-outline-secondary" href="#/reserva/${r.id}">Ver reserva</a>
                <button class="btn btn-primary px-4" type="submit">Revisar pago</button>
              </div>
            </div>
          </form>

          <div id="pasoRevision" hidden></div>

        </div></div>
      </div>
      <aside class="col-lg-4">${resumenHtml(r)}</aside>
    </div>`;

  const form = document.getElementById('formPago');
  const pasoMetodo = document.getElementById('pasoMetodo');
  const pasoRevision = document.getElementById('pasoRevision');
  const detalle = document.getElementById('detallePago');
  const montoInput = document.getElementById('montoPago');
  const saldoDespues = document.getElementById('saldoDespues');
  const errorEl = document.getElementById('formError');

  // Resalta la opción elegida y muestra sus campos
  function pintarDetalle() {
    const metodo = form.metodo.value;
    el.querySelectorAll('.metodo-opcion').forEach(lbl => {
      lbl.classList.toggle('activo', lbl.querySelector('input').checked);
    });
    detalle.innerHTML = panelPago(metodo, r);

    // Formato mientras escribe
    const num = detalle.querySelector('[name=numeroTarjeta]');
    if (num) num.addEventListener('input', () => {
      num.value = num.value.replace(/\D/g, '').slice(0, 19).replace(/(\d{4})(?=\d)/g, '$1 ');
    });
    const vence = detalle.querySelector('[name=vencimiento]');
    if (vence) vence.addEventListener('input', () => {
      let v = vence.value.replace(/\D/g, '').slice(0, 4);
      if (v.length > 2) v = v.slice(0, 2) + '/' + v.slice(2);
      vence.value = v;
    });
    const cvv = detalle.querySelector('[name=cvv]');
    if (cvv) cvv.addEventListener('input', () => { cvv.value = cvv.value.replace(/\D/g, ''); });
  }

  function actualizarSaldo() {
    const monto = Number(montoInput.value) || 0;
    saldoDespues.textContent = UI.dinero(Math.max(Number((saldo - monto).toFixed(2)), 0));
  }

  // Revisa los campos del metodo elegido antes de mostrar el resumen
  function validarMetodo() {
    if (form.metodo.value === 'TARJETA') {
      const numero = form.numeroTarjeta.value.replace(/\s+/g, '');
      if (!/^\d{13,19}$/.test(numero) || !luhnValido(numero)) return 'El número de tarjeta no es válido.';
      if (!vencimientoValido(form.vencimiento.value)) return 'La fecha de vencimiento no es válida o la tarjeta ya venció (use MM/AA).';
      if (!/^\d{3,4}$/.test(form.cvv.value)) return 'El CVV debe tener 3 o 4 números.';
    }
    return null;
  }

  el.querySelectorAll('input[name=metodo]').forEach(inp => {
    inp.addEventListener('change', () => { UI.limpiarError(errorEl); pintarDetalle(); });
  });
  montoInput.addEventListener('input', actualizarSaldo);
  pintarDetalle();
  actualizarSaldo();

  // Paso 1 - revisión
  form.addEventListener('submit', (e) => {
    e.preventDefault();
    UI.limpiarError(errorEl);
    if (!form.reportValidity()) return;
    const problema = validarMetodo();
    if (problema) { UI.aviso(errorEl, problema, 'warning'); return; }

    const metodo = form.metodo.value;
    const monto = Number(montoInput.value);
    const restante = Number((saldo - monto).toFixed(2));

    pasoMetodo.hidden = true;
    pasoRevision.hidden = false;
    pasoRevision.innerHTML = `
      <h5 class="mb-3">Revise su pago</h5>
      <dl class="row small mb-4">
        <dt class="col-6">Método</dt><dd class="col-6 text-end">${UI.esc(etiquetaMetodo(metodo))}</dd>
        <dt class="col-6">Reserva</dt><dd class="col-6 text-end">#${UI.esc(r.id)}</dd>
        <dt class="col-6">Monto a pagar</dt><dd class="col-6 text-end fw-semibold">${UI.dinero(monto)}</dd>
        <dt class="col-6">Saldo después</dt><dd class="col-6 text-end">${UI.dinero(restante)}</dd>
      </dl>
      <div id="errorRevision"></div>
      <div class="d-flex justify-content-between align-items-center gap-2 flex-wrap">
        <button class="btn btn-outline-secondary" type="button" id="btnEditar">Volver a editar</button>
        <button class="btn btn-success px-4" type="button" id="btnConfirmar">Confirmar pago ${UI.dinero(monto)}</button>
      </div>`;

    const btnEditar = document.getElementById('btnEditar');
    const btnConfirmar = document.getElementById('btnConfirmar');
    const textoConfirmar = btnConfirmar.innerHTML;

    btnEditar.addEventListener('click', () => {
      pasoRevision.hidden = true;
      pasoMetodo.hidden = false;
    });

    // Pasa de Revisión a pago
    btnConfirmar.addEventListener('click', async () => {
      btnConfirmar.disabled = true;
      btnEditar.disabled = true;
      btnConfirmar.innerHTML = '<span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Procesando pago…';

      // Espera corta para simular la respuesta de la pasarela de pago
      await new Promise(resolver => setTimeout(resolver, 1200));

      try {
        await Api.post(`/reservas/${id}/pagos`, { metodo, monto });
        ir('#/reservar/confirmacion/' + id);
      } catch (err) {
        UI.mostrarError(document.getElementById('errorRevision'), err);
        btnConfirmar.innerHTML = textoConfirmar;
        btnConfirmar.disabled = false;
        btnEditar.disabled = false;
      }
    });
  });
}

// Paso 3: confirmación
async function pasoConfirmacion(el, id) {
  UI.cargando(el);
  let r;
  try {
    r = await Api.get('/reservas/' + id);
  } catch (e) {
    UI.mostrarError(el, e);
    return;
  }
  Seleccion.borrar();

  const confirmada = r.estado === 'CONFIRMADA';
  el.innerHTML = `
    ${stepper('confirmacion')}
    <div class="row justify-content-center">
      <div class="col-lg-7">
        <div class="card text-center"><div class="card-body p-4 p-md-5">
          <div class="check-circulo mb-3 ${confirmada ? '' : 'pendiente'}">${confirmada ? '✓' : '!'}</div>
          <h3 class="mb-1">${confirmada ? '¡Reserva confirmada!' : 'Reserva registrada'}</h3>
          <p class="text-muted mb-3">${confirmada
      ? 'Su pago cubre el total. Le esperamos.'
      : 'Aún hay saldo pendiente. Puede completar el pago desde sus reservas.'}</p>
          <div class="mb-4">${UI.badge(r.estado)} <span class="badge text-bg-light border">Reserva #${UI.esc(r.id)}</span></div>

          <dl class="row text-start small mb-4">
            <dt class="col-6">Hotel</dt><dd class="col-6 text-end">${UI.esc(r.hotelNombre)}</dd>
            <dt class="col-6">Habitación</dt><dd class="col-6 text-end">${UI.esc(r.numeroHabitacion)} · ${UI.esc(r.tipoHabitacion)}</dd>
            <dt class="col-6">Llegada</dt><dd class="col-6 text-end">${UI.esc(r.fechaEntrada)}</dd>
            <dt class="col-6">Salida</dt><dd class="col-6 text-end">${UI.esc(r.fechaSalida)}</dd>
            <dt class="col-6">Titular</dt><dd class="col-6 text-end">${UI.esc(r.titular ? r.titular.nombreCompleto : '—')}</dd>
            <dt class="col-6">Total pagado</dt><dd class="col-6 text-end fw-semibold">${UI.dinero(r.totalPagado)}</dd>
          </dl>

          <div class="d-flex gap-2 justify-content-center flex-wrap">
            <a class="btn btn-outline-secondary" href="#/reservas">Ver mis reservas</a>
            <a class="btn btn-primary" href="#/disponibilidad">Volver al inicio</a>
          </div>
        </div></div>
      </div>
    </div>`;
}

Views.reservar = async function (el, param) {
  const [paso, id] = (param || 'datos').split('/');
  if (paso === 'datos') return pasoDatos(el);
  if (paso === 'pago') return pasoPago(el, id);
  if (paso === 'confirmacion') return pasoConfirmacion(el, id);
  UI.aviso(el, 'Paso no válido.', 'warning');
};