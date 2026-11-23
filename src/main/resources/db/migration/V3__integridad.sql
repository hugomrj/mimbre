-- V3: integridad de datos, auditoria basica y soft delete.
-- Se puede aplicar sobre una base existente: limpia huerfanos antes de crear
-- las foreign keys y usa IF NOT EXISTS para no fallar si algo ya existe.

-- ---------------------------------------------------------------------------
-- 1. Timestamps de creacion y actualizacion
-- ---------------------------------------------------------------------------
ALTER TABLE producto ADD COLUMN IF NOT EXISTS creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE producto ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE producto_categoria ADD COLUMN IF NOT EXISTS creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE producto_categoria ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE proveedor ADD COLUMN IF NOT EXISTS creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE proveedor ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE venta ADD COLUMN IF NOT EXISTS creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE venta ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE venta_detalle ADD COLUMN IF NOT EXISTS creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE venta_detalle ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE compra ADD COLUMN IF NOT EXISTS creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE compra ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE compra_detalle ADD COLUMN IF NOT EXISTS creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE compra_detalle ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

UPDATE producto SET creado_en = CURRENT_TIMESTAMP WHERE creado_en IS NULL;
UPDATE producto SET actualizado_en = CURRENT_TIMESTAMP WHERE actualizado_en IS NULL;
UPDATE producto_categoria SET creado_en = CURRENT_TIMESTAMP WHERE creado_en IS NULL;
UPDATE producto_categoria SET actualizado_en = CURRENT_TIMESTAMP WHERE actualizado_en IS NULL;
UPDATE cliente SET creado_en = CURRENT_TIMESTAMP WHERE creado_en IS NULL;
UPDATE cliente SET actualizado_en = CURRENT_TIMESTAMP WHERE actualizado_en IS NULL;
UPDATE proveedor SET creado_en = CURRENT_TIMESTAMP WHERE creado_en IS NULL;
UPDATE proveedor SET actualizado_en = CURRENT_TIMESTAMP WHERE actualizado_en IS NULL;
UPDATE venta SET creado_en = CURRENT_TIMESTAMP WHERE creado_en IS NULL;
UPDATE venta SET actualizado_en = CURRENT_TIMESTAMP WHERE actualizado_en IS NULL;
UPDATE venta_detalle SET creado_en = CURRENT_TIMESTAMP WHERE creado_en IS NULL;
UPDATE venta_detalle SET actualizado_en = CURRENT_TIMESTAMP WHERE actualizado_en IS NULL;
UPDATE compra SET creado_en = CURRENT_TIMESTAMP WHERE creado_en IS NULL;
UPDATE compra SET actualizado_en = CURRENT_TIMESTAMP WHERE actualizado_en IS NULL;
UPDATE compra_detalle SET creado_en = CURRENT_TIMESTAMP WHERE creado_en IS NULL;
UPDATE compra_detalle SET actualizado_en = CURRENT_TIMESTAMP WHERE actualizado_en IS NULL;

-- ---------------------------------------------------------------------------
-- 2. Soft delete
-- ---------------------------------------------------------------------------
ALTER TABLE producto ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT TRUE NOT NULL;
ALTER TABLE producto_categoria ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT TRUE NOT NULL;
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT TRUE NOT NULL;
ALTER TABLE proveedor ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT TRUE NOT NULL;

UPDATE producto SET activo = TRUE WHERE activo IS NULL;
UPDATE producto_categoria SET activo = TRUE WHERE activo IS NULL;
UPDATE cliente SET activo = TRUE WHERE activo IS NULL;
UPDATE proveedor SET activo = TRUE WHERE activo IS NULL;

-- ---------------------------------------------------------------------------
-- 3. Normalizacion previa a los indices unicos
-- ---------------------------------------------------------------------------
UPDATE cliente SET ruc_documento = NULL WHERE ruc_documento IS NOT NULL AND TRIM(ruc_documento) = '';
UPDATE proveedor SET ruc = NULL WHERE ruc IS NOT NULL AND TRIM(ruc) = '';
UPDATE producto SET sku = NULL WHERE sku IS NOT NULL AND TRIM(sku) = '';

-- ---------------------------------------------------------------------------
-- 4. Limpieza de huerfanos (para poder crear las foreign keys)
-- ---------------------------------------------------------------------------
UPDATE venta SET cliente_id = NULL
WHERE cliente_id IS NOT NULL AND cliente_id NOT IN (SELECT id FROM cliente);
UPDATE compra SET proveedor_id = NULL
WHERE proveedor_id IS NOT NULL AND proveedor_id NOT IN (SELECT id FROM proveedor);

DELETE FROM venta_detalle WHERE venta_id IS NULL OR venta_id NOT IN (SELECT id FROM venta);
DELETE FROM compra_detalle WHERE compra_id IS NULL OR compra_id NOT IN (SELECT id FROM compra);

UPDATE venta_detalle SET producto_id = NULL
WHERE producto_id IS NOT NULL AND producto_id NOT IN (SELECT id FROM producto);
UPDATE compra_detalle SET producto_id = NULL
WHERE producto_id IS NOT NULL AND producto_id NOT IN (SELECT id FROM producto);

-- ---------------------------------------------------------------------------
-- 5. Indices unicos
-- ---------------------------------------------------------------------------
CREATE UNIQUE INDEX IF NOT EXISTS ux_producto_sku ON producto(sku);
CREATE UNIQUE INDEX IF NOT EXISTS ux_cliente_ruc ON cliente(ruc_documento);
CREATE UNIQUE INDEX IF NOT EXISTS ux_proveedor_ruc ON proveedor(ruc);
CREATE UNIQUE INDEX IF NOT EXISTS ux_venta_numero ON venta(numero_factura);
CREATE UNIQUE INDEX IF NOT EXISTS ux_compra_numero ON compra(numero_factura);

-- ---------------------------------------------------------------------------
-- 6. Checks de valores
-- ---------------------------------------------------------------------------
ALTER TABLE producto ADD CONSTRAINT IF NOT EXISTS chk_producto_precio CHECK (precio IS NULL OR precio >= 0);
ALTER TABLE producto ADD CONSTRAINT IF NOT EXISTS chk_producto_stock CHECK (stock IS NULL OR stock >= 0);
ALTER TABLE venta ADD CONSTRAINT IF NOT EXISTS chk_venta_total CHECK (monto_total IS NULL OR monto_total >= 0);
ALTER TABLE compra ADD CONSTRAINT IF NOT EXISTS chk_compra_total CHECK (monto_total IS NULL OR monto_total >= 0);
ALTER TABLE venta_detalle ADD CONSTRAINT IF NOT EXISTS chk_venta_detalle_cantidad CHECK (cantidad IS NULL OR cantidad > 0);
ALTER TABLE compra_detalle ADD CONSTRAINT IF NOT EXISTS chk_compra_detalle_cantidad CHECK (cantidad IS NULL OR cantidad > 0);

-- ---------------------------------------------------------------------------
-- 7. Foreign keys
-- ---------------------------------------------------------------------------
ALTER TABLE venta_detalle ADD CONSTRAINT IF NOT EXISTS fk_venta_detalle_venta
    FOREIGN KEY (venta_id) REFERENCES venta(id) ON DELETE CASCADE;
ALTER TABLE venta_detalle ADD CONSTRAINT IF NOT EXISTS fk_venta_detalle_producto
    FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE RESTRICT;
ALTER TABLE compra_detalle ADD CONSTRAINT IF NOT EXISTS fk_compra_detalle_compra
    FOREIGN KEY (compra_id) REFERENCES compra(id) ON DELETE CASCADE;
ALTER TABLE compra_detalle ADD CONSTRAINT IF NOT EXISTS fk_compra_detalle_producto
    FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE RESTRICT;
ALTER TABLE venta ADD CONSTRAINT IF NOT EXISTS fk_venta_cliente
    FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE SET NULL;
ALTER TABLE compra ADD CONSTRAINT IF NOT EXISTS fk_compra_proveedor
    FOREIGN KEY (proveedor_id) REFERENCES proveedor(id) ON DELETE SET NULL;

-- ---------------------------------------------------------------------------
-- 8. Indices de consulta
-- ---------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS ix_producto_categoria ON producto(categoria);
CREATE INDEX IF NOT EXISTS ix_producto_activo ON producto(activo);
CREATE INDEX IF NOT EXISTS ix_cliente_activo ON cliente(activo);
CREATE INDEX IF NOT EXISTS ix_cliente_nombre ON cliente(nombre);
CREATE INDEX IF NOT EXISTS ix_proveedor_activo ON proveedor(activo);
CREATE INDEX IF NOT EXISTS ix_proveedor_nombre ON proveedor(nombre);
CREATE INDEX IF NOT EXISTS ix_venta_fecha ON venta(fecha);
CREATE INDEX IF NOT EXISTS ix_venta_cliente ON venta(cliente_id);
CREATE INDEX IF NOT EXISTS ix_venta_estado ON venta(estado);
CREATE INDEX IF NOT EXISTS ix_venta_detalle_venta ON venta_detalle(venta_id);
CREATE INDEX IF NOT EXISTS ix_venta_detalle_producto ON venta_detalle(producto_id);
CREATE INDEX IF NOT EXISTS ix_compra_fecha ON compra(fecha);
CREATE INDEX IF NOT EXISTS ix_compra_proveedor ON compra(proveedor_id);
CREATE INDEX IF NOT EXISTS ix_compra_estado ON compra(estado);
CREATE INDEX IF NOT EXISTS ix_compra_detalle_compra ON compra_detalle(compra_id);
CREATE INDEX IF NOT EXISTS ix_compra_detalle_producto ON compra_detalle(producto_id);
