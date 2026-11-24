-- V8: el RUC del proveedor vuelve a ser obligatorio.
-- Se eliminan los proveedores invalidos cargados sin RUC antes de restaurar
-- la restriccion NOT NULL.

DELETE FROM proveedor WHERE ruc IS NULL OR TRIM(ruc) = '';

ALTER TABLE proveedor ALTER COLUMN ruc SET NOT NULL;
ALTER TABLE proveedor ALTER COLUMN nombre SET NOT NULL;
