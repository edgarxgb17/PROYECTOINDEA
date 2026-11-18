# Sistema de Gestión para INDEA - Release final

Equipo 9 - Programación Orientada a Objetos, Universidad Veracruzana.

## Qué incluye este incremento

- Inicio de sesión con usuario y contraseña (HU08): contraseñas con PBKDF2, bloqueo tras 3 intentos fallidos y permisos por rol.
- Panel principal con resumen (equipos en reparación y clientes registrados).
- Clientes (HU09): registrar, consultar, buscar por nombre o teléfono (búsqueda parcial, sin distinguir mayúsculas/minúsculas; acepta teléfonos con espacios, guiones y paréntesis), editar; acceso al módulo desde los cuatro roles (eliminar solo administrador).
- Reparaciones (HU02, HU04): registrar equipo con folio único y recibo, asignar técnico, buscar cliente por nombre o teléfono, actualizar estado, diagnóstico, costo y pago, con historial de cambios.
- Consulta por folio de solo lectura (HU06), disponible desde la pantalla de inicio de sesión sin entrar al sistema.

Flujo completo para la demostración: iniciar sesión, registrar cliente, registrar reparación (se genera el folio), actualizar el estado hasta Entregado y consultar el folio.

## Interfaz y UML

Las ventanas se implementan con Swing `JFrame`, son redimensionables y usan componentes Swing estándar. En Eclipse, la pestaña **Design** requiere instalar el complemento WindowBuilder; el diseño visual no se guarda como un archivo de formulario independiente. Los diagramas actualizados están en `docs/uml/`. Consulta `docs/INCREMENTO3.md` para validación y alcance.

## Actualizar una instalación que ya tiene registros

1. Cierra INDEA y conserva una copia de tu archivo local `config/db.properties`.
2. Descomprime el proyecto actualizado, importa la nueva carpeta en Eclipse y copia tu configuración a `config/db.properties`. Conserva la misma dirección y las mismas credenciales.
3. Para conservar tus registros de `indea_sistema`, respalda esa base y ejecuta una sola vez [la actualización 03](sql/actualizaciones/03_proveedores_y_lotes.sql). Vincula las compras previas con su proveedor, sin borrar registros. Si tu base tiene otro nombre, modifica primero la línea `USE` del script.
4. En `indea_sistema` la columna de teléfono ya admite correo sin teléfono. Si actualizas una versión más antigua que no tiene ese cambio, revisa por separado la actualización 02 y su nombre de base.

## Compra, venta y publicaciones sin capturar IDs

- En **Inventario → Compra**, elige **Persona** para buscar por nombre, teléfono o correo. Usa **Registrar persona nueva** si no aparece. El alta y la compra se guardan juntas. Si compras a un negocio, elige **Proveedor**, selecciónalo o pulsa **Registrar proveedor nuevo**. Captura la cantidad y los precios por equipo. Cada unidad queda disponible para su seguimiento y venta por separado, según el estado elegido.
- En **Inventario → Proveedores**, administrador y vendedor pueden crear y editar fichas y consultar el historial. El historial muestra las compras agrupadas por lote, con cantidad, costo por equipo y total.
- En **Reparaciones → Seguimiento**, selecciona una fila y usa **Copiar folio** o selecciona el texto de su casilla. El nombre del cliente aparece en otra línea.
- En **Inventario → Venta**, selecciona el equipo en la tabla y después a la persona compradora. Puedes registrarla ahí mismo si hace falta.
- En **Publicaciones → Nueva**, selecciona el equipo por marca, modelo o estado. Para editar una publicación existente, selecciona su fila y conserva el equipo asociado.
- En **Historial**, busca a la persona para consultar sus compras, ventas y reparaciones.

## Refacciones para reparaciones

1. Como **Administrador**, abre **Inventario → Refacciones → Nueva refacción**. Escribe el nombre, el costo por pieza y las existencias iniciales. Ejemplo: Pantalla 14 pulgadas, costo `650.00`, existencias `3`.
2. Para registrar una llegada de piezas, elige la refacción y pulsa **Agregar existencias**. La cantidad se suma a la que ya había.
3. Como **Administrador** o **Técnico**, abre **Reparaciones → Seguimiento**, selecciona un folio y pulsa **Refacciones del folio → Agregar refacción**. Busca la pieza, elígela e indica cuántas unidades se usaron.
4. Revisa el listado del folio y vuelve a **Inventario → Refacciones**. Si se usó una pantalla de las tres disponibles, deben quedar dos. No se permiten cantidades mayores a las disponibles ni agregar piezas a folios entregados.

El costo mostrado de las piezas no cambia automáticamente el costo del servicio. Esta función reutiliza las tablas de refacciones de la base original.

## Cómo ejecutarlo

1. Con MySQL 8, abre y ejecuta [`sql/INDEA_base_xalapa.sql`](sql/INDEA_base_xalapa.sql). Crea `indea_xalapa` con 12 tablas llenas y las cinco cuentas originales. Si prefieres conservar los datos que ya capturaste, sigue la sección de actualización existente y no cambies la base en la configuración.
2. Descarga MySQL Connector/J (opción Platform Independent) desde https://dev.mysql.com/downloads/connector/j/ y copia el archivo .jar a la carpeta lib.
3. En Eclipse: File > Import > General > Existing Projects into Workspace y elige esta carpeta.
4. Clic derecho en el proyecto > Build Path > Configure Build Path > Libraries > Classpath > Add JARs... y elige el .jar de la carpeta lib. Usa JDK 17 o posterior.
5. Copia `config/db.properties.example` como `config/db.properties` y ajusta `db.user` y `db.password` para tu instalación de MySQL. Este archivo local no se sube al repositorio.
6. Ejecuta com.indea.Main (clic derecho > Run As > Java Application).

La base con datos incluye las cuentas originales del proyecto. Si eliges la instalación sin registros, `sql/01_crear_base_indea.sql` crea `indea_db` y sus cuentas iniciales. En ese caso reemplaza `indea_xalapa` por `indea_db` en la URL.

## Usuarios de prueba

| Rol | Nombre | Usuario | Contraseña inicial |
|---|---|---|---|
| Administrador / Dueño | Ángel Leonardo Hernández Casarín | `admin` | `admin123` |
| Técnico | Edgar Báez García | `Edgar Báez García` | `edgar123` |
| Técnico | Óscar Daniel Hernández Hernández | `Óscar Daniel Hernández Hernández` | `oscar123` |
| Vendedor | Alan López | `Alan López` | `alan123` |
| Marketing | Juan Manuel Hernández Cova | `Juan Manuel Hernández Cova` | `juan123` |

Estas son cuentas de demostración de la aplicación. Escríbelas como aparecen en la tabla y no las uses con información real.

**Base de datos y roles:** todos los usuarios de la aplicación trabajan sobre la misma base `indea_db`, porque clientes, reparaciones, inventario y ventas deben relacionarse entre sí. Cada empleado tiene una cuenta individual en la tabla `usuarios` y su rol se valida dentro de la aplicación. No se debe crear una base de datos separada por empleado, porque fragmentaría los registros compartidos.

## Estructura

- com.indea.modelo: Persona, Cliente, Empleado, Usuario, Reparacion, CambioEstado y enumeraciones.
- com.indea.dao: interfaces y su implementación JDBC.
- com.indea.servicio: reglas de negocio, validaciones y permisos por rol.
- com.indea.controlador: conecta la vista con los servicios.
- com.indea.vista: ventanas Swing.
- com.indea.util: ConexionBD, SeguridadUtil, Sesion y DatosIniciales.
- com.indea.excepcion: excepciones propias.

## Release final
Nuevos módulos: inventario, compras, ventas, empleados, publicaciones, historial y reportes. Ver docs/ENTREGA_FINAL.md. Alternativa a Eclipse: instalar Maven y ejecutar `mvn compile exec:java` desde esta carpeta. En Eclipse se debe agregar Connector/J manualmente; no se incluye el JAR externo. El SQL incluye cuentas de demostración. No ejecutar sobre una base existente sin respaldo. Compilación y prueba de seguridad básica verificadas; integración MySQL y capturas pendientes.

## Documentación y rúbrica

- [Índice de evidencias para la rúbrica](docs/RUBRICA_Y_EVIDENCIAS.md)
- [Entrega final con manual técnico, manual de usuario y backlog](docs/ENTREGA_FINAL.md)
- [Diagramas UML](docs/uml/)
- [Rúbrica de la actividad](docs/rubrica/RUBRICA_PROYECTO_POO.pdf)

Queda para una entrega posterior configurar los perfiles de MySQL para técnico, marketing y los demás roles que defina el equipo. Las cuentas de demostración dentro de la aplicación no son cuentas de MySQL.

## Entregas por sprint

- [Sprint 1 · Requisitos y backlog](docs/sprints/01-requisitos.md)
- [Sprint 2 · Diseño y datos](docs/sprints/02-diseno.md)
- [Sprint 3 · Primer incremento](docs/sprints/03-primer-incremento.md)
- [Sprint 4 · Funciones centrales](docs/sprints/04-funciones-centrales.md)
- [Sprint 5 · Integración](docs/sprints/05-integracion.md)
- [Sprint 6 · Pruebas](docs/sprints/06-pruebas.md)
- [Sprint 7 · Manuales y backlog final](docs/ENTREGA_FINAL.md)
- [Sprint 8 · Entrega final y rúbrica](docs/RUBRICA_Y_EVIDENCIAS.md)
