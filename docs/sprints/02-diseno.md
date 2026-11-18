# Sprint 2 · Arquitectura y datos

La aplicación se estructura en capas. Las ventanas de Swing reciben las acciones del usuario, los controladores y servicios aplican las validaciones, los DAO manejan las consultas y JDBC conecta con MySQL.

| Área | Responsabilidad |
|---|---|
| Vista | Formularios, listas, mensajes y navegación |
| Servicio | Validaciones, permisos y reglas de operación |
| DAO | Lectura y escritura de registros |
| Modelo | Clientes, empleados, usuarios, reparaciones y estados |
| MySQL | Almacenamiento de los registros y sus relaciones |

- [Casos de uso](../uml/casos_uso_incremento3.puml)
- [Modelo de datos](../uml/datos.puml)

Se incorpora la estructura del proyecto para Eclipse o Maven, junto con la configuración de ejemplo de MySQL. Cada instalación conserva sus credenciales en su configuración local.
