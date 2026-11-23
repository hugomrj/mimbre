-- V4: normaliza Producto.categoria a una FK real (producto_categoria).
-- Se conserva la columna `categoria` como cache del nombre para las vistas.

ALTER TABLE producto ADD COLUMN IF NOT EXISTS categoria_id BIGINT;

-- Crea las categorias que existan solo como texto en productos viejos.
INSERT INTO producto_categoria (nombre, descripcion, activo, creado_en, actualizado_en)
SELECT DISTINCT TRIM(p.categoria), 'Migrada desde productos', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM producto p
WHERE p.categoria IS NOT NULL
  AND TRIM(p.categoria) <> ''
  AND NOT EXISTS (
      SELECT 1 FROM producto_categoria c
      WHERE LOWER(c.nombre) = LOWER(TRIM(p.categoria))
  );

-- Enlaza cada producto con su categoria.
UPDATE producto p
SET categoria_id = (
    SELECT MIN(c.id)
    FROM producto_categoria c
    WHERE LOWER(c.nombre) = LOWER(TRIM(p.categoria))
)
WHERE p.categoria IS NOT NULL AND TRIM(p.categoria) <> '';

ALTER TABLE producto ADD CONSTRAINT IF NOT EXISTS fk_producto_categoria
    FOREIGN KEY (categoria_id) REFERENCES producto_categoria(id) ON DELETE RESTRICT;

CREATE INDEX IF NOT EXISTS ix_producto_categoria_id ON producto(categoria_id);
