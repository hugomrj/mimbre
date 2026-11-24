# PROGRESS.md — Estado de desarrollo de Mimbre

> **Este es el documento que un agente de IA debe leer primero.** Responde en un
> vistazo: dónde estoy, qué terminé, qué estoy haciendo, qué sigue y qué me
> bloquea. Contexto del sistema: `CONTEXT.md`. Reglas de trabajo: `AGENTS.md`.
> Plan por hitos y revisiones: `PLAN.md`. Traspaso de sesión: `PROCESS.md`.

**Última actualización:** 2026-11-23 · **Versión:** 0.1 · **Rama:** `main` · **Commit base:** `20b584f` (+ M0/M1a sin commitear)
**Tests:** 10 · **Cobertura:** sin medir

---

## ⚡ Snapshot para IA (leer primero)

> Si sos un agente: leé esta tabla, después `## Ahora` y `## En curso`, y recién
> entonces proponé o ejecutá. No asumas que algo está hecho porque exista el
> archivo: verificá contra `## Hecho` y contra el repo.

| Pregunta | Estado al 2026-11-23 |
|---|---|
| **¿Qué es el proyecto?** | **Mimbre**: gestión comercial de escritorio para un pequeño negocio paraguayo (productos, categorías, clientes, proveedores, ventas, compras, stock y dashboard). Java 21 + Micronaut 4.8 + H2 + Pebble/HTMX, offline en `http://localhost:8000`. |
| **¿En qué fase está?** | **MVP funcional + base versionada, íntegra, con kardex y secuencias (M0–M3).** Flyway, constraints, FKs, categoría normalizada y soft delete. Sin login por diseño. |
| **¿Qué está terminado y usable?** | CRUD de productos, categorías, clientes y proveedores; ventas y compras con actualización de stock; anulaciones con reversión; dashboard con KPIs; búsqueda y paginación; build multiplataforma con JRE incluido. |
| **¿Qué estoy haciendo ahora?** | **M3 implementado; dashboard y UI extra también.** Esperando revisión y commit. Datos en `~/.mimbre`, integridad, kardex, secuencias y dashboard ampliado. |
| **¿Cuál es la próxima acción concreta?** | Commitear lo pendiente, revisar M3 y decidir: **M4 — Caja** o **UX de venta completa**. |
| **¿Qué me bloquea?** | Nada. |
| **¿Qué decisión está pendiente?** | Ninguna de alcance; la siguiente es numeración (M3). |
| **¿Qué NO asumir?** | Que hay caja, cobros o tests de ventas a crédito: **no los hay**. Kardex, secuencias, FKs, unique y soft delete sí. El login no existe por diseño. |
| **¿Dónde toco si...?** | Ver `AGENTS.md` §4 (mapa del código) y §5 (anatomía de un feature). |
| **¿Cómo sé que terminé?** | Criterio del hito en `PLAN.md` + checklist de `AGENTS.md` §9. |

### Frescura del documento (importante para la IA)

1. El commit del encabezado debe coincidir con `git rev-parse --short HEAD`.
2. Si el snapshot tiene **más de 7 días**, el commit no coincide, o `git status`
   muestra cambios sin registrar: **preguntá antes de asumir**.
3. Si el código contradice este documento, **gana el código**: corregí el
   documento en el mismo cambio.
4. Nunca borres entradas de `## Hecho` ni del `## Registro de avances`: son
   historial. Solo se agrega.

---

## Protocolo para agentes de IA

1. Leer `## ⚡ Snapshot`, `## Ahora` y el hito correspondiente de `PLAN.md`.
2. Contrastar con el repo: `git log -1`, `git status`, `./mvnw test`.
3. Si la tarea pedida no está en `## Ahora` ni en `## En curso`, preguntar si se
   agrega antes de empezar.
4. Al terminar: marcar `[x]`, mover la tarea a `## Hecho` con fecha, agregar una
   línea en `## Registro de avances` y actualizar el snapshot y el encabezado.
5. Si el cambio altera dominio, arquitectura o rutas, actualizar también
   `CONTEXT.md`.

---

## Cómo usar este archivo (humano)

1. Marcá `[x]` **solo cuando esté probado**, no cuando esté codeado.
2. Al terminar una tarea: movela a **Hecho** con fecha y referencia (commit, PR o "probado a mano").
3. Lo que no vas a hacer ahora va a **Backlog**, no a **Ahora**.
4. Una tarea = un resultado verificable. Si es grande, partila en subtareas.
5. En cada sesión: actualizá el **Snapshot**, mové tarjetas y agregá una línea al **Registro**.
6. Si una tarea cambia el dominio o la arquitectura, actualizá también `CONTEXT.md`.

**Prioridades:** `alta` = bloquea o duele · `media` = mejora concreta · `baja` = nice to have.

---

## Ficha técnica

| Campo | Valor |
|---|---|
| Versión | 0.1 |
| Java / Micronaut | 21 / 4.8.0 |
| Base de datos | H2 en `~/.mimbre/data/database.mv.db` (backups en `~/.mimbre/backups`) |
| Puerto | 8000 |
| Tests | 10 (humo, baseline, migración, integridad, seed, kardex y numeración) |
| Distribución | `jpackage` app-image (Win/Linux/macOS) vía `build.py` + CI |
| Último commit | `20b584f` |

---

## Ahora (máximo 3)

- [ ] **M3 — Numeración** · `alta` — **implementado, en revisión**
  - `secuencia_comprobante` con prefijo/longitud, backfill desde los números existentes y `FOR UPDATE` en la transacción.
  - Ventas y compras usan la secuencia; unique ya impedía duplicados.
  - Criterio: borrar un comprobante no reutiliza su número y la secuencia sobrevive reinicios.

- [ ] **Tests mínimos de ventas y compras** · `alta`
  - Cubrir: venta descuenta stock, venta sin stock falla, anular venta repone, compra aumenta stock, anular compra reduce.
  - Criterio: `./mvnw test` verde con al menos esos 5 casos.

---

## En curso

> Mové acá lo que estés haciendo. Una sola cosa a la vez si es posible.

- [ ] **Revisión de M3** — demo de `PLAN.md` §5: crear 3 ventas, anular/borrar la del medio y verificar que la siguiente no reutiliza.

---

## Próximo

### Calidad y datos

- [ ] **Stock atómico con bloqueo optimista** · `baja`
  - Hoy todo pasa por `MovimientoStockService`, pero sin lock; la app es monousuario local.
  - Criterio: `UPDATE ... WHERE stock >= ?` o versión optimista, con test.

- [ ] **Validación en DTOs de cliente y proveedor** · `media`
  - Revisar que los `@Valid` de los controladores tengan constraints reales.
  - Criterio: errores 400 claros al enviar datos inválidos.

- [ ] **Completar o eliminar `DevSeedController.limpiar`** · `baja`
  - Hoy solo devuelve conteos, no borra.
  - Criterio: borra en orden respetando dependencias, o se elimina el endpoint.

### Producto

- [ ] **Reportes** · `media`
  - El ítem del sidebar existe pero no tiene ruta.
  - Criterio: al menos ventas por período y productos más vendidos, o se quita el enlace.

- [ ] **Exportar / imprimir factura (PDF)** · `media`
  - Hoy el detalle es solo HTML.
  - Criterio: botón "Imprimir/PDF" que genere la factura con los datos del snapshot.

- [ ] **Dashboard con consultas agregadas** · `media`
  - Hoy carga todas las ventas en memoria y agrupa por `dd/MM` (mezcla años).
  - Criterio: `SUM`/`GROUP BY` en SQL y serie separada por año.

- [ ] **Normalizar `Producto.categoria` a FK** · `media`
  - Es M1b: `categoria_id` + migración de datos + formularios y seed por ID.

- [ ] **Restore de la base desde la UI** · `media`
  - El backup automático ya existe en `~/.mimbre/backups`; falta restaurar desde la app.
  - Criterio: elegir un backup y recuperar los datos con confirmación.

### Distribución

- [ ] **Instaladores nativos** · `baja`
  - Hoy es app-image portable; evaluar `.msi` (Windows), `.dmg` (macOS), `.deb` (Linux).
  - Criterio: instalador generado en CI y probado en cada SO.

- [ ] **Firma de código / notarización (macOS, Windows)** · `baja`
  - Evita warnings de "app no verificada".
  - Criterio: build firmado en CI (requiere certificados).

- [ ] **Rotación de logs y nivel configurable** · `baja`
  - Criterio: `logback.xml` con rotación diaria y perfil de producción.

---

## Hecho

### Base de datos y calidad

- [x] **M0 — Base segura** (2026-11-23): Flyway V1/V2, baseline, backup automático, `database_empty` completa y 2 tests. Aprobado al continuar con M1.
- [x] **M1 — Integridad** (2026-11-23): datos en `~/.mimbre`, V3, soft delete, categoría FK (V4) y 7 tests. Aprobado al continuar con M2.
- [x] **M2 — Kardex** (2026-11-23): V5, movimientos, costo promedio, stock mínimo y 8 tests. Aprobado al continuar con M3.

### Núcleo de negocio

- [x] CRUD de **productos** (API + UI), con SKU, precio, stock y categoría.
- [x] CRUD de **categorías** de producto, con validación de borrado si tienen productos.
- [x] CRUD de **clientes**, con validación de borrado si tienen ventas.
- [x] CRUD de **proveedores**, con validación de borrado si tienen compras.
- [x] **Ventas**: carrito, validación de stock, descuento automático, total calculado en servidor, factura autogenerada `001-001-%07d`, detalle y filtro por estado.
- [x] **Anulación de ventas** con reposición de stock y `fechaAnulacion`.
- [x] **Compras**: registro, aumento de stock, costo, factura `COMP-%07d`, detalle y filtro por estado.
- [x] **Anulación de compras** con reducción de stock.
- [x] **Dashboard**: KPIs, gráficos y productos con bajo stock.
- [x] Búsqueda por nombre/SKU/RUC y paginación en todos los listados.
- [x] Semilla demo (10 categorías, 10 proveedores, 15 clientes, 49 productos) y endpoints de datos masivos.

### UI / UX

- [x] Layout con sidebar, navbar, tema claro/oscuro y paletas persistidas en `localStorage`.
- [x] Navegación sin recarga con HTMX sobre `#id-main`.
- [x] Modales propios, toasts de éxito/error, `vanilla-select`, selector de tamaño de página.
- [x] Assets vendorizados para funcionar **offline** (HTMX, Chart.js, FontAwesome, fuentes).
- [x] Manejo global de errores con mensajes en español y toast vía `HX-Trigger`.
- [x] Dashboard ampliado y compacto: 8 KPIs, 4 gráficos combinados con relieve, ranking top y bajo stock.

### Infraestructura

- [x] H2 en archivo con `AUTO_SERVER`, creación de `./data` y base plantilla `database_empty`.
- [x] Arranque con chequeo de puerto ocupado, apertura del navegador y bandeja del sistema.
- [x] JAR sombreado + `jpackage` (app-image con JRE incluido) mediante `build.py`.
- [x] CI multiplataforma (Windows/Linux/macOS) y release automático en tags `v*`.
- [x] `README.md` con comandos de build y ejecución.

### Documentación

- [x] `CONTEXT.md`: qué es el sistema, arquitectura, modelo, reglas y limitaciones. (2026-11-23)
- [x] `AGENTS.md`: convenciones y guía operativa para agentes. (2026-11-23)
- [x] `PROGRESS.md`: este archivo. (2026-11-23)

---

## Backlog (sin compromiso)

- [ ] Cuentas corrientes / pagos parciales de ventas a crédito.
- [ ] Impuestos y descuentos por línea.
- [ ] Multi-sucursal / multi-depósito.
- [ ] Importación/exportación CSV de productos y clientes.
- [ ] Etiquetas o códigos de barras.
- [ ] Usuarios y auditoría (quién hizo cada venta/anulación).
- [ ] Modo kiosco / pantalla táctil.
- [ ] Tests E2E de UI (Playwright u similar).
- [ ] Accesibilidad y atajos de teclado.

---

## Decisiones

> Registro corto de decisiones que condicionan el futuro. Formato: fecha — decisión — por qué.

- **2026-11-23 — Secuencias de comprobantes.** `secuencia_comprobante` con `FOR UPDATE` reemplaza `count() + 1`. Motivo: no reutilizar números al borrar y sobrevivir reinicios.
- **2026-11-23 — Kardex como única vía de stock.** `producto.stock` es cache; cada cambio escribe `movimiento_stock` en la misma transacción. Motivo: poder explicar diferencias y calcular costo.
- **2026-11-23 — Categoría como FK.** `producto.categoria_id` es la fuente de verdad; `producto.categoria` queda como cache del nombre para las vistas. Motivo: integridad real sin rehacer todas las pantallas.
- **2026-11-23 — Datos en `~/.mimbre`.** La base, la plantilla y los backups viven en la carpeta del usuario; `LegacyDataMigrator` copia una vez desde `./data` o `target/data`. Motivo: sobrevivir a `mvn clean` y no depender del directorio de ejecución.
- **2026-11-23 — Flyway es el dueño del esquema.** `schema-generate=NONE`; `V1` baseline, `V2` fecha_anulacion y `baseline-on-migrate` para bases existentes. Motivo: migrar sin perder datos y dejar de tocar el esquema a mano.
- **2026-11-23 — Aplicación local, monousuario y sin login.** Se descarga y se ejecuta en la PC del negocio; no hay multiempresa, ni roles, ni pantalla de acceso. Puede existir un usuario predeterminado, pero es transparente para el usuario. Motivo: simplicidad y offline. _No agregar `usuario`, `rol`, `permiso` ni `empresa_id`._
- **2026-11-23 — Sin ORM JPA.** Micronaut Data JDBC directo. Motivo: menos magia, SQL predecible.
- **2026-11-23 — Frontend sin bundler.** Pebble + HTMX + JS vanilla y assets locales. Motivo: offline y cero toolchain de npm.
- **2026-11-23 — Snapshot en detalles de venta/compra.** Se copian nombre y precio. Motivo: la factura histórica no debe cambiar si cambia el producto.

---

## Registro de avances

| Fecha | Qué | Referencia |
|---|---|---|
| 2026-11-24 | Fix Proveedores: email vacío ya no devuelve 400; validación con `@Pattern` que acepta vacío | UI |
| 2026-11-24 | Fix Proveedores: “Nuevo/Editar” quedaba en blanco por `hx-select` sobre un fragmento; se quitó el select | UI |
| 2026-11-23 | Se crea `PROCESS.md` con el traspaso y los próximos pasos | traspaso |
| 2026-11-23 | Dashboard rediseñado: KPIs compactos, evolución con área degradada, composición en 2 doughnuts, ventas vs compras y ranking top | UI |
| 2026-11-23 | UI Venta/Compra: sin título "Detalle de Productos", botón Agregar a la derecha y estilo secundario; total alineado | UI |
| 2026-11-23 | Regla de UI: todos los `<select>` usan `vanilla-select`; clientes, proveedores, productos y condiciones incluidos; modal de producto a 760px | UI |
| 2026-11-23 | **M3 implementado**: `V6` con `secuencia_comprobante`, backfill y uso en ventas/compras; 10 tests | M3 |
| 2026-11-23 | Verificado: dos ventas reales → `001-001-0000001` y `...0002`; secuencia persiste y no reutiliza | M3 |
| 2026-11-23 | **M2 implementado**: `V5` con `movimiento_stock`, `costo_actual`, `stock_minimo`; 49 movimientos iniciales; venta/anulación/ajuste verificados | M2 |
| 2026-11-23 | UI de movimientos por producto y campos de costo/stock mínimo; dashboard usa el mínimo por producto | M2 |
| 2026-11-23 | **M1b implementado**: `V4` con `categoria_id`, backfill de 49 productos, formulario por ID y seed con FK; 6 tests | M1b |
| 2026-11-23 | Verificado: producto con `categoriaId` y con `categoria` legacy → 201; categoría inexistente → 400 | M1b |
| 2026-11-23 | **M1a implementado**: datos en `~/.mimbre`, `V3` con timestamps, unique, checks, FKs e índices, soft delete y 5 tests | M1a |
| 2026-11-23 | Verificado en la base real: SKU duplicado → 400; soft delete → 204 y desaparece del listado; 6 checks y 6 FKs activos | M1a |
| 2026-11-23 | Resuelto el hallazgo de M0: la base ya no vive en `target/data` y `mvn clean` no la borra | M1a |
| 2026-11-23 | Verificación real de M0: base existente migrada con baseline 1 + V2, 49 productos intactos, app en `:8000` y backup generado | M0 |
| 2026-11-23 | **M0 implementado**: Flyway V1/V2 + baseline, 2 tests, `BackupService`, `database_empty` con las 8 tablas. Pendiente revisión | M0 |
| 2026-11-23 | Empaquetado verificado: `target/mimbre-0.1.jar` (22 MB) con Flyway incluido | M0 |
| 2026-11-23 | Hallazgo: con `mn:run` la base queda en `target/data` y `mvn clean` la borra. A resolver antes de M1 | M0 |
| 2026-11-23 | Alcance cerrado: local, monousuario, sin login ni roles. Se elige el subconjunto de tablas del DER para la base | este commit |
| 2026-11-23 | `PROGRESS.md` se reorganiza como snapshot para IA: tabla de estado, protocolo y reglas de frescura | este commit |
| 2026-11-23 | Se crean `AGENTS.md`, `CONTEXT.md` y `PROGRESS.md` con el estado del proyecto | este commit |
| 2026-11-23 | Auditoría del código: 0 tests, sin auth, sin migraciones, numeración por `count()+1` | `20b584f` |

<!--
Plantilla para nuevas entradas:

## Ahora
- [ ] **Título** · `prioridad`
  - Contexto: ...
  - Criterio: ...

## Hecho
- [x] **Título** (fecha, referencia).

## Registro
| 2026-11-23 | Qué se hizo | commit/PR |
-->
