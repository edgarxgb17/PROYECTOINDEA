# Sprint 6 · Pruebas y calidad

La prueba básica comprueba la coincidencia de una contraseña con su hash, el rechazo de una contraseña incorrecta y el rechazo de acceso sin sesión.

| Caso de integración | Resultado esperado | Estado |
|---|---|---|
| Iniciar sesión con una cuenta autorizada | Se abre su panel con las funciones disponibles para el rol | Pendiente de ejecución con MySQL |
| Registrar cliente y reparación | Se guarda el cliente y se genera un folio único | Pendiente de ejecución con MySQL |
| Cambiar estado y consultar el folio | El historial conserva el cambio y la consulta no permite editar | Pendiente de ejecución con MySQL |
| Registrar compra y venta | El equipo pasa al inventario y no admite una segunda venta | Pendiente de ejecución con MySQL |
| Registrar un cobro y consultar el reporte | El total aparece en el periodo correspondiente | Pendiente de ejecución con MySQL |

[Prueba básica](../../tests/Smoke.java)
