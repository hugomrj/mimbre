# PROCESS.md — Traspaso de sesión

> Documento para **retomar el trabajo en otra sesión**. Responde: dónde estamos,
> qué quedó hecho, qué falta y cómo continuar.
> Contexto: `CONTEXT.md`. Plan por hitos: `PLAN.md`. Estado diario: `PROGRESS.md`.
> Reglas: `AGENTS.md`.

**Fecha de corte:** 2026-11-23 (America/Asunción)
**Rama:** `main` · **Commit base:** `20b584f` · **Working tree:** 59 cambios sin commitear
**App:** corriendo en `http://localhost:8000` (una sola instancia)
**Base:** `~/.mimbre/data/database.mv.db` · **Backups:** `~/.mimbre/backups/`

---

## 1. Resumen en 30 segundos

Mimbre es una aplicación **local, monousuario y sin login** de gestión comercial
para una myPyme paraguaya. Hoy funciona de punta a punta: productos, categorías,
clientes, proveedores, ventas, compras, kardex de stock, secuencias de
comprobantes y dashboard.

- **Hitos M0–M2:** aprobados y verificados.
- **M3 (numeración):** implementado y verificado; pendiente de aprobación formal.
- **Extras posteriores:** dashboard ampliado/compacto y varias correcciones de UI
  (navegación Ventas/Compras, `vanilla-select`, modales de producto, layout de venta).
- **Sin commit:** todo el trabajo está en el working tree. **Riesgo principal.**

---

## 2. Qué está hecho

### Base de datos y calidad (M0/M1)
- Flyway como dueño del esquema (`schema-generate=NONE`).
- Migraciones **V1 a V6**:
  - V1 baseline (8 tablas).
  - V2 `fecha_anulacion`.
  - V3 integridad: timestamps, `activo`, unique, checks, FKs, índices.
  - V4 `producto.categoria_id` con backfill y FK.
  - V5 kardex: `movimiento_stock`, `costo_actual`, `stock_minimo`.
  - V6 `secuencia_comprobante`.
- Datos movidos de `./data`/`target/data` a **`~/.mimbre/data`**.
- `BackupService`: `CHECKPOINT` + copia a `~/.mimbre/backups`, retención 30 días.
- Soft delete en producto, cliente y proveedor; listados filtran activos.
- Errores de integridad devueltos como HTTP 400.

### Inventario y ventas (M2/M3)
- `MovimientoStockService` es la única vía de escritura del stock.
- Venta, compra, anulación y ajuste manual dejan fila en `movimiento_stock`.
- Compras recalculan costo promedio ponderado.
- Dashboard de bajo stock usa `stock_minimo` por producto.
- Secuencias `VENTA` (`001-001-0000000`) y `COMPRA` (`COMP-0000000`) con `FOR UPDATE`; no reutilizan números al borrar.

### Dashboard
- 8 KPIs: ventas del mes, ventas de hoy, ticket promedio, facturas del mes, compras del mes, ganancia estimada, valor de inventario y bajo stock.
- Gráficos: evolución de 14 días con área degradada, composición del mes en dos doughnuts (categoría y contado/crédito), ventas vs compras de 6 meses.
- Ranking de top 5 productos y tabla de bajo stock.
- `/api/dashboard` devuelve 25 campos calculados.

### UI
- Sidebar Ventas/Compras corregido (el `hx-select` rompía la carga de fragmentos).
- **Regla:** todos los `<select>` usan `vanilla-select`.
- Selectores de cliente, proveedor, producto y condición migrados a `vanilla-select`.
- Modal de producto más ancho (`760px`).
- Formulario de venta/compra: sin título “Detalle de Productos”, botón Agregar a la derecha y secundario, total alineado.

### Tests
- **10 tests**, última corrida completa: 10/10.
  - humo de contexto, baseline Flyway, migración legacy, 3 de integridad, seed, kardex, 2 de numeración.
- Después de esa corrida hubo cambios de dashboard/UI; **volver a correr tests al retomar**.

---

## 3. Estado real de la base demo

| Dato | Valor |
|---|---|
| Productos | 49 |
| Clientes activos | 15 |
| Ventas | 1 (₲ 150.000) |
| Movimientos de stock | 50 (49 iniciales + 1 venta) |
| Secuencia VENTA | 1 |
| Secuencia COMPRA | 0 |
| Migración actual | V6 |

**No borrar esa venta ni la base**: puede ser data de prueba del humano.

---

## 4. Qué falta

### Inmediato (antes de seguir)
1. **Revisar y commitear** todo el working tree. Hay 59 archivos modificados/nuevos sin commit.
2. Aprobar formalmente **M3** (o registrar observaciones).
3. Decidir prioridad: **M4 Caja** o **UX de venta completa**.

### UX de venta pendiente (no está en un hito formal)
- Consumidor final / venta sin cliente.
- Forma de pago (efectivo, transferencia, tarjeta) y **vuelto**.
- Descuento por línea o global.
- Vencimiento y cuenta por cobrar para ventas a crédito (se solapa con M5).
- Imprimir/PDF de la factura.
- Atajos de teclado (`F2` producto, `F4` cobrar).
- URLs directas `/ui/ventas/list` y `/ui/ventas/form` renderizan solo el fragmento (sin layout).

### Hitos siguientes (ver `PLAN.md`)
- **M4 — Caja:** `caja_sesion`, `caja_movimiento`, apertura/cierre, arqueo simple, ventas en efectivo.
- **M5 — Cobranzas:** `forma_pago`, `cuenta_cobrar`, `cobro`, `cobro_aplicacion`.
- **M6 — Compras a crédito:** `cuenta_pagar`, `pago_proveedor`.
- **M7 — Gastos:** `gasto`, `categoria_gasto`, ganancia real en dashboard.
- **M8 — Devoluciones:** parciales, reposición de stock y saldo.
- **M9 — Reportes y release:** consultas agregadas, CSV, restore de backup, `v0.2`.

### Backlog / deuda
- Costos en 0 en la base demo: valor de inventario y ganancia estimada salen 0 o inflados.
- Dashboard calcula en memoria (sin `SUM`/`GROUP BY` en SQL).
- Stock sin bloqueo optimista (la app es monousuario local).
- Número de factura personalizado por API no sincroniza la secuencia.
- Impuesto/IVA, multi-depósito, cotizaciones y facturación electrónica: **fuera de alcance**.

---

## 5. Cómo retomar

### Leer, en este orden
1. `PROCESS.md` (este archivo).
2. `PROGRESS.md` (snapshot y registro).
3. `PLAN.md` (hitos y criterios de revisión).
4. `AGENTS.md` (reglas y convenciones).
5. `CONTEXT.md` (arquitectura y dominio).

### Comandos
```bash
cd /home/hugo/dev/java/micronaut/mimbre

# JDK 21 (el default del sistema es 25)
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# Ver si ya hay una instancia
ss -ltnp | grep :8000

# Tests
./mvnw test

# Desarrollo
./mvnw mn:run
```

> **No ejecutes `package`/`clean` con la app corriendo**: la recarga en caliente
> puede corromper `target/classes`. Pará la app primero.

### Estado de la app
- Si querés verla: abrí http://localhost:8000.
- Para detenerla: bandeja del sistema → “Salir”, o matar el proceso `com.app.Application`.
- Para levantarla de nuevo: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw mn:run`.

### Entorno del agente
- En esta sesión, la app necesitó permiso **`danger-full-access`** para escribir
  en `~/.mimbre` (fuera del workspace). Un humano normal no tiene esa restricción.
- Los tests usan H2 en memoria; no tocan `~/.mimbre`.

---

## 6. Decisiones clave (no reabrir sin motivo)

- **Local, monousuario, sin login**, sin multiempresa ni roles.
- **Sin facturación electrónica** en este proyecto.
- **Datos en `~/.mimbre`**, backups incluidos.
- **Flyway** versiona el esquema; nada de `ALTER` a mano.
- **Kardex**: `producto.stock` es cache; la verdad es `movimiento_stock`.
- **Soft delete** en maestros (`activo=false`).
- **Secuencias** con `FOR UPDATE`, no `count() + 1`.
- **Categoría** con FK (`categoria_id`) y nombre cache (`categoria`).
- **Todos los `<select>`** usan `vanilla-select`.
- **Snapshot** de nombre/precio en detalles de venta/compra.

---

## 7. Riesgos y cuidados

1. **Trabajo sin commit.** Es el riesgo más alto: 59 cambios. Commitear antes de seguir.
2. **App corriendo** en `:8000`; no empaquetar con la app viva.
3. **No tocar `~/.mimbre` a mano** sin backup; hay una venta real.
4. **Costos en 0** en la base demo: no confundir con un bug del dashboard.
5. **`dependency-reduced-pom.xml`** está modificado por el shade plugin; es generado.

---

## 8. Prompt para la próxima sesión

> Leé `PROCESS.md`, `PROGRESS.md`, `PLAN.md` y `AGENTS.md`.
> Decime en qué estado estamos y cuál es la próxima acción.
> Primero ayudame a revisar y commitear los cambios pendientes.
> Después seguí con **M4 — Caja**, salvo que yo pida priorizar la UX de venta.
> Un hito a la vez: no mezcles dos. No toques `~/.mimbre` sin backup.
