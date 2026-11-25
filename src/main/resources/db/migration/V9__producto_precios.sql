-- V9: separa el precio de venta del precio de compra del producto.
-- El precio viejo era el de venta; el de compra se inicializa con el costo
-- actual (kardex), que es la mejor aproximacion disponible.

ALTER TABLE producto ADD COLUMN IF NOT EXISTS precio_venta DECIMAL(19,2);
ALTER TABLE producto ADD COLUMN IF NOT EXISTS precio_compra DECIMAL(19,2);

UPDATE producto SET precio_venta = COALESCE(precio, 0) WHERE precio_venta IS NULL;
UPDATE producto SET precio_compra = CASE
    WHEN COALESCE(costo_actual, 0) = 0 THEN COALESCE(precio, 0) * 0.70
    ELSE costo_actual
END WHERE precio_compra IS NULL;

ALTER TABLE producto ALTER COLUMN precio_venta SET NOT NULL;
ALTER TABLE producto ALTER COLUMN precio_compra SET NOT NULL;

ALTER TABLE producto DROP CONSTRAINT IF EXISTS chk_producto_precio;
ALTER TABLE producto DROP COLUMN IF EXISTS precio;

ALTER TABLE producto ADD CONSTRAINT IF NOT EXISTS chk_producto_precio_venta CHECK (precio_venta >= 0);
ALTER TABLE producto ADD CONSTRAINT IF NOT EXISTS chk_producto_precio_compra CHECK (precio_compra >= 0);
