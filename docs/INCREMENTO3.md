# Incremento 3 — alcance y validación

## Cambios incluidos
- Los cuatro usuarios de demostración se crean individualmente si faltan; una base existente ya no impide crear roles de demostración ausentes.
- Acceso al módulo de clientes desde todos los roles. Las reglas de autorización de reparaciones siguen aplicándose en la capa de servicio.
- En el alta de reparación, el cliente se puede localizar por teléfono exacto o por coincidencia parcial de nombre/teléfono. Si hay varias coincidencias, se permite elegir.
- Ventanas Swing `JFrame` ampliadas, redimensionables y con tipografía/filas más legibles.
- Búsqueda de clientes por nombre o teléfono en el módulo de clientes.
- Diagramas UML de casos de uso y componentes en `docs/uml/`.

## Usuarios de demostración
| Rol | Usuario | Contraseña |
|---|---|---|
| Administrador | admin | admin123 |
| Técnico | tecnico1 | tecnico123 |
| Vendedor | vendedor1 | vendedor123 |
| Marketing | marketing1 | marketing123 |

En bases de datos que ya tengan esos nombres de usuario, las cuentas existentes se conservan. No se restablecen contraseñas existentes.

## Validación manual sugerida
1. Importar el proyecto en Eclipse como **Existing Projects into Workspace**.
2. Ejecutar `sql/01_crear_base_indea.sql` en MySQL Workbench si la base aún no existe.
3. Configurar `config/db.properties` y añadir MySQL Connector/J a `lib/` y al Build Path.
4. Ejecutar `com.indea.Main`.
5. Probar inicio de sesión con cada rol de demostración.
6. Abrir Clientes y buscar con nombre parcial y teléfono.
7. Abrir Reparaciones con Administrador o Técnico; buscar cliente por nombre y por teléfono y registrar una reparación.
8. Actualizar estado y revisar historial; consultar un folio desde la pantalla de inicio.

## Evidencia Git
Consulta los commits del repositorio para ver los archivos incorporados por etapa.

## Alcance honesto del incremento
Se integran y mejoran los flujos de autenticación, clientes, recepción/seguimiento de reparaciones y consulta por folio. La venta/inventario, administración completa de empleados, publicaciones de marketing y reportes de ingresos siguen siendo trabajo pendiente y no deben presentarse como terminados hasta implementarlos y probarlos.
