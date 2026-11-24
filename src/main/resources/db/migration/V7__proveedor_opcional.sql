-- V7: los datos de proveedor son opcionales. La tabla vieja se habia creado
-- con todas las columnas NOT NULL; se alinean con el modelo y el formulario.

ALTER TABLE proveedor ALTER COLUMN nombre SET NULL;
ALTER TABLE proveedor ALTER COLUMN ruc SET NULL;
ALTER TABLE proveedor ALTER COLUMN telefono SET NULL;
ALTER TABLE proveedor ALTER COLUMN email SET NULL;
ALTER TABLE proveedor ALTER COLUMN direccion SET NULL;
