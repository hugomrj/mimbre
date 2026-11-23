# PLAN.md — Plan de trabajo por hitos

> Cómo se construye Mimbre, en qué orden y **cómo se revisa cada avance**.
> Estado diario: `PROGRESS.md`. Qué es el sistema: `CONTEXT.md`. Reglas de
> trabajo: `AGENTS.md`.

**Objetivo:** completar Mimbre como aplicación **local, monousuario y sin login**
para una myPyme, con inventario confiable, caja, cobros y compras a crédito,
gastos y reportes. **Sin facturación electrónica.**

---

## 1. Alcance

**Entra en este plan**

- Base de datos versionada con migraciones (Flyway).
- Integridad: timestamps, unique, checks, foreign keys, soft delete.
- Kardex de stock (`movimiento_stock`) y costo actual.
- Numeración segura de comprobantes.
- Caja: apertura, cierre y arqueo simple.
- Cobros y cuentas por cobrar (ventas a crédito).
- Compras a crédito y cuentas por pagar.
- Gastos por categoría.
- Devoluciones parciales.
- Reportes, exportación CSV y backup/restore.
- Tests de la lógica crítica.

**No entra**

- Multiempresa, sucursal, multi-depósito.
- Login, usuarios, roles y permisos.
- Contabilidad (plan de cuentas, asientos, ejercicios).
- Facturación electrónica (SIFEN, CDC, timbrado como entidad).
- Preventa (cotización, pedido, remisión).
- Moneda extranjera, cheques y conciliación bancaria.
- Impuesto/IVA por línea (queda como opción posterior).

---

## 2. Principios

1. **Un hito = un cambio revisable.** Cada hito deja la app funcionando; no se
   mezclan dos.
2. **Nada se da por hecho sin demo.** Un hito termina cuando el humano recorre
   su checklist, no cuando compila.
3. **Migraciones, nunca ALTER a mano.** Todo cambio de esquema es una migración
   Flyway versionada.
4. **El stock se explica.** Todo cambio de existencia deja un
   `movimiento_stock`; `producto.stock` es solo un cache.
5. **Tests primero en lo crítico.** Ventas, compras, anulaciones, cobros y caja
   no se tocan sin test que los cubra.
6. **Sin sorpresas.** Si un hito crece, se parte y se avisa antes de seguir.

---

## 3. Cómo se revisa cada avance

### Protocolo

1. El agente implementa **un solo hito** y entrega el resumen con la plantilla
   de abajo.
2. El humano levanta la app y recorre la **Demo** del hito (30–60 min).
3. Marca el resultado en el **Registro de revisiones** (§6):
   `Aprobado` / `Con observaciones` / `Rechazado`.
4. Si hay observaciones: se corrigen **dentro del mismo hito** y se vuelve a
   revisar. No se abre el siguiente.
5. Si se aprueba: se actualiza `PROGRESS.md`, se commitea y se etiqueta
   `m0`, `m1`, … (opcional, pero recomendado).

### Regla de corte

Un hito **no** está terminado si:

- `./mvnw test` falla,
- la app no arranca desde cero,
- la migración no aplica sobre una base existente sin perder datos,
- o falta actualizar `PROGRESS.md`.

### Plantilla de entrega (la completa el agente)

```markdown
## Entrega Mx — <nombre>

**Qué cambió**
- ...

**Migración**
- Archivo: Vx__....sql
- Aplica sobre base existente: sí/no (cómo se probó)

**Cómo probar**
1. `./mvnw clean test`
2. `./mvnw mn:run`
3. ...

**Checklist de demo**
- [ ] ...
- [ ] ...

**Tests agregados**
- ...

**Pendientes / observaciones**
- ...
```

---

## 4. Resumen de hitos

| Hito | Nombre | Resultado visible | Esfuerzo | Depende de |
|---|---|---|---|---|
| **M0** | Base segura | Migraciones + tests + backup | 2–3 d | — |
| **M1** | Integridad | Datos sin duplicados ni huérfanos | 2–3 d | M0 |
| **M2** | Kardex | Todo movimiento de stock queda registrado | 3–4 d | M1 |
| **M3** | Numeración | Facturas y recibos sin números repetidos | 1–2 d | M1 |
| **M4** | Caja | Abrir/cerrar turno y arquear | 3–4 d | M2 |
| **M5** | Cobranzas | Vender a crédito y cobrar en cuotas | 4–5 d | M4 |
| **M6** | Compras a crédito | Pagar proveedores y ver vencimientos | 2–3 d | M5 |
| **M7** | Gastos | Ver ganancia real, no solo ventas | 2–3 d | M4 |
| **M8** | Devoluciones | Devolver parte de una venta | 3 d | M2, M5 |
| **M9** | Reportes y release | Cierre de versión `v0.2` | 3–4 d | M0–M8 |

> Esfuerzo en días de trabajo efectivo, no calendario. Total: ~25–32 días.

---

## 5. Detalle de cada hito

### M0 — Base segura

**Objetivo:** que el esquema sea reproducible y que exista red de tests antes de
tocar el dominio.

**Tareas**
- [x] Agregar Flyway y crear `V1__baseline.sql` con el esquema actual
      (`baseline-on-migrate` para bases existentes).
- [x] Agregar `micronaut-test` (JUnit 5) y un test de humo que levante el
      contexto.
- [x] Completar `database_empty` con `COMPRA` y `COMPRA_DETALLE` (hoy faltan).
- [x] Backup automático: copiar `./data/database.mv.db` a `./backups/` al
      arrancar (con timestamp y retención de 7–30 días).
- [x] Documentar el flujo de migración en `CONTEXT.md`.

**Demo**
1. Borrar la base (`target/data` si usás `mn:run`, `./data` si corrés el JAR),
   arrancar: se crea la base con Flyway y los datos demo.
2. Con una base existente, arrancar: no se pierde nada y Flyway registra
   `baseline` + `V2`.
3. `./mvnw test` corre y pasa.
4. Aparece un archivo en `backups/`.

**Aceptación:** base nueva y base existente funcionan; hay al menos 1 test; el
backup se genera solo.

**Observación de la revisión (M0):** con `./mvnw mn:run` la base y los backups
quedan en `target/`, y `mvn clean` los borra. Hay que definir una ubicación
estable antes de M1.

**Riesgo:** baseline mal puesto → migraciones duplicadas. Mitigación: probar con
una copia de `./data` antes de tocar la real.

---

### M1 — Integridad

**Objetivo:** que la base impida datos inválidos, no solo la aplicación.

**Tareas**
- [x] Definir una ubicación estable para la base (`~/.mimbre/data`) y migrar la
      base existente sin perder datos.
- [x] `creado_en` / `actualizado_en` en todas las tablas.
- [x] Unique: `producto.sku`, `cliente.ruc_documento`, `proveedor.ruc`,
      `venta.numero_factura`, `compra.numero_factura`.
- [x] Checks: `cantidad > 0`, `stock >= 0`, `precio >= 0`, `monto_total >= 0`.
- [x] Foreign keys de detalles y cabeceras, tras limpiar huérfanos.
- [x] Índices: fechas, `cliente_id`, `producto_id`, `venta_id`, `compra_id`.
- [x] `activo` en producto, cliente, proveedor y categoría; los listados filtran
      activos (no hay toggle para ver inactivos todavía).
- [x] `producto.categoria_id` → `PRODUCTO_CATEGORIA` (migrar el string actual).

**Demo**
- Cargar dos productos con el mismo SKU → error 400 claro.
- Desactivar un producto con ventas → desaparece del catálogo, la venta conserva
  el nombre y el historial.
- Desactivar un cliente → desaparece de los selects de venta, pero su historial
  sigue visible.
- Guardar un producto con una categoría inexistente → error claro.
- Los listados no muestran inactivos por defecto.

**Aceptación:** las constraints están en la base (no solo en Java); no quedan
huérfanos; los tests siguen verdes; la base vive en `~/.mimbre` y sobrevive a
`mvn clean`.

---

### M2 — Kardex

**Objetivo:** poder explicar cada unidad de stock y conocer el costo.

**Tareas**
- [x] Tabla `movimiento_stock` (producto, tipo, cantidad, stock_anterior,
      stock_nuevo, costo_unitario, origen_tipo, origen_id, motivo, fecha).
- [x] Backfill: un movimiento `INICIAL` por producto con el stock actual.
- [x] `VentaService` y `CompraService` escriben movimiento en la misma
      transacción; anular genera el movimiento inverso.
- [x] `producto.costo_actual`: costo promedio ponderado en cada compra.
- [x] `producto.stock_minimo`; el dashboard deja de usar `<= 5` fijo.
- [x] UI: campos nuevos en el formulario de producto y pestaña "Movimientos"
      en el detalle.

**Demo**
- Vender 2 unidades → movimiento `VENTA` y stock baja.
- Anular la venta → movimiento `ANULACION_VENTA` y stock vuelve.
- Comprar 10 → movimiento `COMPRA` y costo actualizado.
- Ajustar stock manualmente → movimiento `AJUSTE` con motivo.
- La suma de movimientos coincide con `producto.stock`.

**Aceptación:** no existe ningún camino que cambie stock sin movimiento;
`stock = SUM(movimientos)` para todos los productos.

---

### M3 — Numeración

**Objetivo:** eliminar duplicados por `count() + 1`.

**Tareas**
- [x] Tabla `secuencia_comprobante` (tipo, prefijo, último número).
- [x] Migrar el contador desde el máximo número existente.
- [x] `VentaService` y `CompraService` usan la secuencia.
- [x] Unique en `numero_factura` (hecho en M1).

**Demo**
- Crear 3 ventas, borrar la del medio, crear otra → no repite número.
- Reiniciar la app → la secuencia continúa.

**Aceptación:** no se puede insertar un número repetido; la secuencia sobrevive
reinicios.

---

### M4 — Caja

**Objetivo:** controlar el efectivo del día.

**Tareas**
- [ ] `caja_sesion` (apertura, cierre, montos, diferencia, estado).
- [ ] `caja_movimiento` (ingreso/egreso/retiro/depósito, concepto, monto).
- [ ] UI: abrir caja, cerrar caja con arqueo simple (declarado vs sistema),
      registrar retiro/ingreso manual.
- [ ] Venta de contado en efectivo → ingreso automático en caja.
- [ ] Compra de contado en efectivo → egreso automático.
- [ ] Bloquear ventas en efectivo si no hay caja abierta (o advertir).

**Demo**
- Abrir caja con 500.000.
- Vender 2 productos en efectivo.
- Registrar un retiro.
- Cerrar caja: el sistema calcula el esperado y muestra la diferencia contra lo
  declarado.

**Aceptación:** una sola sesión abierta por vez; el cierre cuadra con los
movimientos; la diferencia queda registrada.

---

### M5 — Cobranzas

**Objetivo:** vender a crédito y cobrar total o parcialmente.

**Tareas**
- [ ] `forma_pago` (efectivo, transferencia, tarjeta, cheque).
- [ ] `cuenta_cobrar` 1:1 con venta a crédito (saldo, vencimiento, estado).
- [ ] `cobro` + `cobro_aplicacion` (un cobro puede cubrir varias deudas).
- [ ] UI: cobrar desde la venta o desde "Cobranzas"; ver saldo y vencimiento.
- [ ] Pago en efectivo → movimiento de caja; transferencia → no afecta caja.
- [ ] Alertas de vencidas en el dashboard.

**Demo**
- Vender a crédito 1.000.000.
- Cobrar 400.000 en efectivo → saldo 600.000 y caja +400.000.
- Cobrar el resto por transferencia → saldo 0 y caja sin cambios.
- Intentar cobrar de más → bloqueado.

**Aceptación:** el saldo nunca queda negativo; la suma de aplicaciones nunca
supera el cobro; los tests cubren pago parcial y total.

---

### M6 — Compras a crédito

**Objetivo:** deberle al proveedor y pagarle.

**Tareas**
- [ ] `cuenta_pagar` 1:1 con compra a crédito.
- [ ] `pago_proveedor` (monto, fecha, forma de pago, caja si es efectivo).
- [ ] UI: vencimientos, saldo y pago desde la compra.
- [ ] Compra de contado en efectivo descuenta caja; a crédito, no.

**Demo**
- Comprar a crédito 2.000.000.
- Pagar 800.000.
- Ver saldo 1.200.000 y vencimiento.
- Pagar el resto.

**Aceptación:** mismo criterio que M5, del lado de proveedores.

---

### M7 — Gastos

**Objetivo:** saber la ganancia real del negocio.

**Tareas**
- [ ] `categoria_gasto` y `gasto` (fecha, categoría, descripción, monto, forma
      de pago, caja si es efectivo).
- [ ] UI: alta/listado de gastos y categorías.
- [ ] Dashboard: `ganancia = ventas - costo de ventas - gastos` del período.
- [ ] Los gastos en efectivo generan egreso de caja.

**Demo**
- Cargar alquiler y sueldos del mes.
- Ver que el dashboard descuente esos gastos y muestre la ganancia.
- Ver el egreso reflejado en el cierre de caja.

**Aceptación:** el total de gastos coincide con la suma de movimientos de caja
de tipo egreso-gasto; el dashboard no cuenta dos veces.

---

### M8 — Devoluciones

**Objetivo:** devolver parte de una venta sin anularla entera.

**Tareas**
- [ ] `devolucion` + `devolucion_detalle`.
- [ ] Reponer stock con movimiento `ENT` y motivo.
- [ ] Devolución en efectivo → egreso de caja; a cuenta → saldo a favor o
      reducción de `cuenta_cobrar`.
- [ ] UI: devolver desde el detalle de la venta, indicando ítems y cantidades.
- [ ] No permitir devolver más de lo vendido ni dos veces lo mismo.

**Demo**
- Vender 3 unidades.
- Devolver 1 en efectivo → stock +1, caja −precio, venta sigue completada.
- Intentar devolver 4 → bloqueado.

**Aceptación:** stock y saldo consistentes; la devolución queda auditada.

---

### M9 — Reportes y release

**Objetivo:** cerrar `v0.2` con información útil y datos protegidos.

**Tareas**
- [ ] Dashboard con consultas agregadas en SQL (no cargar todo en memoria).
- [ ] Reportes: ventas por período, productos más vendidos, cobranzas, caja,
      gastos, cuentas por cobrar/pagar.
- [ ] Exportación CSV de los reportes principales.
- [ ] Backup/restore desde la UI (elegir carpeta, restaurar con confirmación).
- [ ] Revisar `database_empty`, seed y migraciones desde cero.
- [ ] Actualizar `README.md`, `CONTEXT.md`, `PROGRESS.md` y este plan.
- [ ] `python build.py` y prueba del ejecutable en el SO objetivo.
- [ ] Tag `v0.2`.

**Demo**
- Recorrer cada reporte con datos reales.
- Exportar un CSV y abrirlo.
- Hacer backup, borrar la base, restaurar y verificar que todo vuelve.
- Instalar el ejecutable generado y usarlo sin el IDE.

**Aceptación:** la app empaquetada funciona; los reportes cuadran con la base;
el restore recupera los datos.

---

## 6. Registro de revisiones

Completar al terminar cada hito.

| Hito | Fecha | Resultado | Observaciones | Aprobado por |
|---|---|---|---|---|
| M0 | 2026-11-23 | Aprobado | Observación de ubicación resuelta en M1 | |
| M1 | 2026-11-23 | Aprobado | Integridad completa; se continuó con M2 | |
| M2 | 2026-11-23 | Aprobado | Kardex verificado; se continuó con M3 | |
| M3 | 2026-11-23 | En revisión | Secuencias VENTA/COMPRA, backfill, bloqueo FOR UPDATE y tests de no reutilización | |
| M4 | | Pendiente | | |
| M5 | | Pendiente | | |
| M6 | | Pendiente | | |
| M7 | | Pendiente | | |
| M8 | | Pendiente | | |
| M9 | | Pendiente | | |

**Resultados válidos:** `Pendiente`, `En revisión`, `Aprobado`, `Con observaciones`, `Rechazado`.

---

## 7. Riesgos y mitigaciones

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Perder datos al migrar | Alto | Backup automático antes de cada migración + probar sobre copia. |
| El alcance crece | Alto | Todo lo nuevo va a `PROGRESS.md` → Backlog; no entra al hito en curso. |
| Sin tests, romper ventas | Alto | M0 instala el harness; cada hito agrega tests de su lógica. |
| Stock inconsistente | Alto | Un solo camino de escritura (`movimiento_stock`) y test de suma. |
| H2 bloqueada | Medio | Cerrar la app antes de migrar; `AUTO_SERVER` solo para una instancia. |
| El ejecutable pesa/falla | Medio | Probar `build.py` en M9 y en cada release. |

---

## 8. Qué pedirle a la IA en cada sesión

> Implementá el hito **Mx** de `PLAN.md`. Leé `PROGRESS.md` y `AGENTS.md` antes
> de tocar nada. No avances al hito siguiente. Al terminar, entregá el resumen
> con la plantilla de `PLAN.md` §3 y actualizá `PROGRESS.md`.

Si algo del hito no cierra, la IA debe **parar y preguntar** antes de inventar
una solución que cambie el alcance.
