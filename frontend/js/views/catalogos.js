// CRUD para catálogos, huéspedes y usuarios.
// Cada entidad describe su ruta, quién puede leer/escribir, sus columnas y sus campos.
const TODOS = ['ADMIN', 'RECEPCIONISTA', 'HUESPED'];
const STAFF = ['ADMIN', 'RECEPCIONISTA'];
const SOLO_ADMIN = ['ADMIN'];

const ENTIDADES = {
  hoteles: {
    titulo: 'Hoteles', ruta: '/hoteles', leer: TODOS, escribir: SOLO_ADMIN,
    columnas: [
      { k: 'id', t: 'ID' }, { k: 'nombre', t: 'Nombre' },
      { k: 'nitRuc', t: 'NIT / RUC' }, { k: 'estado', t: 'Estado', badge: true }
    ],
    campos: [
      { k: 'nombre', t: 'Nombre', req: true, maxLen: 100 },
      { k: 'nitRuc', t: 'NIT / RUC', maxLen: 20 },
      { k: 'estado', t: 'Estado', tipo: 'select', opciones: ENUMS.estadoGeneral, req: true, defecto: 'ACTIVO' }
    ]
  },

  sucursales: {
    titulo: 'Sucursales', ruta: '/sucursales', leer: TODOS, escribir: SOLO_ADMIN,
    columnas: [
      { k: 'id', t: 'ID' }, { k: 'hotelNombre', t: 'Hotel' }, { k: 'nombre', t: 'Nombre' },
      { k: 'ciudad', t: 'Ciudad' }, { k: 'telefono', t: 'Teléfono' }
    ],
    campos: [
      { k: 'hotelId', t: 'Hotel', tipo: 'select', origen: '/hoteles', valor: 'id', etiqueta: 'nombre', req: true },
      { k: 'nombre', t: 'Nombre', req: true, maxLen: 100 },
      { k: 'ciudad', t: 'Ciudad', req: true, maxLen: 60 },
      { k: 'direccion', t: 'Dirección', req: true, maxLen: 150 },
      { k: 'telefono', t: 'Teléfono', maxLen: 15 }
    ]
  },

  tipos: {
    titulo: 'Tipos de habitación', ruta: '/tipos-habitacion', leer: TODOS, escribir: SOLO_ADMIN,
    columnas: [
      { k: 'id', t: 'ID' }, { k: 'nombre', t: 'Tipo' },
      { k: 'capacidadMaxima', t: 'Capacidad' }, { k: 'descripcion', t: 'Descripción' }
    ],
    campos: [
      { k: 'nombre', t: 'Tipo', tipo: 'select', opciones: ENUMS.tipoHabitacion, req: true },
      { k: 'capacidadMaxima', t: 'Capacidad máxima', tipo: 'int', req: true, min: 1, max: 10 },
      { k: 'descripcion', t: 'Descripción', maxLen: 200 }
    ]
  },

  habitaciones: {
    titulo: 'Habitaciones', ruta: '/habitaciones', leer: TODOS, escribir: SOLO_ADMIN,
    columnas: [
      { k: 'id', t: 'ID' }, { k: 'sucursalNombre', t: 'Sucursal' }, { k: 'tipoHabitacion', t: 'Tipo' },
      { k: 'numeroHabitacion', t: 'Número' }, { k: 'estado', t: 'Estado', badge: true }
    ],
    campos: [
      { k: 'sucursalId', t: 'Sucursal', tipo: 'select', origen: '/sucursales', valor: 'id', etiqueta: 'nombre', req: true },
      { k: 'tipoHabitacionId', t: 'Tipo', tipo: 'select', origen: '/tipos-habitacion', valor: 'id', etiqueta: 'nombre', req: true },
      { k: 'numeroHabitacion', t: 'Número de habitación', req: true, maxLen: 10 },
      { k: 'estado', t: 'Estado', tipo: 'select', opciones: ENUMS.estadoHabitacion, req: true, defecto: 'DISPONIBLE' }
    ]
  },

  tarifas: {
    titulo: 'Tarifas', ruta: '/tarifas', leer: TODOS, escribir: SOLO_ADMIN,
    columnas: [
      { k: 'id', t: 'ID' }, { k: 'sucursalId', t: 'Sucursal (ID)' }, { k: 'tipoHabitacion', t: 'Tipo' },
      { k: 'precioNoche', t: 'Precio/noche', dinero: true },
      { k: 'fechaInicioVigencia', t: 'Desde' }, { k: 'fechaFinVigencia', t: 'Hasta' }
    ],
    campos: [
      { k: 'sucursalId', t: 'Sucursal', tipo: 'select', origen: '/sucursales', valor: 'id', etiqueta: 'nombre', req: true },
      { k: 'tipoHabitacionId', t: 'Tipo', tipo: 'select', origen: '/tipos-habitacion', valor: 'id', etiqueta: 'nombre', req: true },
      { k: 'precioNoche', t: 'Precio por noche ($)', tipo: 'number', paso: '0.01', min: 0.01, req: true },
      { k: 'fechaInicioVigencia', t: 'Inicio de vigencia', tipo: 'date', req: true },
      { k: 'fechaFinVigencia', t: 'Fin de vigencia (opcional)', tipo: 'date' }
    ]
  },

  servicios: {
    titulo: 'Servicios adicionales', ruta: '/servicios', leer: TODOS, escribir: SOLO_ADMIN,
    columnas: [
      { k: 'id', t: 'ID' }, { k: 'nombre', t: 'Nombre' },
      { k: 'precio', t: 'Precio', dinero: true }, { k: 'tipoServicio', t: 'Tipo' }
    ],
    campos: [
      { k: 'nombre', t: 'Nombre', req: true, maxLen: 60 },
      { k: 'precio', t: 'Precio ($)', tipo: 'number', paso: '0.01', min: 0, req: true },
      { k: 'tipoServicio', t: 'Tipo de servicio', tipo: 'select', opciones: ENUMS.tipoServicio, req: true }
    ]
  },

  politicas: {
    titulo: 'Políticas de cancelación', ruta: '/politicas-cancelacion', leer: TODOS, escribir: SOLO_ADMIN,
    columnas: [
      { k: 'id', t: 'ID' }, { k: 'hotelNombre', t: 'Hotel' }, { k: 'diasPrevios', t: 'Días previos' },
      { k: 'porcentajeCargo', t: 'Cargo', porcentaje: true }, { k: 'descripcion', t: 'Descripción' }
    ],
    campos: [
      { k: 'hotelId', t: 'Hotel', tipo: 'select', origen: '/hoteles', valor: 'id', etiqueta: 'nombre', req: true },
      { k: 'diasPrevios', t: 'Días de anticipación', tipo: 'int', min: 0, req: true },
      { k: 'porcentajeCargo', t: 'Porcentaje de cargo (%)', tipo: 'number', paso: '0.01', min: 0, max: 100, req: true },
      { k: 'descripcion', t: 'Descripción', maxLen: 200 }
    ]
  },

  huespedes: {
    titulo: 'Huéspedes', ruta: '/huespedes', leer: STAFF, escribir: STAFF,
    descripcion: 'Perfiles de huéspedes: datos personales y contacto. Para dar o quitar acceso al sistema, use Cuentas de acceso. Al eliminar un huésped, su cuenta también se desactiva.',
    avisoBorrar: 'Al eliminar este huésped, su cuenta de acceso también se desactivará. ¿Continuar?',
    columnas: [
      { k: 'id', t: 'ID' }, { k: 'nombreCompleto', t: 'Nombre' },
      { k: 'documentoIdentidad', t: 'Documento' }, { k: 'telefono', t: 'Teléfono' },
      { k: 'email', t: 'Correo' }, { k: 'fechaNacimiento', t: 'Nacimiento' }
    ],
    campos: [
      { k: 'nombreCompleto', t: 'Nombre completo', req: true, maxLen: 100 },
      { k: 'fechaNacimiento', t: 'Fecha de nacimiento', tipo: 'date', req: true, max: UI.fechaHoy() },
      { k: 'documentoIdentidad', t: 'DUI o pasaporte', req: true, maxLen: 20,
        pattern: '(\\d{8}-\\d|[A-Za-z0-9]{6,20})', ayuda: 'DUI ########-# o pasaporte de 6 a 20 caracteres' },
      { k: 'telefono', t: 'Teléfono', req: true, pattern: '\\d{4}-\\d{4}', ayuda: 'Formato ####-####' },
      { k: 'email', t: 'Correo electrónico', tipo: 'email', req: true, maxLen: 100 }
    ]
  },

  usuarios: {
    titulo: 'Cuentas de acceso', ruta: '/usuarios', leer: SOLO_ADMIN, escribir: SOLO_ADMIN, sinBorrar: true,
    descripcion: 'Cuentas para iniciar sesión (administradores, recepcionistas y huéspedes). Para quitar acceso use Desactivar; las cuentas no se eliminan para conservar el historial.',
    columnas: [
      { k: 'id', t: 'ID' }, { k: 'nombreCompleto', t: 'Nombre' }, { k: 'email', t: 'Correo' },
      { k: 'rol', t: 'Rol', badge: false }, { k: 'estado', t: 'Estado', badge: true }
    ],
    campos: [
      { k: 'nombreCompleto', t: 'Nombre completo', req: true, maxLen: 100 },
      { k: 'email', t: 'Correo electrónico', tipo: 'email', req: true, maxLen: 100 },
      { k: 'rol', t: 'Rol', tipo: 'select', opciones: ENUMS.rol, req: true },
      { k: 'password', t: 'Contraseña', tipo: 'password', reqCrear: true,
        ayuda: 'Al editar, déjela vacía para no cambiarla.' }
    ],
    acciones: [
      {
        t: f => f.estado === 'ACTIVO' ? 'Desactivar' : 'Activar',
        clase: f => f.estado === 'ACTIVO' ? 'btn-outline-warning' : 'btn-outline-success',
        fn: f => Api.patch(`/usuarios/${f.id}/${f.estado === 'ACTIVO' ? 'desactivar' : 'activar'}`)
      }
    ]
  }
};

// Lee las opciones de un select: enum fijo o lista traída de la API
async function opcionesCampo(c) {
  if (c.opciones) return UI.enumAOpciones(c.opciones);
  const lista = await Api.get(c.origen);
  return lista.map(r => ({ v: r[c.valor || 'id'], t: r[c.etiqueta] }));
}

// HTML de un campo del formulario
async function campoHtml(c, valores, esNuevo) {
  const req = c.req || (c.reqCrear && esNuevo);
  const id = 'f_' + c.k;
  let val = valores[c.k];
  if ((val === undefined || val === null) && esNuevo && c.defecto !== undefined) val = c.defecto;

  const atributos = [
    req ? 'required' : '',
    c.maxLen ? `maxlength="${c.maxLen}"` : '',
    c.min !== undefined ? `min="${c.min}"` : '',
    c.max !== undefined ? `max="${c.max}"` : '',
    c.paso ? `step="${c.paso}"` : '',
    c.tipo === 'int' ? 'step="1"' : '',
    c.pattern ? `pattern="${UI.esc(c.pattern)}" title="${UI.esc(c.ayuda || '')}"` : ''
  ].join(' ');

  let control;
  if (c.tipo === 'select') {
    let ops = [];
    try { ops = await opcionesCampo(c); } catch (e) { ops = []; }
    const vacia = req ? '— Seleccione —' : 'Ninguno';
    control = `<select id="${id}" name="${c.k}" class="form-select" ${req ? 'required' : ''}>${UI.opciones(ops, val, { vacia })}</select>`;
  } else if (c.tipo === 'textarea') {
    control = `<textarea id="${id}" name="${c.k}" class="form-control" rows="3" ${atributos}>${UI.esc(val ?? '')}</textarea>`;
  } else {
    const tipoInput = { email: 'email', date: 'date', number: 'number', int: 'number', password: 'password' }[c.tipo] || 'text';
    const valorAttr = c.tipo === 'password' ? '' : `value="${UI.esc(val ?? '')}"`;
    control = `<input id="${id}" name="${c.k}" type="${tipoInput}" class="form-control" ${valorAttr} ${atributos}>`;
  }

  const ayuda = c.ayuda ? `<div class="form-text">${UI.esc(c.ayuda)}</div>` : '';
  return `<div class="mb-3">
    <label class="form-label" for="${id}">${UI.esc(c.t)}${req ? ' *' : ''}</label>
    ${control}${ayuda}
  </div>`;
}

// Arma el JSON a enviar. Los campos opcionales vacíos se omiten.
function leerPayload(ent, form, esNuevo) {
  const body = {};
  for (const c of ent.campos) {
    if (c.reqCrear && !esNuevo && !form.elements[c.k].value) continue;
    const el = form.elements[c.k];
    if (!el) continue;
    let v = el.value.trim();
    if (v === '') continue;
    if (c.tipo === 'number' || c.tipo === 'int' || c.origen) v = Number(v);
    body[c.k] = v;
  }
  return body;
}

Views.catalogo = async function (el, clave) {
  const rol = Session.rol();
  const legibles = Object.keys(ENTIDADES).filter(k => ENTIDADES[k].leer.includes(rol));
  if (!legibles.length) {
    UI.aviso(el, 'Su rol no tiene acceso a catálogos.', 'warning');
    return;
  }
  const key = legibles.includes(clave) ? clave : legibles[0];
  const ent = ENTIDADES[key];
  const escribe = ent.escribir.includes(rol);
  const acciones = ent.acciones || [];
  let filas = [];

  el.innerHTML = `
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
      <h3 class="mb-0">${UI.esc(ent.titulo)}</h3>
      ${escribe
      ? '<button class="btn btn-primary" id="btnNuevo" type="button">+ Nuevo</button>'
      : '<span class="badge text-bg-secondary">Solo lectura</span>'}
    </div>
    <ul class="nav nav-pills mb-3 flex-wrap">
      ${legibles.map(k => `<li class="nav-item">
        <a class="nav-link ${k === key ? 'active' : ''}" href="#/catalogo/${k}">${ENTIDADES[k].titulo}</a>
      </li>`).join('')}
    </ul>
    ${ent.descripcion ? `<p class="text-muted small mb-3">${UI.esc(ent.descripcion)}</p>` : ''}
    <div class="card shadow-sm"><div class="card-body p-0"><div class="tabla-scroll" id="tabla"></div></div></div>`;

  const cont = document.getElementById('tabla');

  function pintar() {
    const cols = ent.columnas.length + (escribe ? 1 : 0);
    let html = '<table class="table table-hover align-middle mb-0"><thead class="table-light"><tr>';
    html += ent.columnas.map(c => `<th>${UI.esc(c.t)}</th>`).join('');
    if (escribe) html += '<th class="text-end">Acciones</th>';
    html += '</tr></thead><tbody>';

    if (!filas.length) {
      html += `<tr><td colspan="${cols}" class="text-center text-muted py-4">No hay registros.</td></tr>`;
    }
    filas.forEach((f, i) => {
      html += '<tr>';
      html += ent.columnas.map(c => `<td>${celda(c, f[c.k])}</td>`).join('');
      if (escribe) {
        html += `<td class="text-end text-nowrap">
          <button class="btn btn-sm btn-outline-secondary" data-tipo="editar" data-idx="${i}">Editar</button>
          ${acciones.map((a, j) => `<button class="btn btn-sm ${a.clase(f)} ms-1" data-tipo="extra" data-extra="${j}" data-idx="${i}">${UI.esc(a.t(f))}</button>`).join('')}
          ${ent.sinBorrar ? '' : `<button class="btn btn-sm btn-outline-danger ms-1" data-tipo="borrar" data-idx="${i}">Eliminar</button>`}
        </td>`;
      }
      html += '</tr>';
    });
    html += '</tbody></table>';
    cont.innerHTML = html;
  }

  function celda(c, valor) {
    if (valor === null || valor === undefined || valor === '') return '—';
    if (c.badge) return UI.badge(valor);
    if (c.dinero) return UI.dinero(valor);
    if (c.porcentaje) return UI.esc(valor) + '%';
    return UI.esc(valor);
  }

  async function cargar() {
    UI.cargando(cont);
    try {
      filas = await Api.get(ent.ruta);
      pintar();
    } catch (e) {
      UI.mostrarError(cont, e);
    }
  }

  async function abrirFormulario(fila) {
    const esNuevo = !fila;
    const valores = fila ? { ...fila } : {};
    let campos = '';
    for (const c of ent.campos) campos += await campoHtml(c, valores, esNuevo);

    UI.abrirModal(`${esNuevo ? 'Nuevo' : 'Editar'} · ${ent.titulo}`, `
      <form id="formEntidad" novalidate>
        ${campos}
        <div id="formError"></div>
        <div class="text-end mt-3"><button class="btn btn-primary px-4" type="submit">Guardar</button></div>
      </form>`);

    const form = document.getElementById('formEntidad');
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!form.reportValidity()) return;
      const errorEl = document.getElementById('formError');
      UI.limpiarError(errorEl);
      const body = leerPayload(ent, form, esNuevo);
      try {
        if (esNuevo) await Api.post(ent.ruta, body);
        else await Api.put(`${ent.ruta}/${fila.id}`, body);
        UI.cerrarModal();
        UI.toast('Guardado correctamente.');
        cargar();
      } catch (err) {
        UI.mostrarError(errorEl, err);
      }
    });
  }

  async function borrar(fila) {
    if (!confirm(ent.avisoBorrar || `¿Eliminar este registro de ${ent.titulo}?`)) return;
    try {
      await Api.del(`${ent.ruta}/${fila.id}`);
      UI.toast('Registro eliminado.');
      cargar();
    } catch (e) {
      // Aquí llega el 409 con la razón
      UI.toast(e.message + (e.detalles && e.detalles.length ? ' ' + e.detalles.join(' ') : ''), 'danger');
    }
  }

  // Un solo listener para toda la tabla, así no se acumulan al recargar
  cont.addEventListener('click', async (ev) => {
    const btn = ev.target.closest('button[data-tipo]');
    if (!btn) return;
    const fila = filas[Number(btn.dataset.idx)];
    const tipo = btn.dataset.tipo;
    if (tipo === 'editar') abrirFormulario(fila);
    else if (tipo === 'borrar') borrar(fila);
    else if (tipo === 'extra') {
      const accion = acciones[Number(btn.dataset.extra)];
      try {
        await accion.fn(fila);
        UI.toast('Listo.');
        cargar();
      } catch (e) {
        UI.toast(e.message, 'danger');
      }
    }
  });

  const btnNuevo = document.getElementById('btnNuevo');
  if (btnNuevo) btnNuevo.addEventListener('click', () => abrirFormulario(null));

  cargar();
};