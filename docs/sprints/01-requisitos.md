# Sprint 1 · Análisis y requisitos

INDEA necesita registrar y consultar en un mismo sistema la atención a clientes, el seguimiento de equipos en reparación y los movimientos de compra y venta de equipos. La consulta de reparaciones por folio permite seguir el estado sin editar registros.

## Actores

- Administración. Supervisa el sistema, administra cuentas de empleados y consulta indicadores.
- Personal técnico. Registra o da seguimiento a reparaciones según los permisos de su cuenta.
- Ventas. Atiende a clientes y registra los movimientos de inventario y venta autorizados.
- Marketing. Crea y actualiza las publicaciones de equipos.
- Cliente. Consulta el estado de una reparación si conoce su folio.

## Backlog y criterios de aceptación

Las prioridades, estimaciones y responsables se toman del backlog incluido con la entrega. Las historias se ordenan aquí por alcance funcional.

| HU | Prioridad | Puntos | Responsable en el backlog | Historia y criterio de aceptación |
|---|---|---:|---|---|
| HU01 | Alta | 5 | Oscar / Alan | Como administrador, quiero dar de alta y editar empleados para gestionar sus cuentas. El sistema valida campos y exige un rol. |
| HU02 | Alta | 5 | Alan / Oscar | Como persona que recibe equipos, quiero registrar una reparación. Se asigna un folio único y se genera el recibo. |
| HU03 | Alta | 8 | Alan / Oscar | Como vendedor, quiero registrar compras y ventas. Se guarda el origen de la compra y no se permite vender dos veces el mismo equipo. |
| HU04 | Media | 3 | Alan | Como técnico, quiero actualizar el estado para mostrar el avance. Se conserva un historial con fecha. |
| HU05 | Media | 5 | Juan Manuel | Como administrador, quiero consultar reportes. El sistema separa ventas y cobros de reparación por periodo. |
| HU06 | Baja | 3 | Edgar | Como cliente, quiero consultar mi folio para saber el estado del equipo. La consulta no permite editar datos. |
| HU07 | Media | 5 | Edgar / Juan Manuel | Como encargado de marketing, quiero publicar equipos y manejar su estado. La venta respeta las restricciones de la publicación. |
| HU08 | Alta | 5 | Oscar | Como empleado, quiero iniciar sesión con mi rol. La contraseña se guarda como hash y se controla el número de intentos. |
| HU09 | Alta | 5 | Alan | Como usuario autorizado, quiero registrar, buscar y editar clientes. El sistema valida los datos y permite consultar su historial. |
| HU10 | Media | 5 | Juan Manuel / Oscar | Como administrador, quiero ver un resumen actualizado de la operación. |

## Requisitos generales

Persistir los registros en MySQL, conservar la separación entre pantallas y acceso a datos y validar las operaciones según el rol. Antes de aceptar un flujo, comprobar que sus datos se conservan después de cerrar y abrir la aplicación.
