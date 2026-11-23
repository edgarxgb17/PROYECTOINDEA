# Actualización: Historial y Reportes

## Historial

- Al abrir el módulo se muestran todas las reparaciones, compras y ventas registradas.
- Las compras a proveedores se incluyen en la vista general.
- El botón «Consultar cliente» filtra las operaciones de la persona seleccionada.
- El botón «Ver todos» permite regresar a la vista general.
- La tabla muestra operación, referencia, cliente/origen, fecha, importe y estado.

## Reportes

- Al ingresar, se muestran los ingresos del mes actual (desde el día 1 hasta hoy).
- El botón «Periodo» permite elegir hoy, semana actual, mes actual, todo el historial o fechas específicas.
- Se incluyen los ingresos de ventas efectivamente registradas y reparaciones marcadas como pagadas y con fecha de cobro.
- Aparece la suma de los ingresos de ambas categorías.
- El PDF de ganancias utiliza el mismo periodo seleccionado.

## Comprobación con datos existentes

1. Ingresa a Historial: deben aparecer operaciones sin elegir primero un cliente.
2. Usa «Consultar cliente» y «Ver todos» para comprobar los filtros.
3. Ingresa a Reportes: comprueba que el periodo corresponda al mes actual.
4. Presiona «Periodo» y selecciona «Todo el historial» para incluir operaciones anteriores.
5. Si los importes siguen en cero, revisa en MySQL las tablas `ventas` y `reparaciones`, especialmente los campos `pagado` y `fecha_pago`.

No se necesita volver a importar archivos SQL: esta actualización solo modifica consultas y presentación.
