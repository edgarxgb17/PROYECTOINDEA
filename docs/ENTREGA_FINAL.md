# INDEA · Equipo 9 · Entrega E9
## Manual técnico
Java 17, Swing, JDBC, MySQL 8.0. Arquitectura vista → controlador/servicio → DAO JDBC → MySQL. NegocioService encapsula las operaciones nuevas y sus transacciones; las vistas no construyen SQL. Persona es la superclase de Cliente y Empleado. Sesion controla permisos; SeguridadUtil usa PBKDF2 con sal. Main inicia la aplicación. El SQL conserva las once tablas originales y añade fecha de pago y un trigger. Instalar siguiendo README. Para una instalación existente, respaldar antes de migrar; el script completo está destinado a una base nueva.
## Manual de usuario
Entrar con una cuenta del README. Clientes: registrar nombre/teléfono, buscar, editar; consultar historial buscando a la persona. Reparaciones: elegir cliente y técnico, capturar equipo y falla, guardar folio y recibo; buscar folio, registrar diagnóstico, costo, pago y estado hasta entrega. Consulta pública: ingresar folio en login, solo lectura. Inventario: Compra captura tipo, condición, precios y origen. Para una compra a una persona, buscarla o registrar sus datos sin salir de la operación. Para una venta, seleccionar el equipo y buscar o registrar a quien compra. Venta exige equipo disponible sin publicación activa no disponible. Publicaciones: seleccionar el equipo, indicar título, descripción y tipo; editar o desactivar seleccionando fila. Empleados: crear o editar datos, usuario, rol y contraseña; contraseña nueva desbloquea. Reportes: elegir día, semana o mes; muestra cobros de reparación y ventas. Panel administrador: totales al activar ventana. Cerrar sesión al terminar.
## Backlog final (implementación, pendiente aceptación integral)
| HU | Prioridad | Responsable original | Puntos | Función | Criterio |
|---|---|---|---|---|---|
|01|Alta|Oscar / Alan|5|Empleados|Alta, edición, rol, campos obligatorios|
|02|Alta|Alan / Oscar|5|Reparación|Folio único y recibo|
|03|Alta|Alan / Oscar|8|Compra/venta|Origen, inventario, no doble venta|
|04|Media|Alan|3|Estados|Catálogo e historial fechado|
|05|Media|Juan Manuel|5|Reportes|Totales separados por periodo|
|06|Baja|Edgar|3|Consulta|Folio solo lectura|
|07|Media|Edgar / Juan Manuel|5|Marketing|Publicaciones y bloqueo de venta|
|08|Alta|Oscar|5|Login|Roles, hash, bloqueo|
|09|Alta|Alan|5|Clientes|Validación e historial|
|10|Media|Juan Manuel / Oscar|5|Panel|Resumen actualizado|
## Entregables por sprint
0 visión: gestionar operaciones de INDEA en una base compartida; 1 análisis: documento original; 2 arquitectura: UML y SQL; 3 login, clientes y reparación; 4 estados, compras y ventas; 5 integración, inventario, marketing, reportes; 6 plan de pruebas; 7 documentación y revisión; 8 entrega y demostración. El índice de evidencias indica los documentos que acompañan la entrega.
## Pruebas
Compilación Java y Smoke ejecutados: ver resultado_pruebas.txt. Pruebas de integración pendientes de ejecutar con MySQL y escritorio: 1 login correcto/incorrecto y tres fallos; 2 alta cliente y duplicado; 3 recepción y folio único; 4 cambio de estado e historial; 5 compra de cliente/proveedor; 6 venta y segundo intento rechazado; 7 publicación próxima bloquea venta; 8 edición de empleado y persistencia al reiniciar; 9 reportes con cero y con cobros; 10 consulta pública sin edición. Resultado esperado: cada flujo válido persiste, inválido no modifica datos. Resultado obtenido: pendiente para los diez casos. Capturas de pantalla reales pendientes.
## Guion de demostración
Entrar administrador, crear cliente, registrar reparación y recibo, actualizar diagnóstico y pago, comprar equipo, publicar próximamente, intentar venta rechazada, actualizar publicación a disponible y vender, revisar historial y reportes, mostrar consulta pública. Retrospectiva propuesta: conservar separación por capas; mejorar pruebas automatizadas y seguridad de consulta pública; acordar responsables y fechas en equipo. No representa una retrospectiva celebrada.
