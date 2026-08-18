/* GymCore | cliente del API REST compartido por todas las paginas. */
 
const API = '/api';
 
/* ---------------- Llamadas HTTP ---------------- */
 
/**
 * Envuelve fetch y traduce los errores del ManejadorGlobalErrores.
 * El backend responde {estado, error, mensaje, codigoOracle}, asi que el
 * mensaje que ve el usuario es el que escribio el PL/SQL.
 */
async function pedir(ruta, opciones = {}) {
  const respuesta = await fetch(API + ruta, {
    headers: { 'Content-Type': 'application/json' },
    ...opciones
  });
 
  if (respuesta.status === 204) return null;
 
  const cuerpo = await respuesta.json().catch(() => null);
 
  if (!respuesta.ok) {
    const detalle = cuerpo?.mensaje || 'Error inesperado del servidor.';
    const error = new Error(detalle);
    error.codigoOracle = cuerpo?.codigoOracle;
    error.estado = respuesta.status;
    throw error;
  }
  return cuerpo;
}
 
const api = {
  get:  (ruta) => pedir(ruta),
  post: (ruta, datos) => pedir(ruta, { method: 'POST', body: JSON.stringify(datos) }),
  put:  (ruta, datos) => pedir(ruta, { method: 'PUT', body: JSON.stringify(datos) }),
  patch:(ruta) => pedir(ruta, { method: 'PATCH' }),
  del:  (ruta) => pedir(ruta, { method: 'DELETE' })
};
 
/* ---------------- Avisos ---------------- */
 
function avisar(mensaje, tipo = 'info') {
  const zona = document.getElementById('avisos');
  if (!zona) return;
 
  const icono = tipo === 'ok' ? 'check-circle-fill'
              : tipo === 'error' ? 'exclamation-triangle-fill'
              : 'info-circle-fill';
 
  const aviso = document.createElement('div');
  aviso.className = `gym-aviso ${tipo}`;
  aviso.innerHTML = `<i class="bi bi-${icono} me-2"></i><span></span>`;
  aviso.querySelector('span').textContent = mensaje;
  zona.appendChild(aviso);
 
  setTimeout(() => aviso.remove(), 5000);
}
 
/* ---------------- Utilidades de render ---------------- */
 
const chip = (valor) => {
  if (!valor) return '';
  const clase = String(valor).trim().toLowerCase().replace(/[^a-z0-9_-]+/g, '-');
  return `<span class="gym-chip ${clase}">${limpio(valor)}</span>`;
};
 
const fecha = (iso) =>
  iso ? new Date(iso).toLocaleDateString('es-CR') : '\u2014';
 
const fechaHora = (iso) =>
  iso ? new Date(iso).toLocaleString('es-CR', { dateStyle: 'short', timeStyle: 'short' }) : '\u2014';
 
const colones = (monto) =>
  new Intl.NumberFormat('es-CR', { style: 'currency', currency: 'CRC' }).format(monto ?? 0);
 
/** Escapa el texto que viene de la base antes de meterlo en innerHTML. */
const limpio = (texto) => {
  const nodo = document.createElement('div');
  nodo.textContent = texto ?? '';
  return nodo.innerHTML;
};
 
function filaVacia(tbody, columnas, mensaje = 'Sin registros.') {
  tbody.innerHTML =
    `<tr><td colspan="${columnas}" class="gym-vacio">${limpio(mensaje)}</td></tr>`;
}
 
/** Lee un formulario y devuelve un objeto, con los vacios como null. */
function datosDe(formulario) {
  const datos = {};
  new FormData(formulario).forEach((valor, clave) => {
    datos[clave] = valor === '' ? null : valor;
  });
  return datos;
}
 
/* ---------------- Estado de conexion en el header ---------------- */
 
async function revisarConexion() {
  const indicador = document.getElementById('estadoConexion');
  if (!indicador) return;
  const texto = indicador.querySelector('span');
 
  try {
    await api.get('/membresias/tipos');
    indicador.classList.add('ok');
    indicador.classList.remove('error');
    texto.textContent = 'Base conectada';
  } catch {
    indicador.classList.add('error');
    indicador.classList.remove('ok');
    texto.textContent = 'Sin conexion';
  }
}
 
document.addEventListener('DOMContentLoaded', revisarConexion);
