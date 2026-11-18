# Evidencias de la rúbrica · INDEA POO

[Rúbrica original](rubrica/RUBRICA_PROYECTO_POO.pdf) · [Instrucciones de instalación](../README.md)

| Criterio final | Archivos disponibles y alcance |
|---|---|
| Aplicación ejecutable y funcional | Proyecto Java Swing para Eclipse o Maven en [src](../src/). El README indica cómo arrancarlo. La validación completa desde la interfaz requiere MySQL. |
| Código organizado y documentado | [Código fuente](../src/com/indea/) y arquitectura en el [manual técnico](ENTREGA_FINAL.md#manual-técnico). |
| UML y modelo de datos | [Diagramas en formato PlantUML](uml/), incluidos casos de uso, clases, componentes y datos. |
| Persistencia | [Script de instalación](../sql/01_crear_base_indea.sql) y [SQL original adjunto](../sql/original/bd_indea%20%281%29%281%29.sql). Los perfiles propios de MySQL quedan pendientes. |
| Pruebas documentadas | [Prueba básica](../tests/Smoke.java), [resultado previo conservado](resultado_pruebas.txt) y [plan de pruebas de integración](ENTREGA_FINAL.md#pruebas). |
| Git y participación | Los commits del repositorio permiten revisar los archivos incorporados por sprint. Los materiales adjuntos no acreditan por sí solos la participación individual de los integrantes. |
| Manuales y presentación | [Manual técnico, manual de usuario y guion de demostración](ENTREGA_FINAL.md). Guion y propuesta de retrospectiva no equivalen a presentación ni retrospectiva realizadas. Faltan capturas reales. |
| SCRUM y entregas E1–E9 | [Backlog final y resumen por sprint](ENTREGA_FINAL.md) y [documentación del incremento 3](INCREMENTO3.md). No se adjuntaron el Project Charter, las actas ni las evidencias históricas completas de cada sprint. |

## Ejecutar la prueba básica

Desde la raíz, en una consola compatible con Bash y con JDK 17 o posterior:

```bash
mvn compile
javac -encoding UTF-8 -cp target/classes -d target/test-classes tests/Smoke.java
java -cp target/classes:target/test-classes Smoke
```

En Windows usa un punto y coma como separador del classpath en el último comando. Esta prueba no valida la integración con MySQL.
