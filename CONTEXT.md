# Contexto del sistema: Mimbre

> Documento descriptivo para dar contexto a una IA (u otra persona) sobre qué es
> este sistema, cómo está construido y cómo se comporta. Para instrucciones de
> trabajo sobre el repositorio, ver `AGENTS.md`.

## 1. Qué es

**Mimbre** es una aplicación de escritorio/local de **gestión comercial para un
pequeño negocio o comercio minorista**, pensada para el mercado paraguayo
(moneda guaraní ₲, RUC, teléfonos `021-...`/`098x-...`, locale `es-PY`). Cubre el
ciclo básico de un comercio:

- Catálogo de **productos** y **categorías**.
- **Clientes** y **proveedores**.
- **Ventas** con descuento automático de stock y anulación con reposición.
- **Compras** con aumento automático de stock y anulación con reducción.
- **Dashboard** con KPIs (ventas del mes, facturas, productos, clientes), gráfico
  de ventas y listado de productos con bajo stock.

No es un SaaS ni una aplicación multiusuario. Se ejecuta **en la máquina del
usuario**, levanta un servidor embebido en `http://localhost:8000`, abre el
navegador y se controla desde un **ícono en la bandeja del sistema** (abrir
navegador / salir). Funciona **offline**: todas las librerías de frontend (HTMX,
Chart.js, FontAwesome, fuentes) están vendorizadas localmente.

## 2. Stack técnico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Micronaut 4.8.0 (`micronaut.runtime=netty`) |
| Persistencia | Micronaut Data JDBC + HikariCP + **H2 en archivo** |
| Migraciones | **Flyway 10** (scripts en `src/main/resources/db/migration`) |
| Vistas | Pebble (renderizado en servidor) |
| Frontend | HTMX + JavaScript vanilla + CSS propio + Chart.js |
| Serialización | Micronaut Serialization (Jackson) |
| Validación | Jakarta Validation |
| Build | Maven Wrapper + `maven-shade-plugin` (JAR único) |
| Distribución | `jpackage` → app-image con JRE incluido (Windows/Linux/macOS) |
| CI | GitHub Actions (matriz Windows/Linux/macOS; release en tags `v*`) |

## 3. Arquitectura

Aplicación **monolítica en capas**, organizada **por feature** (package por
funcionalidad), no por capa global:

```
com.app
├── Application.java          # main: arranque, puerto, bandeja, navegador
├── DatabaseInitializer.java  # crea ./data, esquema extra, seed inicial
├── DemoDataSeeder.java       # datos demo (solo primera ejecución)
├── HomeController.java       # "/" → dashboard
├── <feature>/
│   ├── controller/           # *Controller (API REST) y *UiController (vistas)
│   ├── service/              # lógica de negocio y transacciones
│   ├── repository/           # interfaces Micronaut Data (@JdbcRepository)
│   ├── model/                # entidades @MappedEntity
│   └── dto/                  # DTOs @Serdeable
├── exception/                # excepciones de negocio + handler global
└── utils/DevSeedController   # endpoints de datos masivos (solo desarrollo)
```

Cada feature tiene **dos controladores**:

- `/api/...` → REST JSON (integraciones o consumo programático).
- `/ui/...` → devuelve vistas Pebble completas o fragmentos HTML que HTMX
  inyecta en `#id-main`.

La navegación del sidebar usa `hx-get` + `hx-target="#id-main"` +
`hx-select="#id-main"` + `hx-push-url="true"`. Los formularios se envían como
`application/x-www-form-urlencoded` y el controlador responde con la tabla
actualizada (no JSON). El frontend tiene "managers" JS (`VentaManager`,
`CompraManager`) que mantienen el carrito de ítems en memoria y lo renderizan en
una tabla antes de enviar.

### Capas y responsabilidades

- **Controller**: parseo de request, armado del modelo de la vista o DTO de
  respuesta. No contiene reglas de negocio.
- **Service**: reglas de negocio, validaciones, transacciones, mapeo entidad ↔
  DTO. Es la única capa que otros servicios deberían invocar.
- **Repository**: interfaces Micronaut Data; queries derivadas o `@Query` para
  búsquedas.
- **Model**: entidades `@MappedEntity` con getters/setters manuales.
- **DTO**: objetos `@Serdeable` de entrada/salida; se mapean a mano.

## 4. Modelo de datos

| Entidad | Campos principales |
|---|---|
| `Producto` | id, nombre, sku, precio, stock, **costoActual**, **stockMinimo**, categoria_id (FK) y categoria (nombre cache) |
| `ProductoCategoria` | id, nombre, descripcion |
| `Cliente` | id, nombre, rucDocumento, telefono, email, direccion |
| `Proveedor` | id, nombre, ruc, telefono, email, direccion |
| `Venta` | id, numeroFactura, clienteId, clienteNombre, fecha, condicion, montoTotal, estado, fechaAnulacion |
| `VentaDetalle` | id, ventaId, productoId, productoNombre, cantidad, precioUnitario, subtotal |
| `Compra` | id, numeroFactura, proveedorId, proveedorNombre, fecha, condicion, montoTotal, estado, fechaAnulacion |
| `CompraDetalle` | id, compraId, productoId, productoNombre, cantidad, precioCosto, subtotal |
| `MovimientoStock` | id, productoId, tipo, cantidad, stockAnterior, stockNuevo, costoUnitario, origenTipo, origenId, motivo, creadoEn |
| `SecuenciaComprobante` | id, tipo, prefijo, ultimoNumero, longitud, creadoEn, actualizadoEn |

Notas de diseño:

- Todas las tablas tienen `creado_en` / `actualizado_en`; producto, categoría,
  cliente y proveedor tienen `activo` para soft delete.

- **No hay foreign keys reales** entre entidades; se relacionan por IDs
  (`clienteId`, `productoId`, `ventaId`). La integridad referencial se resuelve
  en los servicios.
- `Producto.categoria_id` es una **FK real** a `ProductoCategoria`; `categoria`
  queda como cache del nombre para las vistas. Al guardar un producto se setean
  ambos.
- Las líneas de venta/compra **duplican nombre y precio** del producto
  ("snapshot" histórico): una factura vieja conserva los valores originales
  aunque el producto cambie después.
- `condicion` es texto: `"Contado"` o `"Crédito"`. `estado` es `"COMPLETADA"` o
  `"ANULADA"`.
- Los montos son `BigDecimal` y la moneda es el guaraní (₲), sin decimales en la
  práctica.

## 5. Reglas de negocio

### Numeracion (`SecuenciaComprobanteService`)

- Cada tipo de comprobante (`VENTA`, `COMPRA`) tiene una fila en
  `secuencia_comprobante` con prefijo, ultimo numero y longitud.
- `siguiente(tipo)` bloquea la fila con `FOR UPDATE`, incrementa y devuelve el
  numero formateado; corre en la misma transaccion del documento.
- Formatos: `001-001-0000000` y `COMP-0000000`.

### Stock (`MovimientoStockService`)

- Todo cambio de existencia pasa por `MovimientoStockService.registrar`:
  actualiza `producto.stock` y guarda una fila en `movimiento_stock` dentro de la
  misma transacción.
- Tipos: `INICIAL`, `VENTA`, `COMPRA`, `ANULACION_VENTA`, `ANULACION_COMPRA`,
  `AJUSTE`. `cantidad` es firmada.
- Las compras recalculan `producto.costo_actual` como costo promedio ponderado.
- `producto.stock_minimo` define el umbral de bajo stock (default 5).

### Ventas (`VentaService.registrarVenta`, `@Transactional`)

1. La venta debe tener al menos un detalle.
2. Antes de tocar nada, valida stock de cada producto; si falta lanza
   `StockInsuficienteException`.
3. Si no se envía número de factura, lo autogenera como `001-001-%07d` usando
   `count() + 1`.
4. Si hay `clienteId`, copia el nombre del cliente; si no, usa
   `"Cliente Ocasional"`.
5. Guarda la venta, luego cada detalle, **descuenta stock**
   (`max(0, stock - cantidad)`) y acumula el total; al final actualiza
   `montoTotal`.
6. **Anular**: marca `estado=ANULADA`, setea `fechaAnulacion` y **repone el
   stock** de todos los detalles.
7. No se puede borrar un producto que aparezca en alguna venta
   (`countByProductoId > 0`); tampoco un cliente con ventas.

### Compras (`CompraService.registrarCompra`, `@Transactional`)

1. Valida que exista el proveedor y que haya detalles.
2. Autogenera `COMP-%07d`.
3. Cada detalle **aumenta el stock** y usa `precioCosto` (si no viene, toma el
   precio de venta del producto).
4. **Anular** una compra **reduce el stock** (con piso 0).
5. No se puede borrar un proveedor con compras.

### Dashboard (`GET /api/dashboard`)

- KPIs: ventas del mes, ventas de hoy, ticket promedio, facturas del mes,
  compras del mes, ganancia estimada, valor de inventario y productos bajo stock.
- Totales: productos, clientes, proveedores y facturas históricas.
- Gráficos Chart.js: evolución de los últimos 14 días (área con degradado),
  composición del mes en dos doughnuts (categoría y contado/crédito) y ventas vs
  compras de los últimos 6 meses.
- Ranking de top 5 productos del mes y tabla de productos bajo stock.
- KPIs compactos en dos filas; los gráficos usan degradados y sombra para dar
  relieve.
- Los cálculos se hacen en memoria sobre ventas con detalle, compras y productos;
  no hay consultas agregadas en SQL todavía.

### Eliminaciones

Se validan por conteo y lanzan `BusinessException` con mensaje en español, que
el frontend muestra como toast.

## 6. Manejo de errores

`GlobalExceptionHandler` captura `Throwable` y distingue:

- Petición de UI (`/ui/**` o header `HX-Request`) → responde JSON con
  `error`/`path` + header `HX-Trigger` con un `showToast` para que el frontend
  muestre el mensaje.
- Petición API → JSON `{timestamp, status, error, message, path}`.

Mapeo: `ResourceNotFoundException` → 404; `StockInsuficienteException`,
`BusinessException`, `ConstraintViolationException`,
`IllegalArgumentException/StateException` → 400; resto → 500.

## 7. Rutas principales

### API REST

- `GET/POST /api/productos`, `GET/PUT/DELETE /api/productos/{id}`,
  `GET /api/productos/{id}/movimientos`
- `GET/POST /api/clientes`, `GET/PUT/DELETE /api/clientes/{id}`
- `GET/POST /api/proveedores`, `GET/PUT/DELETE /api/proveedores/{id}`
- `GET/POST /api/producto-categorias`,
  `GET/PUT/DELETE /api/producto-categorias/{id}`
- `GET/POST /api/ventas`, `GET /api/ventas/{id}`,
  `POST /api/ventas/{id}/anular`
- `GET/POST /api/compras`, `GET /api/compras/{id}`,
  `POST /api/compras/{id}/anular`
- `GET /api/dashboard`
- `GET /api/dev/seed/status`, `POST /api/dev/seed/generar?confirmar=true`,
  `POST /api/dev/seed/limpiar?confirmar=true`

### UI (HTMX/Pebble)

- `/` y `/ui/dashboard` → dashboard
- `/ui/productos/list|form|buscar-producto|movimientos?id=|save|delete/{id}`
- `/ui/categorias/list|form|save|delete/{id}`
- `/ui/proveedores/list|form|buscar-proveedor|save|delete/{id}`
- `/ui/clientes/list|form|buscar-cliente|save|delete/{id}`
- `/ui/ventas/list|form|detalle|save|anular/{id}`
- `/ui/compras/list|form|detalle|buscar-producto|save|anular/{id}`

Listados paginados con `?page=&size=` (default 10) y búsqueda con `?q=` (LIKE
case-insensitive sobre nombre/SKU/RUC). "Reportes" en el sidebar es un enlace
muerto (sin implementar).

## 8. Persistencia y datos iniciales

- Base H2 en archivo **estable**: `~/.mimbre/data/database.mv.db`, URL
  `jdbc:h2:file:${user.home}/.mimbre/data/database;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE`,
  usuario `sa` sin contraseña. No depende del directorio desde el que se ejecute
  y sobrevive a `mvn clean`.
- `LegacyDataMigrator` corre al inicio y, si `~/.mimbre/data` está vacío, copia
  la base, la plantilla y los backups de `./data` o `./target/data`. Nunca borra
  el origen.
- **Esquema versionado con Flyway** (`datasources.default.schema-generate=NONE`):
  - `V1__baseline.sql`: las 8 tablas del MVP.
  - `V2__fecha_anulacion.sql`: columnas `fecha_anulacion` en venta y compra.
  - `V3__integridad.sql`: timestamps, `activo`, unique, checks, foreign keys e
    índices.
  - `V4__categoria_fk.sql`: agrega `producto.categoria_id`, crea las categorías
    que existían solo como texto y enlaza los productos.
  - `V5__kardex.sql`: `movimiento_stock`, `costo_actual`, `stock_minimo` y el
    saldo inicial de cada producto.
  - `V6__secuencias.sql`: `secuencia_comprobante` y backfill desde los numeros
    existentes.
  - `baseline-on-migrate=true` y `baseline-version=1`: una base existente sin
    historial se marca como versión 1 y solo se aplican las migraciones nuevas,
    sin perder datos.
- `DatabaseInitializer` (en `StartupEvent`): usa `~/.mimbre/data`, crea la base
  "plantilla" `database_empty.mv.db` (8 tablas), corre el seed **solo la primera
  vez** (marcador `.initialized`) y al final llama a `BackupService`. Se puede
  desactivar con `app.init.enabled=false` (lo usan los tests).
- `BackupService`: ejecuta `CHECKPOINT` sobre H2 y copia la base a
  `~/.mimbre/backups/database-AAAAmmdd-HHmmss.mv.db`; borra las copias más viejas
  que `app.backup.retention-days` (30 por defecto).
  Propiedades: `app.backup.enabled`, `app.backup.source`, `app.backup.dir`,
  `app.backup.retention-days`.
- `DemoDataSeeder` carga 10 categorías, 10 proveedores, 15 clientes y 49
  productos con precios en guaraníes.
- `DevSeedController` permite generar 100 clientes, 100 productos, 50 compras y
  500 ventas por HTTP, y consultar conteos. **No tiene autenticación**: está
  pensado para desarrollo/demo.

## 9. Ejecución, build y distribución

```bash
./mvnw mn:run          # desarrollo
./mvnw clean package    # JAR (shade) → target/mimbre-0.1.jar
python build.py         # package + jpackage → release/Mimbre (JRE incluido)
```

- `build.py` detecta SO, ejecuta `clean package -DskipTests` y arma un
  **app-image** con `jpackage` (`--name Mimbre`, main class
  `com.app.Application`). Salidas: `release/Mimbre/Mimbre.exe` (Windows),
  `release/Mimbre/bin/Mimbre` (Linux), `release/Mimbre.app` (macOS).
- `Application.main` detecta si corre desde JAR (empaquetado) o IDE, **verifica
  si el puerto 8000 ya está ocupado** (si lo está, avisa y abre el navegador en
  vez de fallar), abre el navegador automáticamente solo en modo empaquetado y
  crea el ícono de bandeja con "Abrir en navegador" y "Salir".
- GitHub Actions compila en los tres SO y publica los artefactos en un release al
  pushear un tag `v*`.

## 10. Convenciones y estilo

- Código, nombres de clases, mensajes de error, vistas y comentarios en
  **español**.
- Paquetes por feature; DTOs separados de entidades; mapeo **manual** (sin
  MapStruct).
- Servicios `@Singleton` con `@Transactional` en escrituras; repositorios
  `@JdbcRepository(dialect = Dialect.H2)` con `@Query` para búsquedas.
- Vistas Pebble con `layout.html` como base (`{% block content %}`), fragmentos
  reutilizables (paginación, modales, selector de tamaño de página).
- Todos los `<select>` usan el componente `vanilla-select`
  (`class="form-control vanilla-select"`); no se usan selects nativos.
- UI: tema claro/oscuro y paletas de color persistidas en `localStorage`
  (`mimbre-theme`, `mimbre-palette`), modales propios, toasts, ripple,
  `vanilla-select`, HTMX para navegación sin recarga.
- Frontend sin bundler ni npm: JS y CSS servidos como estáticos desde
  `src/main/resources/public`.

## 11. Limitaciones y deudas técnicas

- **Aplicación local, monousuario y sin login.** No hay multiempresa,
  autenticación, sesiones ni roles: la app se abre directo en la PC del negocio
  y, si existe, el usuario es predeterminado y transparente. Los endpoints de
  desarrollo están expuestos.
- **Tests básicos** (10): humo, baseline, migración, tres de integridad, seed,
  kardex y dos de numeración (ventas y compras). Todavía
  no hay tests de ventas, compras, cobros ni caja (M2/M5).
- **Soft delete sin interfaz de administración**: los listados solo muestran
  activos; no hay pantalla para ver o reactivar inactivos.
- El stock se escribe solo vía `MovimientoStockService`, pero sin bloqueo
  optimista: en teoría dos ventas simultáneas podrían pisarse (la app es
  monousuario local, así que hoy no ocurre).
- La numeración ya usa `secuencia_comprobante`; un numero personalizado enviado
  por API no sincroniza la secuencia.
- `Producto.categoria` sigue existiendo como cache del nombre; la fuente de
  verdad es `categoria_id`. Hay que mantener ambos sincronizados al guardar.
- `movimiento_stock` es el historial del stock: solo se inserta. `producto.stock`
  es un cache que se actualiza en la misma transacción que el movimiento.
- Dashboard con lógica de fallback discutible y agrupación por `dd/MM` que mezcla
  años.
- Enlace "Reportes" sin implementar; no hay exportación a PDF/Excel ni impresión
  de facturas.
- Los listados de ventas/compras se cargan completos en memoria para el
  dashboard y los filtros (no hay consultas agregadas en SQL).
- `DevSeedController.limpiar` está incompleto (solo devuelve conteos, no borra).

## 12. Resumen en una frase

> Mimbre es una aplicación de escritorio offline para la gestión comercial de un
> pequeño negocio paraguayo, construida con Java 21 + Micronaut 4.8 + H2 +
> Pebble/HTMX, que administra productos, categorías, clientes, proveedores,
> ventas y compras con control de stock, y se distribuye como ejecutable
> multiplataforma con JRE incluido.
