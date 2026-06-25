# Proyecto Micronaut 4.8.0

Este proyecto está basado en **Micronaut Framework 4.8.0**, diseñado para la creación de microservicios eficientes y aplicaciones de alto rendimiento.

## Documentación de Referencia

* [Micronaut 4.8.0 User Guide](https://docs.micronaut.io/4.8.0/guide/index.html)
* [API Reference](https://docs.micronaut.io/4.8.0/api/index.html)
* [Configuration Reference](https://docs.micronaut.io/4.8.0/guide/configurationreference.html)
* [Micronaut Guides](https://guides.micronaut.io/index.html)

## Tecnologías y Plugins

* **Micronaut Maven Plugin:** [Documentación](https://micronaut-projects.github.io/micronaut-maven-plugin/latest/)
* **Maven Enforcer Plugin:** [Documentación](https://maven.apache.org/enforcer/maven-enforcer-plugin/)
* **Micronaut Serialization (Jackson):** [Documentación](https://micronaut-projects.github.io/micronaut-serialization/latest/guide/)
* **Micronaut AOT:** [Documentación](https://micronaut-aot.github.io/micronaut-aot/latest/guide/)

## Comandos de Uso

Para gestionar el ciclo de vida de la aplicación, utiliza los siguientes comandos en la terminal (desde la raíz del proyecto):

| Acción | Comando (Linux/Mac) | Comando (Windows) |
| :--- | :--- | :--- |
| **Ejecutar en modo desarrollo** | `./mvnw mn:run` | `mvnw.cmd mn:run` |
| **Compilar el proyecto** | `./mvnw compile` | `mvnw.cmd compile` |
| **Ejecutar tests** | `./mvnw test` | `mvnw.cmd test` |
| **Empaquetar (JAR)** | `./mvnw package` | `mvnw.cmd package` |
| **Limpiar proyecto** | `./mvnw clean` | `mvnw.cmd clean` |
| **Validar (Enforcer)** | `./mvnw validate` | `mvnw.cmd validate` |





### Limpiar y empaquetar
```bash
./mvnw clean package
```


### Preparar carpeta de destino
```bash
rm -rf release
```

### Generar ejecutable
```bash
jpackage \
--name "Desk" \
--input target \
--main-jar mimbre-0.1.jar \
--main-class com.app.Application \
--type app-image \
--dest release
```
