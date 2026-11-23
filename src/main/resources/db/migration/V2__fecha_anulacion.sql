-- V2: columnas de anulacion que antes agregaba DatabaseInitializer a mano.
-- IF NOT EXISTS hace que sea seguro tanto en bases nuevas (V1 ya las trae)
-- como en bases existentes (Flyway las aplica tras el baseline).

ALTER TABLE venta ADD COLUMN IF NOT EXISTS fecha_anulacion TIMESTAMP;
ALTER TABLE compra ADD COLUMN IF NOT EXISTS fecha_anulacion TIMESTAMP;
