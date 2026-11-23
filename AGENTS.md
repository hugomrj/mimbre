# AGENTS.md — Guía de trabajo para agentes de IA en Mimbre

> Instrucciones operativas para modificar este repositorio. Para la descripción
> funcional y técnica del sistema, ver `CONTEXT.md`.

## 1. Identidad del proyecto

**Mimbre** es una aplicación de escritorio/local de gestión comercial para un
pequeño negocio paraguayo: productos, categorías, clientes, proveedores, ventas,
compras, stock y dashboard. Java 21 + Micronaut 4.8.0 + Micronaut Data JDBC +
H2 (archivo) + Pebble + HTMX. Se distribuye como ejecutable multiplataforma con
`jpackage` (JRE incluido) y corre offline en `http://localhost:8000`.

Para retomar una sesión: leé **`PROCESS.md`** (traspaso y próximos pasos).
Antes de cambiar algo: leé **`PROGRESS.md`** (dónde está el desarrollo) y
`CONTEXT.md` (qué es el sistema). Si el cambio altera el dominio, la arquitectura
o las reglas de negocio, actualizá `CONTEXT.md` en el mismo commit.

## 2. Reglas de oro

1. **No romper el modo offline ni el empaquetado.** No agregues dependencias de
   frontend por CDN ni builds con npm/bundler. Todo asset nuevo va versionado en
   `src/main/resources/public/vendor/`.
2. **Mantené la paridad API/UI.** Cada feature tiene `XxxController` (`/api`) y
   `XxxUiController` (`/ui`). Si agregás una operación, cubrí ambas o justificá
   por qué no.
3. **La lógica de negocio va en el Service, no en el Controller.** Los
   controladores solo parsean, delegan y arman el modelo/DTO.
4. **Todo en español**: nombres de clases, mensajes de error, textos de UI,
   comentarios. Los mensajes se muestran al usuario tal cual.
5. **El esquema se versiona con Flyway.** No uses `ALTER TABLE` a mano ni cambies
   `schema-generate` sin actualizar las migraciones. Cada cambio de esquema es
   una migración nueva y debe aplicar también sobre bases existentes (ver
   `PLAN.md` M0).
6. **No expongas datos ni endpoints de desarrollo en producción.** `DevSeedController`
   existe para demo; no lo uses como base para features reales.
7. **Preservá los datos del usuario.** La base vive en `~/.mimbre/data` y los
   backups en `~/.mimbre/backups`. No los borres ni fuerces re-seed; el marcador
   `.initialized` controla la carga demo. Las carpetas `./data` y `target/data`
   solo se leen para migrar.
8. **`PROGRESS.md` es la fuente de verdad del avance.** Leelo completo al inicio
   de cada sesión (snapshot, "Ahora" y "En curso") y contrastalo con el repo
   (`git log -1`, `git status`, tests). Al terminar una tarea, marcala como
   hecha, agregá la fecha, una línea en el "Registro de avances" y actualizá el
   snapshot. Si la tarea pedida no figura en el plan, preguntá antes de empezar.
9. **El orden de construcción está en `PLAN.md`.** Un hito a la vez: no mezcles
   dos ni te adelantes. Al terminar, entregá el resumen con la plantilla de
   `PLAN.md` §3 y esperá la revisión del humano antes de seguir.

## 3. Comandos

```bash
./mvnw mn:run                 # desarrollo (http://localhost:8000)
./mvnw compile                # compilar
./mvnw test                   # tests JUnit 5 (10 tests)
./mvnw clean package          # JAR sombreado en target/mimbre-0.1.jar
python build.py               # package + jpackage → release/Mimbre
./mvnw validate               # enforcer
```

- Requiere **JDK 21** y, para `build.py`, `jpackage` disponible en el PATH.
- Para probar la app sin interfaz: `./mvnw mn:run` y usar `curl` contra
  `http://localhost:8000`.
- Smoke test rápido de la API:

```bash
curl -s http://localhost:8000/api/dashboard
curl -s http://localhost:8000/api/productos | head
curl -s http://localhost:8000/api/dev/seed/status
```

- La app también abre una bandeja del sistema; para detenerla usar el menú
  "Salir" (no `Ctrl+C` en la versión empaquetada).

## 4. Mapa del código

```
src/main/java/com/app/
├── Application.java              # main, chequeo de puerto, bandeja, navegador
├── AppPaths.java                 # rutas estables: ~/.mimbre/data y backups
├── LegacyDataMigrator.java       # migra ./data o ./target/data a ~/.mimbre
├── DatabaseInitializer.java      # base plantilla, seed 1 vez, backup
├── BackupService.java            # copia la base a ~/.mimbre/backups
├── DemoDataSeeder.java           # 10 categorías, 10 proveedores, 15 clientes, 49 productos
├── HomeController.java           # GET / → dashboard
├── cliente/{controller,dto,model,repository,service}
├── compra/{controller,dto,model,repository,service}
├── dashboard/{controller,dto}
├── exception/                    # BusinessException, ResourceNotFoundException,
│                                 # StockInsuficienteException, GlobalExceptionHandler
├── movimiento_stock/{dto,model,repository,service}
├── producto/{controller,dto,model,repository,service}
├── secuencia/{model,repository,service}
├── producto_categoria/{controller,dto,model,repository,service}
├── proveedor/                    # controller, dto, model, repository, service
├── utils/DevSeedController.java  # endpoints de datos masivos (solo dev)
└── venta/{controller,dto,model,repository,service}

src/main/resources/
├── application.properties        # puerto 8000, H2, Flyway, backup, estáticos
├── db/migration/                 # Flyway: V1 baseline, V2 fecha, V3 integridad, V4 categoría, V5 kardex, V6 secuencias
├── views/                        # Pebble: layout.html + <feature>/*.html + fragments/
└── public/                       # css/, js/, vendor/ (htmx, chartjs, fontawesome, ...)
```

## 5. Anatomía de un feature

Para agregar o modificar un feature, seguí este orden:

1. **Model** (`model/Xxx.java`): `@MappedEntity`, `@Id`,
   `@GeneratedValue(GeneratedValue.Type.AUTO)`, campos y getters/setters
   manuales. Sin Lombok.
2. **Repository** (`repository/XxxRepository.java`): interfaz
   `@JdbcRepository(dialect = Dialect.H2)` extendiendo `CrudRepository` o
   `PageableRepository`. Búsquedas con `@Query` y `LIKE` case-insensitive.
3. **DTO(s)** (`dto/`): `@Serdeable`; uno de salida y otro de formulario si hace
   falta. Validaciones Jakarta en el DTO de entrada (`@NotBlank`, `@NotNull`,
   `@Positive`, `@Min`). Mensajes de validación en español.
4. **Service** (`service/XxxService.java`): `@Singleton`; `@Transactional` en
   escrituras; mapeo manual entidad ↔ DTO; reglas de negocio y excepciones.
5. **Controller API** (`controller/XxxController.java`): `@Controller("/api/...")`,
   devuelve `HttpResponse<...>`, usa `@Valid @Body` en escrituras.
6. **Controller UI** (`controller/XxxUiController.java`): `@Controller("/ui/...")`,
   `@View("...")`, devuelve `Map<String,Object>` como modelo. En POST de
   formularios, devuelve la vista de tabla con `mensaje` o `error` en el modelo.
7. **Vistas** (`views/<feature>/table.html`, `form.html`, `results.html`):
   extienden `layout.html`, definen `{% block content %}` y el
   `[data-page-title]` para el navbar. Reutilizá los fragmentos de paginación y
   los modales.
8. **JS** (si hay interacción): un manager en `public/js/<feature>.js`,
   registrado en `layout.html`. No uses frameworks; seguí el estilo de
   `venta.js`/`compra.js`.
9. **Rutas**: agregá el ítem al sidebar de `layout.html` con
   `hx-get`/`hx-target="#id-main"`/`hx-select="#id-main"`/`hx-push-url="true"` y
   `onclick="navTo(this, event)"`.

## 6. Convenciones de código

- **Paquetes por feature** (`com.app.<feature>.<capa>`); no crear paquetes
  globales `controllers/`, `services/`, etc.
- **Nombres en español** para el dominio (`Producto`, `Venta`, `Compra`,
  `Proveedor`, `Cliente`, `ProductoCategoria`). Evitá mezclar inglés en el
  dominio.
- **Inyección por constructor**; sin campos `@Inject` ni Service Locator.
- **BigDecimal** para dinero; nunca `double`. **LocalDateTime** para fechas.
- **Precios/costos**: el servidor recalcula subtotales y totales; no confíes en
  los montos del cliente más allá del precio unitario enviado.
- **Estados**: `"COMPLETADA"` / `"ANULADA"`. **Condiciones**: `"Contado"` /
  `"Crédito"`.
- **Números de factura**: los genera `SecuenciaComprobanteService` (`VENTA`:
  `001-001-0000000`, `COMPRA`: `COMP-0000000`). No uses `count() + 1`.
- **Listados**: paginados con `?page=&size=` (default 10), búsqueda con `?q=`.
- **Errores**: lanzá `ResourceNotFoundException`, `BusinessException` o
  `StockInsuficienteException`; el `GlobalExceptionHandler` los traduce a
  HTTP 404/400/500 y a toast en la UI. No devuelvas `Map.of("error", ...)`
  desde un servicio.
- **UI**: usá las variables CSS de `variables.css` y las clases existentes de
  `components.css`/`layout.css`. No agregues estilos inline si hay una clase.
- **Selects**: todo `<select>` debe llevar `class="form-control vanilla-select"`;
  nunca uses un select nativo. El componente se inicializa solo en
  `DOMContentLoaded` y `htmx:afterSwap`.
- **JavaScript**: `const`/`let`, funciones nombradas, sin dependencias nuevas.
  Respetá el orden de carga de `layout.html` (ui → dominio → main → app).

## 7. Invariantes del dominio (no romper)

- Una venta **no puede** dejar stock negativo; se valida antes de escribir.
- Anular una venta **repone** stock; anular una compra **lo reduce** (piso 0).
- El stock se modifica **solo** a través de `MovimientoStockService`; todo
  cambio deja una fila en `movimiento_stock` en la misma transacción.
- Una venta/compra debe tener al menos un detalle.
- Los detalles guardan **snapshot** de nombre y precio; no los reemplaces por
  joins en las vistas históricas.
- Productos, clientes y proveedores no se borran: se desactivan
  (`activo=false`) y su historial se conserva. Las categorías con productos no
  se pueden desactivar.
- Los totales se calculan en el servidor y se persisten en la cabecera.

## 8. Trampas conocidas (leer antes de tocar)

- **Foreign keys y unique**: la base rechaza SKU, RUC y números de comprobante
  duplicados, cantidades negativas y referencias huérfanas. Los errores de
  integridad se devuelven como HTTP 400.
- **Soft delete**: `delete` en producto, cliente y proveedor marca `activo=false`;
  no hagas `DELETE` físico ni reactives sin querer al editar.
- **`Producto.categoria` es cache**: la FK real es `categoria_id`. Al guardar un
  producto, `ProductoService` setea ambos; no escribas en `producto` sin pasar
  por el servicio.
- **Secuencias**: `secuencia_comprobante` se actualiza con `FOR UPDATE` en la
  transacción del documento. Si agregás otro tipo de comprobante, creá su fila en
  la migración.
- **Kardex**: `producto.stock` es cache; la verdad es la suma de
  `movimiento_stock`. No cambies stock directamente. Las compras recalculan
  `costo_actual` promedio ponderado; las anulaciones no lo revierten.
- **H2 en archivo**: `AUTO_SERVER=TRUE` y `DB_CLOSE_DELAY=-1`. Si corrés tests o
  scripts contra la base, hacelo con la app detenida.
- **Seed**: `DemoDataSeeder` solo corre si no existe `~/.mimbre/data/.initialized`.
  Borrar ese archivo re-siembra (y puede duplicar si la base ya tiene datos).
- **Puerto 8000 fijo**; si está ocupado, `Application` avisa y no arranca. No
  cambies el puerto sin actualizar `README.md`, `application.properties`,
  `Application.java` y `build.py`.
- **Bandeja del sistema**: en macOS/Linux puede no estar disponible; hay fallback
  que mantiene el proceso vivo.
- **Base en `~/.mimbre/data`**: sobrevive a `mvn clean`; no la borres a mano.
  `LegacyDataMigrator` copia una sola vez desde `./data` o `target/data`.
- **Tests**: hay 10 (humo, baseline, migración, tres de integridad, seed, kardex y dos de numeración). Si agregás
  lógica, sumá tests en `src/test/java` con JUnit 5 + Micronaut Test.

## 9. Verificación antes de terminar

1. `./mvnw -q compile` sin errores ni warnings nuevos.
2. `./mvnw test` (si agregaste tests).
3. Levantar `./mvnw mn:run` y probar el flujo tocado:
   - API: `curl` al endpoint correspondiente.
   - UI: navegar en `http://localhost:8000`, probar alta/edición/borrado y
     verificar que los toasts de éxito/error aparezcan.
4. Probar el caso borde del cambio: stock insuficiente, borrado con
   dependencias, anulación, paginación, búsqueda.
5. Revisar que la UI siga funcionando **sin red** (assets locales).
6. Actualizar `README.md`/`CONTEXT.md` si cambió el comportamiento, las rutas o
   el modelo de datos, y `PROGRESS.md` (tarea hecha, fecha y línea en el
   registro).
7. Buscá `<select` en `src/main/resources/views` y confirmá que **todos** tengan
   `class="... vanilla-select"`. Un select nativo es un bug de UI.

## 10. Qué evitar

- Refactors masivos no pedidos, renombres de paquetes o cambios de estilo
  globales.
- Introducir Spring, JPA/Hibernate, Lombok, MapStruct, React/Vue, npm o CDNs.
- Reemplazar HTMX/Pebble por una SPA.
- Tocar `./data/`, `target/` o `release/` a mano.
- "Arreglar" el esquema solo con SQL manual sin reflejarlo en el código.
- Dejar endpoints de dev con datos reales o sin `?confirmar=true`.
