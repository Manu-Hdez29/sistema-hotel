// Menú principal (se filtra por rol)
const MENU = [
  { ruta: '#/disponibilidad', texto: 'Buscar y reservar' },
  { ruta: '#/reservas', texto: 'Reservas' },
  { ruta: '#/reclamos', texto: 'Reclamos' },
  { ruta: '#/catalogo', texto: 'Catálogos' },
  { ruta: '#/huespedes', texto: 'Huéspedes', roles: ['ADMIN', 'RECEPCIONISTA'] },
  { ruta: '#/reportes', texto: 'Estadísticas', roles: ['ADMIN', 'RECEPCIONISTA'] },
  { ruta: '#/usuarios', texto: 'Usuarios', roles: ['ADMIN'] }
];

// Rutas
// publica: se ve sin sesión. roles: si se omite, cualquier usuario autenticado.
const RUTAS = {
  login: { publica: true, vista: el => Views.login(el) },
  registro: { publica: true, vista: el => Views.registro(el) },
  disponibilidad: { vista: el => Views.disponibilidad(el) },
  reservas: { vista: el => Views.reservas(el) },
  reserva: { vista: (el, id) => Views.reservaDetalle(el, id) },
  reclamos: { vista: el => Views.reclamos(el) },
  reservar: { vista: (el, p) => Views.reservar(el, p) },
  catalogo: { vista: (el, clave) => Views.catalogo(el, clave || 'hoteles') },
  huespedes: { roles: ['ADMIN', 'RECEPCIONISTA'], vista: el => Views.catalogo(el, 'huespedes') },
  usuarios: { roles: ['ADMIN'], vista: el => Views.catalogo(el, 'usuarios') },
  reportes: { roles: ['ADMIN', 'RECEPCIONISTA'], vista: el => Views.reportes(el) },
  perfil: { vista: el => Views.perfil(el) }
};

function parseRuta() {
  const h = location.hash.replace(/^#\/?/, '');
  const partes = h.split('/');
  return { ruta: partes[0] || '', param: partes.slice(1).join('/') || null };
}

// Cambia el hash; si ya es el mismo, vuelve a pintar la vista
function ir(hash) {
  if (location.hash === hash) route();
  else location.hash = hash;
}

function renderNav() {
  const nav = document.getElementById('nav');
  const sesion = Session.get();
  if (!sesion) {
    nav.hidden = true;
    nav.innerHTML = '';
    return;
  }
  const rol = sesion.usuario.rol;
  const items = MENU
      .filter(m => !m.roles || m.roles.includes(rol))
      .map(m => {
        const activo = location.hash.startsWith(m.ruta) ? ' active' : '';
        return `<li class="nav-item"><a class="nav-link${activo}" href="${m.ruta}">${m.texto}</a></li>`;
      }).join('');

  nav.hidden = false;
  nav.innerHTML = `
    <div class="app-nav">
      <a class="navbar-brand" href="#/disponibilidad" aria-label="Hotelera Don Bosco, ir al inicio">
        <img class="marca-iso" src="assets/logo/isologo-invertido.png" alt="">
        <span class="marca-texto">Hotelera Don Bosco</span>
      </a>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#menuPrincipal" aria-label="Menú">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="menuPrincipal">
        <ul class="navbar-nav me-auto">${items}</ul>
        <div class="nav-usuario d-flex align-items-center gap-3">
          <div class="text-end">
            <a class="nombre d-block text-white text-decoration-none" href="#/perfil" title="Ver mi perfil">${UI.esc(sesion.usuario.nombreCompleto)}</a>
            <div class="rol">${UI.esc(rol)}</div>
          </div>
          <button class="btn btn-outline-light btn-sm" id="btnSalir" type="button">Salir</button>
        </div>
      </div>
    </div>`;

  document.getElementById('btnSalir').onclick = () => {
    Session.clear();
    renderNav();
    ir('#/login');
  };
}

async function route() {
  const { ruta, param } = parseRuta();
  const sesion = Session.get();
  const def = RUTAS[ruta];

  if (!sesion && !(def && def.publica)) {
    ir('#/login');
    return;
  }
  if (sesion && (!ruta || ruta === 'login' || ruta === 'registro')) {
    ir('#/disponibilidad');
    return;
  }

  renderNav();
  const app = document.getElementById('app');

  if (!def) {
    UI.aviso(app, 'La página que busca no existe.', 'warning');
    return;
  }
  if (def.roles && !def.roles.includes(Session.rol())) {
    UI.aviso(app, 'Su rol no tiene acceso a esta pantalla.', 'warning');
    return;
  }

  app.innerHTML = '';
  try {
    await def.vista(app, param);
  } catch (e) {
    UI.mostrarError(app, e);
  }
}

// Acceso (login y registro)
function marcoAcceso(contenido, extraClase = '') {
  return `
    <div class="acceso-pagina">
      <section class="acceso ${extraClase}">
        <aside class="acceso-marca">
          <img class="acceso-iso" src="assets/logo/isologo-invertido.png" alt="">
          <h2 class="acceso-nombre">Hotelera Don Bosco</h2>
          <p class="acceso-eslogan">Su hogar lejos de casa</p>
          <p class="acceso-pie">Reserve en línea y dé seguimiento a sus reservas desde un solo lugar.</p>
        </aside>
        <div class="acceso-form">${contenido}</div>
      </section>
    </div>`;
}

// Login
Views.login = function (el) {
  el.innerHTML = marcoAcceso(`
    <div class="acceso-cabecera">
      <h3 class="mb-1">Iniciar sesión</h3>
      <p class="text-muted small mb-0">Sistema de Administración y Reserva de Habitaciones</p>
    </div>
    <form id="formLogin" novalidate>
      <div class="mb-3">
        <label class="form-label" for="loginEmail">Correo electrónico</label>
        <input class="form-control form-control-lg" id="loginEmail" name="email" type="email" maxlength="100" required>
      </div>
      <div class="mb-3">
        <label class="form-label" for="loginPass">Contraseña</label>
        <input class="form-control form-control-lg" id="loginPass" name="password" type="password" required>
      </div>
      <div id="loginError"></div>
      <button class="btn btn-primary btn-lg w-100 mt-2" type="submit">Entrar</button>
    </form>
    <p class="small mt-4 mb-0 text-center">¿No tiene cuenta? <a href="#/registro">Regístrese aquí</a></p>`);

  const form = document.getElementById('formLogin');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!form.reportValidity()) return;

    const errorEl = document.getElementById('loginError');
    const btn = form.querySelector('button');
    UI.limpiarError(errorEl);
    btn.disabled = true;
    try {
      const datos = await Api.post('/auth/login', {
        email: form.email.value.trim(),
        password: form.password.value
      }, { auth: false });

      Session.set({ token: datos.token, expiracion: datos.expiracion, usuario: datos.usuario });
      UI.toast('Bienvenido, ' + datos.usuario.nombreCompleto + '.');
      ir('#/disponibilidad');
    } catch (err) {
      UI.mostrarError(errorEl, err);
    } finally {
      btn.disabled = false;
    }
  });
};

// Registro de huésped
Views.registro = function (el) {
  el.innerHTML = marcoAcceso(`
    <div class="acceso-cabecera">
      <h3 class="mb-1">Crear cuenta de huésped</h3>
      <p class="text-muted small mb-0">Con su cuenta podrá buscar habitaciones, reservar y dar seguimiento a sus reservas.</p>
    </div>
    <form id="formRegistro" novalidate>
      <div class="row g-3">
        <div class="col-12">
          <label class="form-label" for="rNombre">Nombre completo *</label>
          <input class="form-control" id="rNombre" name="nombreCompleto" maxlength="100" required>
        </div>
        <div class="col-12">
          <label class="form-label" for="rEmail">Correo electrónico *</label>
          <input class="form-control" id="rEmail" name="email" type="email" maxlength="100" required>
        </div>
        <div class="col-12">
          <label class="form-label" for="rPass">Contraseña *</label>
          <input class="form-control" id="rPass" name="password" type="password" required>
        </div>
        <div class="col-sm-6">
          <label class="form-label" for="rFecha">Fecha de nacimiento *</label>
          <input class="form-control" id="rFecha" name="fechaNacimiento" type="date" required>
        </div>
        <div class="col-sm-6">
          <label class="form-label" for="rDoc">DUI o pasaporte *</label>
          <input class="form-control" id="rDoc" name="documentoIdentidad" required
                 pattern="(\\d{8}-\\d|[A-Za-z0-9]{6,20})"
                 title="DUI ########-# o pasaporte de 6 a 20 caracteres alfanuméricos">
          <div class="form-text">DUI ########-# o pasaporte de 6 a 20 caracteres.</div>
        </div>
        <div class="col-12">
          <label class="form-label" for="rTel">Teléfono *</label>
          <input class="form-control" id="rTel" name="telefono" required pattern="\\d{4}-\\d{4}" title="Formato ####-####" placeholder="7777-8888">
        </div>
      </div>
      <div id="registroError" class="mt-3"></div>
      <button class="btn btn-primary btn-lg w-100 mt-4" type="submit">Crear cuenta</button>
    </form>
    <p class="small mt-4 mb-0 text-center">¿Ya tiene cuenta? <a href="#/login">Inicie sesión</a></p>`, 'acceso-ancho');

  const form = document.getElementById('formRegistro');
  document.getElementById('rFecha').max = UI.fechaHoy();

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!form.reportValidity()) return;

    const errorEl = document.getElementById('registroError');
    UI.limpiarError(errorEl);
    const body = {
      nombreCompleto: form.nombreCompleto.value.trim(),
      email: form.email.value.trim(),
      password: form.password.value,
      fechaNacimiento: form.fechaNacimiento.value,
      documentoIdentidad: form.documentoIdentidad.value.trim(),
      telefono: form.telefono.value.trim()
    };
    try {
      const datos = await Api.post('/auth/registro', body, { auth: false });
      UI.toast(datos.mensaje || 'Cuenta creada. Ya puede iniciar sesión.');
      ir('#/login');
    } catch (err) {
      UI.mostrarError(errorEl, err);
    }
  });
};

// Arranque
window.addEventListener('hashchange', route);

(async function iniciar() {
  const sesion = Session.get();
  if (sesion) {
    // Confirma que el token siga vigente y refresca los datos del usuario
    try {
      const usuario = await Api.get('/auth/me');
      Session.set({ ...sesion, usuario: { ...sesion.usuario, ...usuario } });
    } catch (e) {
      // Si fue 401, Api ya cerró la sesión y mandó al login
    }
  }
  route();
})();