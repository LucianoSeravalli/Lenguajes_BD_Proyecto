# Cambios de vistas - GymCore

Fecha: 2026-08-18

## Cambios realizados

1. Se cambio el tema visual general de oscuro a claro en `src/main/resources/static/css/gymcore.css` para mejorar contraste y legibilidad.
2. Se reforzaron colores de texto, enlaces, encabezados, botones, formularios, tablas, tabs, modales, avisos y chips de estado.
3. Se ajustaron las pantallas de login y registro para usar el header publico en lugar del header interno del panel.
4. Se reemplazaron textos auxiliares tecnicos por mensajes mas claros para el usuario en login, registro, clases, membresias, rutinas y recepcion.
5. Se normalizo el render de chips en `src/main/resources/static/js/gymcore.js` para que los estados mantengan color aunque vengan con mayusculas, espacios o variantes desde la base.
6. Se escapan los mensajes de filas vacias antes de renderizarlos en HTML.
7. Se simplifico la vista de rutinas quitando el boton y el panel que mostraban ejercicios por rutina.
8. Se quito el apartado "Por vencer" de la vista de membresias.
9. Se limpio el panel general removiendo accesos de hoy, accesos denegados de hoy y membresias que vencen en 7 dias.

## Archivos modificados

- `src/main/resources/static/css/gymcore.css`
- `src/main/resources/static/js/gymcore.js`
- `src/main/resources/templates/paginas/login.html`
- `src/main/resources/templates/paginas/registro.html`
- `src/main/resources/templates/paginas/clases.html`
- `src/main/resources/templates/paginas/membresias.html`
- `src/main/resources/templates/paginas/rutinas.html`
- `src/main/resources/templates/paginas/recepcion.html`

## Archivos no modificados

- No se modificaron clases Java.
- No se modificaron controladores.
- No se modificaron servicios.
- No se modifico `pom.xml`.
- No se modifico la configuracion de base de datos.
