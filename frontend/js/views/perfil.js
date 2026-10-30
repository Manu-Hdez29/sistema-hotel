// Cuenta: GET /auth/me (cualquier rol). Datos personales: GET /perfil (solo huéspedes).
const ROL_TEXTO = { ADMIN: 'Administrador', RECEPCIONISTA: 'Recepcionista', HUESPED: 'Huésped' };

Views.perfil = async function (el) {
    UI.cargando(el);
    let u;
    try {
        u = await Api.get('/auth/me');
    } catch (e) {
        UI.mostrarError(el, e);
        return;
    }

    const iniciales = (u.nombreCompleto || '?')
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map(p => p[0].toUpperCase())
        .join('');
    const rol = ROL_TEXTO[u.rol] || u.rol;

    el.innerHTML = `
    <div class="row justify-content-center">
      <div class="col-lg-7">
        <h4 class="mb-3">Mi perfil</h4>
        <div class="card mb-4"><div class="card-body p-4">
          <div class="d-flex align-items-center gap-3 mb-4">
            <div style="width:64px;height:64px;border-radius:50%;background:var(--navy);color:#fff;display:flex;align-items:center;justify-content:center;font-size:1.4rem;font-weight:600;flex-shrink:0;">${UI.esc(iniciales)}</div>
            <div>
              <div class="fs-5 fw-semibold">${UI.esc(u.nombreCompleto)}</div>
              <div class="small text-muted">${UI.esc(rol)}</div>
            </div>
          </div>
          <dl class="row mb-0">
            <dt class="col-sm-4 text-muted small">Correo electrónico</dt>
            <dd class="col-sm-8">${UI.esc(u.email)}</dd>
            <dt class="col-sm-4 text-muted small">Número de cuenta</dt>
            <dd class="col-sm-8">#${UI.esc(u.id)}</dd>
          </dl>
        </div></div>
        <div id="datosPersonales"></div>
        <p class="small text-muted mt-3">Para cambiar sus datos de contacto, comuníquese con recepción.</p>
      </div>
    </div>`;

    // Datos personales del huésped (DUI, teléfono, nacimiento). RECEPCIONISTA Y ADMIN sin perfil de huésped recibe
    // 404 y no se muestra.
    try {
        const h = await Api.get('/perfil');
        const destino = document.getElementById('datosPersonales');
        if (!destino) return;
        destino.innerHTML = `
      <div class="card"><div class="card-body p-4">
        <h5 class="mb-3">Datos personales</h5>
        <dl class="row mb-0">
          <dt class="col-sm-4 text-muted small">Nombre completo</dt>
          <dd class="col-sm-8">${UI.esc(h.nombreCompleto)}</dd>
          <dt class="col-sm-4 text-muted small">DUI o pasaporte</dt>
          <dd class="col-sm-8">${UI.esc(h.documentoIdentidad)}</dd>
          <dt class="col-sm-4 text-muted small">Fecha de nacimiento</dt>
          <dd class="col-sm-8">${UI.esc(h.fechaNacimiento)}</dd>
          <dt class="col-sm-4 text-muted small">Teléfono</dt>
          <dd class="col-sm-8">${UI.esc(h.telefono)}</dd>
        </dl>
      </div></div>`;
    } catch (e) {
        // Sin perfil de huésped: no se muestra la sección
    }
};