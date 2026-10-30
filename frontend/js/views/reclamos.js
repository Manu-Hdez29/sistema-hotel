// Reclamos: el huésped crea y ve los suyos; los demás ven todos y los atiende
Views.reclamos = async function (el) {
  const rol = Session.rol();
  const esHuesped = rol === 'HUESPED';
  const esStaff = ['ADMIN', 'RECEPCIONISTA'].includes(rol);

  el.innerHTML = `
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
      <h3 class="mb-0">Reclamos</h3>
      <div class="d-flex gap-2 flex-wrap">
        <select id="fEstado" class="form-select form-select-sm" style="width:auto">
          <option value="">Todos los estados</option>
          ${ENUMS.estadoReclamo.map(e => `<option value="${e}">${e}</option>`).join('')}
        </select>
        ${esHuesped ? '<button class="btn btn-primary btn-sm" id="btnNuevo" type="button">+ Nuevo reclamo</button>' : ''}
      </div>
    </div>
    <div class="card shadow-sm"><div class="card-body p-0"><div class="tabla-scroll" id="lista"></div></div></div>`;

  const lista = document.getElementById('lista');
  const filtro = document.getElementById('fEstado');
  let filas = [];

  async function cargar() {
    UI.cargando(lista);
    try {
      filas = await Api.get('/reclamos', { estado: filtro.value });
      pintar();
    } catch (e) {
      UI.mostrarError(lista, e);
    }
  }

  function pintar() {
    const cols = esStaff ? 8 : 7;
    const cuerpo = filas.length
      ? filas.map((r, i) => `
          <tr>
            <td>#${UI.esc(r.id)}</td>
            <td class="small">${UI.esc(r.fecha)}</td>
            <td>${UI.esc(r.descripcion)}</td>
            <td>${r.reservaId ? '#' + UI.esc(r.reservaId) : '—'}</td>
            <td>${UI.badge(r.estado)}</td>
            <td class="small">${UI.esc(r.respuesta || '—')}</td>
            ${esStaff ? `<td class="text-end"><button class="btn btn-sm btn-outline-primary" data-atender="${i}">Atender</button></td>` : ''}
          </tr>`).join('')
      : `<tr><td colspan="${cols}" class="text-center text-muted py-4">No hay reclamos registrados.</td></tr>`;

    lista.innerHTML = `
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light">
          <tr><th>ID</th><th>Fecha</th><th>Descripción</th><th>Reserva</th><th>Estado</th><th>Respuesta</th>
              ${esStaff ? '<th></th>' : ''}</tr>
        </thead>
        <tbody>${cuerpo}</tbody>
      </table>`;

    lista.querySelectorAll('[data-atender]').forEach(btn => {
      btn.addEventListener('click', () => abrirAtender(filas[Number(btn.dataset.atender)]));
    });
  }

  filtro.addEventListener('change', cargar);

  if (esHuesped) {
    document.getElementById('btnNuevo').addEventListener('click', abrirNuevo);
  }

  // Para crear un reclamo la API necesita el id del huésped.
  // Se toma del titular de sus reservas, porque ahí queda ligado a su cuenta.
  async function abrirNuevo() {
    let reservas = [];
    try {
      reservas = await Api.get('/reservas');
    } catch (e) {
      UI.toast(e.message, 'danger');
      return;
    }
    const titular = reservas.find(r => r.titular && r.titular.id);
    if (!titular) {
      UI.toast('Primero necesita una reserva a su nombre para registrar un reclamo.', 'warning');
      return;
    }

    const opciones = UI.opciones(
      reservas.map(r => ({ v: r.id, t: `#${r.id} · hab. ${r.numeroHabitacion} · ${r.estado}` })),
      '', { vacia: 'Ninguna' }
    );

    UI.abrirModal('Nuevo reclamo', `
      <form id="formReclamo" novalidate>
        <div class="mb-3">
          <label class="form-label small">Reserva relacionada</label>
          <select name="reservaId" class="form-select">${opciones}</select>
        </div>
        <div class="mb-3">
          <label class="form-label small">Descripción *</label>
          <textarea name="descripcion" class="form-control" rows="4" maxlength="250" required></textarea>
        </div>
        <div id="formError"></div>
        <div class="text-end"><button class="btn btn-primary" type="submit">Enviar reclamo</button></div>
      </form>`);

    const form = document.getElementById('formReclamo');
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!form.reportValidity()) return;
      const errorEl = document.getElementById('formError');
      UI.limpiarError(errorEl);
      try {
        await Api.post('/reclamos', {
          huespedId: titular.titular.id,
          reservaId: form.reservaId.value ? Number(form.reservaId.value) : null,
          descripcion: form.descripcion.value.trim()
        });
        UI.cerrarModal();
        UI.toast('Reclamo enviado. El equipo lo revisará pronto.');
        cargar();
      } catch (err) {
        UI.mostrarError(errorEl, err);
      }
    });
  }

  // Solo RECEPCIONISTA Y ADMI: pueden cambiar el estado y escribe la respuesta
  function abrirAtender(r) {
    UI.abrirModal(`Atender reclamo #${r.id}`, `
      <p class="small mb-3">${UI.esc(r.descripcion)}</p>
      <form id="formAtender" novalidate>
        <div class="mb-3">
          <label class="form-label small">Estado *</label>
          <select name="estado" class="form-select" required>${UI.opciones(UI.enumAOpciones(ENUMS.estadoReclamo), r.estado)}</select>
        </div>
        <div class="mb-3">
          <label class="form-label small">Respuesta</label>
          <textarea name="respuesta" class="form-control" rows="3" maxlength="250">${UI.esc(r.respuesta || '')}</textarea>
        </div>
        <div id="formError"></div>
        <div class="text-end"><button class="btn btn-primary" type="submit">Guardar</button></div>
      </form>`);

    const form = document.getElementById('formAtender');
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!form.reportValidity()) return;
      const errorEl = document.getElementById('formError');
      UI.limpiarError(errorEl);
      try {
        await Api.patch(`/reclamos/${r.id}/atender`, {
          estado: form.estado.value,
          respuesta: form.respuesta.value.trim() || null
        });
        UI.cerrarModal();
        UI.toast('Reclamo actualizado.');
        cargar();
      } catch (err) {
        UI.mostrarError(errorEl, err);
      }
    });
  }

  cargar();
};
