USE indea_sistema;

CREATE TABLE proveedores (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,
    contacto VARCHAR(100) NULL,
    telefono VARCHAR(30) NULL,
    correo VARCHAR(100) NULL,
    direccion VARCHAR(200) NULL,
    fecha_alta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE compras
    ADD COLUMN id_proveedor INT NULL,
    ADD COLUMN lote_compra CHAR(36) NULL,
    ADD INDEX ix_compras_proveedor_lote (id_proveedor, lote_compra),
    ADD CONSTRAINT fk_compras_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedores (id_proveedor);

START TRANSACTION;

INSERT INTO proveedores (nombre)
SELECT MIN(TRIM(proveedor)) FROM compras
WHERE origen = 'PROVEEDOR' AND proveedor IS NOT NULL AND TRIM(proveedor) <> ''
GROUP BY TRIM(proveedor) COLLATE utf8mb4_unicode_ci;

UPDATE compras c JOIN proveedores p
    ON p.nombre = TRIM(c.proveedor) COLLATE utf8mb4_unicode_ci
SET c.id_proveedor = p.id_proveedor
WHERE c.origen = 'PROVEEDOR';

COMMIT;
