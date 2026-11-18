-- Para instalaciones existentes de INDEA. No vuelve a crear ni borra tablas.
-- Conserva los teléfonos y el índice UNIQUE; permite no tener teléfono
-- cuando el nuevo formulario registra el contacto con correo electrónico.
ALTER TABLE indea_db.clientes MODIFY COLUMN telefono VARCHAR(15) NULL;
