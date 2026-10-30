// Portada, búsqueda de habitaciones y selección.
// Foto de cada tipo de habitación (carpeta assets/habitaciones/)
const FOTO_TIPO = { INDIVIDUAL: 'individual.jpg', DOBLE: 'doble.jpg', SUITE: 'suite.jpg', FAMILIAR: 'family.jpg' };
// La selección se guarda en sessionStorage y el flujo de reserva sigue en reservar.js.
Views.disponibilidad = async function (el) {
  el.innerHTML = `
    <section class="hero">
      <div class="container">
        <p class="hero-kicker">Reserve en línea · Hotelera Don Bosco</p>
        <h1 class="hero-titulo">Encuentre su habitación ideal</h1>
        <p class="hero-texto">Elija fechas, huéspedes y tipo de habitación. Después confirmamos su reserva en tres pasos.</p>
      </div>
    </section>

    <div class="card buscador shadow-sm mb-4">
      <div class="card-body p-3 p-md-4">
        <form id="formBusqueda" class="row g-3 align-items-end" novalidate>
          <div class="col-6 col-lg-3">
            <label class="form-label" for="bEntrada">Llegada *</label>
            <input type="date" id="bEntrada" name="fechaEntrada" class="form-control" required>
          </div>
          <div class="col-6 col-lg-3">
            <label class="form-label" for="bSalida">Salida *</label>
            <input type="date" id="bSalida" name="fechaSalida" class="form-control" required>
          </div>
          <div class="col-6 col-lg-2">
            <label class="form-label" for="bHuespedes">Huéspedes *</label>
            <input type="number" id="bHuespedes" name="huespedes" class="form-control" min="1" max="10" value="1" required>
          </div>
          <div class="col-6 col-lg-2">
            <label class="form-label" for="bTipo">Tipo</label>
            <select id="bTipo" name="tipoHabitacionId" class="form-select"><option value="">Todos</option></select>
          </div>
          <div class="col-12 col-lg-2">
            <label class="form-label" for="bCiudad">Ciudad</label>
            <input type="text" id="bCiudad" name="ciudad" class="form-control" maxlength="60" placeholder="Opcional">
          </div>
          <div class="col-12 text-end">
            <button class="btn btn-primary px-4 py-2" type="submit">Buscar habitaciones</button>
          </div>
        </form>
      </div>
    </div>

    <div id="resultados" class="mb-5"></div>`;

  const form = document.getElementById('formBusqueda');
  const hoy = UI.fechaHoy();
  form.fechaEntrada.min = hoy;
  form.fechaSalida.min = hoy;
  form.fechaEntrada.addEventListener('change', () => {
    const entrada = form.fechaEntrada.value;
    if (entrada) {
      const siguiente = new Date(entrada + 'T00:00:00');
      siguiente.setDate(siguiente.getDate() + 1);
      form.fechaSalida.min = UI.fechaDe(siguiente);
    }
  });

  try {
    const tipos = await Api.get('/tipos-habitacion');
    form.tipoHabitacionId.innerHTML = UI.opciones(
        tipos.map(t => ({ v: t.id, t: t.nombre })), '', { vacia: 'Todos' }
    );
  } catch (e) {  }

  const resultados = document.getElementById('resultados');
  let encontradas = [];

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!form.reportValidity()) return;

    const entrada = form.fechaEntrada.value;
    const salida = form.fechaSalida.value;
    if (entrada < hoy) {
      UI.aviso(resultados, 'La fecha de llegada no puede ser anterior a hoy.', 'warning');
      return;
    }
    if (salida <= entrada) {
      UI.aviso(resultados, 'La fecha de salida debe ser posterior a la de llegada.', 'warning');
      return;
    }

    const busqueda = {
      fechaEntrada: entrada,
      fechaSalida: salida,
      huespedes: Number(form.huespedes.value),
      tipoHabitacionId: form.tipoHabitacionId.value,
      ciudad: form.ciudad.value.trim()
    };

    UI.cargando(resultados);
    try {
      encontradas = await Api.get('/disponibilidad', busqueda);
      pintarResultados(busqueda);
    } catch (err) {
      UI.mostrarError(resultados, err);
    }
  });

  function pintarResultados(b) {
    const noches = UI.noches(b.fechaEntrada, b.fechaSalida);
    const resumen = `
      <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
        <h4 class="mb-0">${encontradas.length} habitación(es) disponible(s)</h4>
        <span class="text-muted small">${b.fechaEntrada} → ${b.fechaSalida} · ${noches} noche(s) · ${b.huespedes} huésped(es)</span>
      </div>`;

    if (!encontradas.length) {
      resultados.innerHTML = resumen + `
        <div class="card"><div class="card-body text-center py-5">
          <h5>No hay habitaciones para esas fechas</h5>
          <p class="text-muted mb-0">Pruebe otras fechas, menos huéspedes u otro tipo de habitación.</p>
        </div></div>`;
      return;
    }

    const tarjetas = encontradas.map((h, i) => `
      <div class="col-12 col-md-6 col-xl-4">
        <article class="card h-100 hab-card">
          <div class="hab-img" style="height:170px;overflow:hidden;position:relative;">
            <img src="assets/habitaciones/${FOTO_TIPO[h.tipoHabitacion] || 'doble.jpg'}" alt="Habitación ${UI.esc(h.tipoHabitacion)}" loading="lazy" style="display:block;width:100%;height:100%;object-fit:cover;">
          </div>
          <div class="card-body d-flex flex-column">
            <div class="d-flex justify-content-between align-items-start gap-2">
              <div class="small text-muted">${UI.esc(h.hotelNombre)} · ${UI.esc(h.ciudad)}</div>
              <span class="badge text-bg-success">Disponible</span>
            </div>
            <h5 class="mt-1 mb-1">Habitación ${UI.esc(h.numeroHabitacion)} · ${UI.esc(h.tipoHabitacion)}</h5>
            <div class="small text-muted">Hasta ${UI.esc(h.capacidadMaxima)} persona(s) · ${UI.esc(h.sucursalNombre)}</div>
            <div class="mt-auto pt-3 d-flex justify-content-between align-items-end gap-2">
              <div>
                <div class="small text-muted">Total ${noches} noche(s)</div>
                <div class="fs-4 fw-semibold">${UI.dinero(h.precioTotal)}</div>
                <div class="small text-muted">${UI.dinero(h.precioNoche)} por noche</div>
              </div>
              <button class="btn btn-primary" type="button" data-seleccionar="${i}">Seleccionar</button>
            </div>
          </div>
        </article>
      </div>`).join('');

    resultados.innerHTML = resumen + `<div class="row g-3">${tarjetas}</div>`;

    resultados.querySelectorAll('[data-seleccionar]').forEach(btn => {
      btn.addEventListener('click', () => {
        const hab = encontradas[Number(btn.dataset.seleccionar)];
        Seleccion.guardar({ hab, busqueda: b });
        ir('#/reservar/datos');
      });
    });
  }
};