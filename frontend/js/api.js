// Guarda el token y el usuario en localStorage. Todo va en try/catch
// porque algunos navegadores bloquean el almacenamiento.
const Session = {
  get() {
    try {
      return JSON.parse(localStorage.getItem('hotel_session')) || null;
    } catch (e) {
      return null;
    }
  },
  set(datos) {
    try {
      localStorage.setItem('hotel_session', JSON.stringify(datos));
    } catch (e) { }
  },
  clear() {
    try {
      localStorage.removeItem('hotel_session');
    } catch (e) {  }
  },
  token() {
    const s = this.get();
    return s ? s.token : null;
  },
  usuario() {
    const s = this.get();
    return s ? s.usuario : null;
  },
  rol() {
    const u = this.usuario();
    return u ? u.rol : null;
  }
};

// Errores de la API
class ApiError extends Error {
  constructor(status, mensaje, detalles = []) {
    super(mensaje);
    this.status = status;
    this.detalles = detalles;
  }
}

// Arma el query string saltando los valores vacíos
function qs(params = {}) {
  const sp = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') sp.append(k, v);
  });
  const texto = sp.toString();
  return texto ? '?' + texto : '';
}

// Cliente HTTP
const Api = {
  async request(metodo, ruta, cuerpo, { auth = true } = {}) {
    const cabeceras = { 'Accept': 'application/json' };
    if (cuerpo !== undefined) cabeceras['Content-Type'] = 'application/json';

    const token = Session.token();
    if (auth && token) cabeceras['Authorization'] = 'Bearer ' + token;

    let res;
    try {
      res = await fetch(API_BASE + ruta, {
        method: metodo,
        headers: cabeceras,
        body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined
      });
    } catch (e) {
      throw new ApiError(0, 'No se pudo conectar con la API. Verifique que el backend esté encendido en ' + API_BASE + '.');
    }

    const texto = await res.text();
    let datos = null;
    if (texto) {
      try { datos = JSON.parse(texto); } catch (e) { datos = null; }
    }

    if (!res.ok) {
      // Token vencido o inválido: se cierra la sesión y se manda al login
      if (res.status === 401 && auth) {
        Session.clear();
        if (typeof renderNav === 'function') renderNav();
        ir('#/login');
      }
      const mensaje = (datos && datos.mensaje) || 'Ocurrió un error inesperado. Intente de nuevo.';
      const detalles = datos && Array.isArray(datos.detalles) ? datos.detalles : [];
      throw new ApiError(res.status, mensaje, detalles);
    }
    return datos;
  },

  get(ruta, params) { return this.request('GET', ruta + qs(params)); },
  post(ruta, cuerpo, opciones) { return this.request('POST', ruta, cuerpo, opciones); },
  put(ruta, cuerpo) { return this.request('PUT', ruta, cuerpo); },
  patch(ruta, cuerpo) { return this.request('PATCH', ruta, cuerpo); },
  del(ruta) { return this.request('DELETE', ruta); }
};
