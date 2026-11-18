SET NAMES utf8mb4;

CREATE DATABASE `indea_xalapa` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `indea_xalapa`;

CREATE TABLE `indea_xalapa`.`clientes` (
  `id_cliente` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `telefono` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `correo` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `direccion` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_registro` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_cliente`),
  UNIQUE KEY `telefono` (`telefono`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `indea_xalapa`.`empleados` (
  `id_empleado` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `telefono` varchar(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  `correo` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rol` enum('ADMINISTRADOR','TECNICO','VENDEDOR','MARKETING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_ingreso` date NOT NULL,
  PRIMARY KEY (`id_empleado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `indea_xalapa`.`equipos` (
  `id_equipo` int NOT NULL AUTO_INCREMENT,
  `tipo` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `marca` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `modelo` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `condicion` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `precio_venta` decimal(10,2) NOT NULL,
  `estado` enum('DISPONIBLE','VENDIDO','NO_DISPONIBLE_AUN') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NO_DISPONIBLE_AUN',
  `fecha_alta` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_equipo`),
  CONSTRAINT `chk_equipos_precio` CHECK ((`precio_venta` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `indea_xalapa`.`refacciones` (
  `id_refaccion` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `costo` decimal(10,2) NOT NULL,
  `stock` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_refaccion`),
  UNIQUE KEY `nombre` (`nombre`),
  CONSTRAINT `chk_refacciones_costo` CHECK ((`costo` >= 0)),
  CONSTRAINT `chk_refacciones_stock` CHECK ((`stock` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `indea_xalapa`.`usuarios` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `id_empleado` int NOT NULL,
  `nombre_usuario` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `contrasena_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `intentos_fallidos` tinyint NOT NULL DEFAULT '0',
  `bloqueado` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `id_empleado` (`id_empleado`),
  UNIQUE KEY `nombre_usuario` (`nombre_usuario`),
  CONSTRAINT `fk_usuarios_empleado` FOREIGN KEY (`id_empleado`) REFERENCES `indea_xalapa`.`empleados` (`id_empleado`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `indea_xalapa`.`reparaciones` (
  `id_reparacion` int NOT NULL AUTO_INCREMENT,
  `folio` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `id_cliente` int NOT NULL,
  `id_tecnico` int NOT NULL,
  `tipo_equipo` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `marca` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `modelo` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `falla_reportada` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `diagnostico` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `costo_servicio` decimal(10,2) NOT NULL DEFAULT '0.00',
  `pagado` tinyint(1) NOT NULL DEFAULT '0',
  `estado` enum('RECIBIDO','EN_DIAGNOSTICO','EN_REPARACION','LISTO_PARA_ENTREGA','ENTREGADO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'RECIBIDO',
  `fecha_ingreso` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_entrega` datetime DEFAULT NULL,
  `fecha_pago` datetime DEFAULT NULL,
  PRIMARY KEY (`id_reparacion`),
  UNIQUE KEY `folio` (`folio`),
  KEY `fk_reparaciones_cliente` (`id_cliente`),
  KEY `fk_reparaciones_tecnico` (`id_tecnico`),
  CONSTRAINT `fk_reparaciones_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `indea_xalapa`.`clientes` (`id_cliente`),
  CONSTRAINT `fk_reparaciones_tecnico` FOREIGN KEY (`id_tecnico`) REFERENCES `indea_xalapa`.`empleados` (`id_empleado`),
  CONSTRAINT `chk_reparaciones_costo` CHECK ((`costo_servicio` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `indea_xalapa`.`proveedores` (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,
    contacto VARCHAR(100) NULL,
    telefono VARCHAR(30) NULL,
    correo VARCHAR(100) NULL,
    direccion VARCHAR(200) NULL,
    fecha_alta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `indea_xalapa`.`compras` (
  `id_compra` int NOT NULL AUTO_INCREMENT,
  `id_equipo` int NOT NULL,
  `origen` enum('CLIENTE','PROVEEDOR') COLLATE utf8mb4_unicode_ci NOT NULL,
  `id_cliente` int DEFAULT NULL,
  `proveedor` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `id_empleado` int NOT NULL,
  `precio_compra` decimal(10,2) NOT NULL,
  `fecha_compra` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `id_proveedor` int DEFAULT NULL,
  `lote_compra` char(36) DEFAULT NULL,
  KEY `ix_compras_proveedor_lote` (`id_proveedor`, `lote_compra`),
  CONSTRAINT `fk_compras_proveedor` FOREIGN KEY (`id_proveedor`) REFERENCES `indea_xalapa`.`proveedores` (`id_proveedor`),
  PRIMARY KEY (`id_compra`),
  UNIQUE KEY `id_equipo` (`id_equipo`),
  KEY `fk_compras_cliente` (`id_cliente`),
  KEY `fk_compras_empleado` (`id_empleado`),
  CONSTRAINT `fk_compras_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `indea_xalapa`.`clientes` (`id_cliente`),
  CONSTRAINT `fk_compras_empleado` FOREIGN KEY (`id_empleado`) REFERENCES `indea_xalapa`.`empleados` (`id_empleado`),
  CONSTRAINT `fk_compras_equipo` FOREIGN KEY (`id_equipo`) REFERENCES `indea_xalapa`.`equipos` (`id_equipo`),
  CONSTRAINT `chk_compras_origen` CHECK ((((`origen` = _utf8mb4'CLIENTE') and (`id_cliente` is not null)) or ((`origen` = _utf8mb4'PROVEEDOR') and (`proveedor` is not null)))),
  CONSTRAINT `chk_compras_precio` CHECK ((`precio_compra` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `indea_xalapa`.`publicaciones` (
  `id_publicacion` int NOT NULL AUTO_INCREMENT,
  `id_equipo` int NOT NULL,
  `id_autor` int NOT NULL,
  `titulo` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tipo` enum('DISPONIBLE','AGOTADO','PROXIMAMENTE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `activa` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_publicacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_publicacion`),
  KEY `fk_publicaciones_equipo` (`id_equipo`),
  KEY `fk_publicaciones_autor` (`id_autor`),
  CONSTRAINT `fk_publicaciones_autor` FOREIGN KEY (`id_autor`) REFERENCES `indea_xalapa`.`empleados` (`id_empleado`),
  CONSTRAINT `fk_publicaciones_equipo` FOREIGN KEY (`id_equipo`) REFERENCES `indea_xalapa`.`equipos` (`id_equipo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `indea_xalapa`.`historial_estado_reparacion` (
  `id_historial` int NOT NULL AUTO_INCREMENT,
  `id_reparacion` int NOT NULL,
  `estado` enum('RECIBIDO','EN_DIAGNOSTICO','EN_REPARACION','LISTO_PARA_ENTREGA','ENTREGADO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_cambio` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `id_empleado` int NOT NULL,
  PRIMARY KEY (`id_historial`),
  KEY `fk_historial_reparacion` (`id_reparacion`),
  KEY `fk_historial_empleado` (`id_empleado`),
  CONSTRAINT `fk_historial_empleado` FOREIGN KEY (`id_empleado`) REFERENCES `indea_xalapa`.`empleados` (`id_empleado`),
  CONSTRAINT `fk_historial_reparacion` FOREIGN KEY (`id_reparacion`) REFERENCES `indea_xalapa`.`reparaciones` (`id_reparacion`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `indea_xalapa`.`reparacion_refaccion` (
  `id_reparacion` int NOT NULL,
  `id_refaccion` int NOT NULL,
  `cantidad` int NOT NULL,
  `costo_unitario` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id_reparacion`,`id_refaccion`),
  KEY `fk_rr_refaccion` (`id_refaccion`),
  CONSTRAINT `fk_rr_refaccion` FOREIGN KEY (`id_refaccion`) REFERENCES `indea_xalapa`.`refacciones` (`id_refaccion`),
  CONSTRAINT `fk_rr_reparacion` FOREIGN KEY (`id_reparacion`) REFERENCES `indea_xalapa`.`reparaciones` (`id_reparacion`) ON DELETE CASCADE,
  CONSTRAINT `chk_rr_cantidad` CHECK ((`cantidad` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `indea_xalapa`.`ventas` (
  `id_venta` int NOT NULL AUTO_INCREMENT,
  `id_equipo` int NOT NULL,
  `id_cliente` int NOT NULL,
  `id_vendedor` int NOT NULL,
  `precio_venta` decimal(10,2) NOT NULL,
  `fecha_venta` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_venta`),
  UNIQUE KEY `id_equipo` (`id_equipo`),
  KEY `fk_ventas_cliente` (`id_cliente`),
  KEY `fk_ventas_vendedor` (`id_vendedor`),
  CONSTRAINT `fk_ventas_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `indea_xalapa`.`clientes` (`id_cliente`),
  CONSTRAINT `fk_ventas_equipo` FOREIGN KEY (`id_equipo`) REFERENCES `indea_xalapa`.`equipos` (`id_equipo`),
  CONSTRAINT `fk_ventas_vendedor` FOREIGN KEY (`id_vendedor`) REFERENCES `indea_xalapa`.`empleados` (`id_empleado`),
  CONSTRAINT `chk_ventas_precio` CHECK ((`precio_venta` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TRIGGER `indea_xalapa`.`reparacion_fecha_pago` BEFORE UPDATE ON `indea_xalapa`.`reparaciones` FOR EACH ROW
SET NEW.fecha_pago = CASE
    WHEN NEW.pagado = 0 THEN NULL
    WHEN NEW.pagado = 1 AND OLD.pagado = 0 THEN CURRENT_TIMESTAMP
    ELSE NEW.fecha_pago
END;

START TRANSACTION;

INSERT INTO `indea_xalapa`.`clientes` (`id_cliente`, `nombre`, `telefono`, `correo`, `direccion`, `fecha_registro`) VALUES
(1, 'Isabel Luna Paredes', '2288164701', 'ilp.contacto.indea@gmail.com', 'Colonia Centro, Xalapa', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 46 DAY), '09:00:00')),
(2, 'Mateo Navarro Ríos', '2288164702', 'mnr.contacto.indea@gmail.com', 'Colonia El Dique, Xalapa', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 47 DAY), '09:00:00')),
(3, 'Renata Salgado Méndez', '2288164703', 'rsm.contacto.indea@gmail.com', 'Colonia Aguacatal, Xalapa', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 48 DAY), '09:00:00')),
(4, 'Iván Cruz Muñoz', '2288164704', 'icm.contacto.indea@gmail.com', 'Colonia Revolución, Xalapa', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 49 DAY), '09:00:00')),
(5, 'Adriana Silva Herrera', '2288164705', 'ash.contacto.indea@gmail.com', 'Colonia Progreso, Xalapa', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 50 DAY), '09:00:00')),
(6, 'Daniela Rivas Castro', '2288164706', 'drc.contacto.indea@gmail.com', 'Colonia Los Laureles, Xalapa', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 51 DAY), '09:00:00')),
(7, 'Héctor Gómez Nieto', '2288164707', 'hgn.contacto.indea@gmail.com', 'Colonia Francisco Ferrer Guardia, Xalapa', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 52 DAY), '09:00:00'));

INSERT INTO `indea_xalapa`.`empleados` (`id_empleado`, `nombre`, `telefono`, `correo`, `rol`, `activo`, `fecha_ingreso`) VALUES
(1, 'Ángel Leonardo Hernández Casarín', '2281000001', NULL, 'ADMINISTRADOR', 1, DATE_SUB(CURRENT_DATE, INTERVAL 111 DAY)),
(2, 'Edgar Báez García', '2281000002', NULL, 'TECNICO', 1, DATE_SUB(CURRENT_DATE, INTERVAL 112 DAY)),
(3, 'Óscar Daniel Hernández Hernández', '2281000005', NULL, 'TECNICO', 1, DATE_SUB(CURRENT_DATE, INTERVAL 113 DAY)),
(4, 'Alan López', '2281000003', NULL, 'VENDEDOR', 1, DATE_SUB(CURRENT_DATE, INTERVAL 114 DAY)),
(5, 'Juan Manuel Hernández Cova', '2281000004', NULL, 'MARKETING', 1, DATE_SUB(CURRENT_DATE, INTERVAL 115 DAY));

INSERT INTO `indea_xalapa`.`equipos` (`id_equipo`, `tipo`, `marca`, `modelo`, `condicion`, `precio_venta`, `estado`, `fecha_alta`) VALUES
(1, 'Laptop', 'Lenovo', 'ThinkPad L490', 'Reacondicionada', 4900, 'VENDIDO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 17 DAY), '09:00:00')),
(2, 'Laptop', 'Dell', 'Latitude 5410', 'Usada, con cargador', 5350, 'VENDIDO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 15 DAY), '09:00:00')),
(3, 'Monitor', 'Samsung', 'S24R350', 'Usado, probado con HDMI', 1790, 'VENDIDO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 12 DAY), '09:00:00')),
(4, 'Impresora', 'Brother', 'DCP-T420W', 'Usada, revisada', 3100, 'VENDIDO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 32 DAY), '09:00:00')),
(5, 'PC de escritorio', 'HP', 'EliteDesk 800 G4', 'Reacondicionada', 4400, 'VENDIDO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 40 DAY), '09:00:00')),
(6, 'Laptop', 'Acer', 'TravelMate P214', 'Usada, batería probada', 5200, 'VENDIDO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 18 DAY), '09:00:00')),
(7, 'Monitor', 'LG', '27ML600M', 'Usado, con base', 2450, 'VENDIDO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 10 DAY), '09:00:00')),
(8, 'Laptop', 'ASUS', 'VivoBook X1502', 'Reacondicionada', 6250, 'DISPONIBLE', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY), '09:00:00')),
(9, 'PC de escritorio', 'Lenovo', 'ThinkCentre M720s', 'Usada, con teclado', 3800, 'DISPONIBLE', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '09:00:00')),
(10, 'Impresora', 'Epson', 'EcoTank L4260', 'Se está revisando el cabezal', 3450, 'NO_DISPONIBLE_AUN', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '09:00:00')),
(11, 'Monitor', 'Dell', 'P2419H', 'Usado, sin cable HDMI', 1900, 'DISPONIBLE', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '09:00:00')),
(12, 'Laptop', 'Dell', 'Latitude 5420', 'Reacondicionada, probada', 5800, 'DISPONIBLE', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '10:00:00')),
(13, 'Laptop', 'Dell', 'Latitude 5420', 'Reacondicionada, probada', 5800, 'DISPONIBLE', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '10:00:00')),
(14, 'Laptop', 'Dell', 'Latitude 5420', 'Reacondicionada, probada', 5800, 'DISPONIBLE', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '10:00:00')),
(15, 'Laptop', 'Dell', 'Latitude 5420', 'Reacondicionada, probada', 5800, 'DISPONIBLE', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '10:00:00')),
(16, 'Laptop', 'Dell', 'Latitude 5420', 'Reacondicionada, probada', 5800, 'DISPONIBLE', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '10:00:00'));

INSERT INTO `indea_xalapa`.`refacciones` (`id_refaccion`, `nombre`, `costo`, `stock`) VALUES
(1, 'SSD M.2 NVMe 500 GB', 520.00, 7),
(2, 'Pantalla 15.6 pulgadas FHD 30 pines', 730.00, 4),
(3, 'Conector DC para Lenovo IdeaPad', 120.00, 9),
(4, 'Ventilador Dell Latitude 5410', 255.00, 5),
(5, 'Memoria RAM DDR4 SO-DIMM 8 GB', 310.00, 6),
(6, 'Pasta térmica 4 g', 85.00, 12),
(7, 'Kit de rodillos Epson L3150', 205.00, 3);

INSERT INTO `indea_xalapa`.`usuarios` (`id_usuario`, `id_empleado`, `nombre_usuario`, `contrasena_hash`, `intentos_fallidos`, `bloqueado`) VALUES
(1, 1, 'admin', '65536:5BE9Z2ZAXrcL88Kp6+WSwQ==:ggm1nRbnq4Cnxo/eepl+XeNQ210E7fxlhdwQxqMPIxA=', 0, 0),
(2, 2, 'Edgar Báez García', '65536:pG5TNATVqGhfVOS/U3AmiA==:lUOoR6YoFdGAd9FK13TdNojU/IqmE5Xdsp/6UV/45F4=', 0, 0),
(3, 3, 'Óscar Daniel Hernández Hernández', '65536:A2iUGAzhRKYdtoYOLyR2Eg==:KcRVJwfsBkTKP3DyEO2WtcqbLspPReWAfs4gIzmStqc=', 0, 0),
(4, 4, 'Alan López', '65536:18qH+qxdLZYCjG5F6FsdIA==:jwhWiJrdnrP4bZ/Leb/rhXiTCINnQg51KS/bZuvkkSE=', 0, 0),
(5, 5, 'Juan Manuel Hernández Cova', '65536:j9LMOYzDSo3aY3+6pHk+RA==:uLIwD3yDxQXV5MxknMsnxl7nGB5meDZsizLxph9p8zo=', 0, 0);

INSERT INTO `indea_xalapa`.`reparaciones` (`id_reparacion`, `folio`, `id_cliente`, `id_tecnico`, `tipo_equipo`, `marca`, `modelo`, `falla_reportada`, `diagnostico`, `costo_servicio`, `pagado`, `estado`, `fecha_ingreso`, `fecha_entrega`, `fecha_pago`) VALUES
(1, 'INDEA-OT-4011', 1, 2, 'Laptop', 'HP', 'Pavilion 15-eg', 'No se ve imagen en la pantalla.', 'Pantalla sustituida. Se verificó imagen y brillo.', 1150, 1, 'ENTREGADO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 8 DAY), '08:45:00'), TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY), '15:00:00'), TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY), '14:55:00')),
(2, 'INDEA-OT-4012', 2, 3, 'Laptop', 'Acer', 'Aspire A514', 'El disco ya no aparece al encender.', 'Se instaló un SSD y se comprobó el arranque.', 890, 1, 'ENTREGADO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 16 DAY), '08:45:00'), TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 9 DAY), '15:00:00'), TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 9 DAY), '14:55:00')),
(3, 'INDEA-OT-4013', 3, 2, 'Laptop', 'Lenovo', 'IdeaPad 3 15ITL6', 'El cargador hace falso contacto.', 'Conector interno sustituido. Carga estable en pruebas.', 560, 0, 'LISTO_PARA_ENTREGA', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY), '08:45:00'), NULL, NULL),
(4, 'INDEA-OT-4014', 4, 2, 'Laptop', 'Dell', 'Latitude 5410', 'Se apaga al usar videollamadas.', 'Se cambió el ventilador y se aplicó pasta térmica.', 760, 0, 'EN_REPARACION', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '08:45:00'), NULL, NULL),
(5, 'INDEA-OT-4015', 5, 3, 'PC de escritorio', 'HP', 'ProDesk 400 G6', 'Se reinicia al encender.', 'La fuente está en revisión.', 0, 0, 'EN_DIAGNOSTICO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '08:45:00'), NULL, NULL),
(6, 'INDEA-OT-4016', 6, 2, 'Impresora', 'Epson', 'EcoTank L3150', 'Atasco de papel al tomar la hoja.', 'Rodillos sustituidos y prueba de alimentación en proceso.', 510, 0, 'EN_REPARACION', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '08:45:00'), NULL, NULL),
(7, 'INDEA-OT-4017', 7, 2, 'Laptop', 'Lenovo', 'ThinkPad E14', 'Se congela al abrir varias aplicaciones.', NULL, 0, 0, 'RECIBIDO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY), '08:45:00'), NULL, NULL);

INSERT INTO `indea_xalapa`.`proveedores` (`id_proveedor`, `nombre`, `contacto`, `telefono`, `correo`, `direccion`) VALUES
(1, 'Distribuidora Cómputo del Centro', 'María Estela Vargas', '2288374101', 'mev.proveedor.indea@gmail.com', 'Av. Manuel Ávila Camacho, Col. Centro, Xalapa, Veracruz'),
(2, 'TecnoPartes del Golfo', 'Luis Ángel Ríos', '2288374102', 'lar.proveedor.indea@gmail.com', 'Av. Lázaro Cárdenas, Col. Rafael Lucio, Xalapa, Veracruz'),
(3, 'Equipos Digitales de Xalapa', 'Fernanda Ortiz Cruz', '2288374103', 'foc.proveedor.indea@gmail.com', 'Av. 20 de Noviembre, Col. Centro, Xalapa, Veracruz'),
(4, 'Suministros del Dique', 'Carlos Mendoza Soto', '2288374104', 'cms.proveedor.indea@gmail.com', 'Paseo de Los Lagos, Col. El Dique, Xalapa, Veracruz'),
(5, 'Electrónica Las Ánimas', 'Lucía Ramírez Peña', '2288374105', 'lrp.proveedor.indea@gmail.com', 'Av. Araucarias, Fracc. Indeco Ánimas, Xalapa, Veracruz'),
(6, 'Soluciones de Cómputo Revolución', 'Jorge Flores Gil', '2288374106', 'jfg.proveedor.indea@gmail.com', 'Av. México, Col. Revolución, Xalapa, Veracruz'),
(7, 'Periféricos del Valle Xalapa', 'Sandra Pérez Ruiz', '2288374107', 'spr.proveedor.indea@gmail.com', 'Av. Miguel Alemán, Col. Federal, Xalapa, Veracruz');

INSERT INTO `indea_xalapa`.`compras` (`id_compra`, `id_equipo`, `origen`, `id_cliente`, `proveedor`, `id_empleado`, `precio_compra`, `fecha_compra`, `id_proveedor`, `lote_compra`) VALUES
(1, 1, 'CLIENTE', 1, NULL, 4, 3100, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 17 DAY), '09:05:00'), NULL, NULL),
(2, 2, 'PROVEEDOR', NULL, 'Distribuidora Cómputo del Centro', 4, 3600, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 15 DAY), '09:05:00'), 1, NULL),
(3, 3, 'CLIENTE', 2, NULL, 4, 910, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 12 DAY), '09:05:00'), NULL, NULL),
(4, 4, 'PROVEEDOR', NULL, 'TecnoPartes del Golfo', 4, 1950, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 32 DAY), '09:05:00'), 2, NULL),
(5, 5, 'CLIENTE', 3, NULL, 4, 2700, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 40 DAY), '09:05:00'), NULL, NULL),
(6, 6, 'CLIENTE', 4, NULL, 4, 3400, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 18 DAY), '09:05:00'), NULL, NULL),
(7, 7, 'PROVEEDOR', NULL, 'Distribuidora Cómputo del Centro', 4, 1550, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 10 DAY), '09:05:00'), 1, NULL),
(8, 8, 'PROVEEDOR', NULL, 'TecnoPartes del Golfo', 4, 4100, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY), '09:05:00'), 2, NULL),
(9, 9, 'CLIENTE', 5, NULL, 4, 2450, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '09:05:00'), NULL, NULL),
(10, 10, 'CLIENTE', 6, NULL, 4, 1800, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '09:05:00'), NULL, NULL),
(11, 11, 'CLIENTE', 7, NULL, 4, 1120, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '09:05:00'), NULL, NULL),
(12, 12, 'PROVEEDOR', NULL, 'Equipos Digitales de Xalapa', 4, 3950, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '10:00:00'), 3, '4ec336f2-549d-4e3d-8183-95d971eb0bd2'),
(13, 13, 'PROVEEDOR', NULL, 'Equipos Digitales de Xalapa', 4, 3950, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '10:00:00'), 3, '4ec336f2-549d-4e3d-8183-95d971eb0bd2'),
(14, 14, 'PROVEEDOR', NULL, 'Equipos Digitales de Xalapa', 4, 3950, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '10:00:00'), 3, '4ec336f2-549d-4e3d-8183-95d971eb0bd2'),
(15, 15, 'PROVEEDOR', NULL, 'Equipos Digitales de Xalapa', 4, 3950, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '10:00:00'), 3, '4ec336f2-549d-4e3d-8183-95d971eb0bd2'),
(16, 16, 'PROVEEDOR', NULL, 'Equipos Digitales de Xalapa', 4, 3950, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '10:00:00'), 3, '4ec336f2-549d-4e3d-8183-95d971eb0bd2');

INSERT INTO `indea_xalapa`.`publicaciones` (`id_publicacion`, `id_equipo`, `id_autor`, `titulo`, `descripcion`, `tipo`, `activa`, `fecha_publicacion`) VALUES
(1, 8, 5, 'ASUS VivoBook X1502 disponible', 'Equipo probado, incluye cargador y revisión de batería.', 'DISPONIBLE', 1, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '14:30:00')),
(2, 9, 5, 'Lenovo ThinkCentre M720s en inventario', 'Equipo de escritorio para oficina con teclado.', 'DISPONIBLE', 1, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '14:30:00')),
(3, 10, 5, 'Epson EcoTank L4260 próximamente', 'Disponible cuando concluya la prueba de impresión.', 'PROXIMAMENTE', 1, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '14:30:00')),
(4, 11, 5, 'Dell P2419H de 24 pulgadas', 'Monitor con base ajustable y entrada HDMI.', 'DISPONIBLE', 1, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '14:30:00')),
(5, 1, 5, 'Lenovo ThinkPad L490 vendido', 'La unidad fue entregada al comprador.', 'AGOTADO', 1, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY), '14:30:00')),
(6, 3, 5, 'Samsung S24R350 agotado', 'No quedan unidades de este modelo en inventario.', 'AGOTADO', 1, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '14:30:00')),
(7, 6, 5, 'Acer TravelMate P214 vendido', 'Publicación cerrada después de la venta.', 'AGOTADO', 0, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '14:30:00'));

INSERT INTO `indea_xalapa`.`historial_estado_reparacion` (`id_historial`, `id_reparacion`, `estado`, `fecha_cambio`, `id_empleado`) VALUES
(1, 1, 'RECIBIDO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 8 DAY), '08:45:00'), 1),
(2, 1, 'EN_DIAGNOSTICO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY), '09:25:00'), 2),
(3, 1, 'EN_REPARACION', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY), '11:00:00'), 2),
(4, 1, 'LISTO_PARA_ENTREGA', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '12:00:00'), 2),
(5, 1, 'ENTREGADO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY), '15:00:00'), 2),
(6, 2, 'RECIBIDO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 16 DAY), '08:45:00'), 1),
(7, 2, 'EN_DIAGNOSTICO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 15 DAY), '09:25:00'), 3),
(8, 2, 'EN_REPARACION', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 14 DAY), '11:00:00'), 3),
(9, 2, 'LISTO_PARA_ENTREGA', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 13 DAY), '12:00:00'), 3),
(10, 2, 'ENTREGADO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 9 DAY), '15:00:00'), 3),
(11, 3, 'RECIBIDO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY), '08:45:00'), 1),
(12, 3, 'EN_DIAGNOSTICO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '09:25:00'), 2),
(13, 3, 'EN_REPARACION', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '11:00:00'), 2),
(14, 3, 'LISTO_PARA_ENTREGA', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '12:00:00'), 2),
(15, 4, 'RECIBIDO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '08:45:00'), 1),
(16, 4, 'EN_DIAGNOSTICO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '09:25:00'), 2),
(17, 4, 'EN_REPARACION', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '11:00:00'), 2),
(18, 5, 'RECIBIDO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '08:45:00'), 1),
(19, 5, 'EN_DIAGNOSTICO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '09:25:00'), 3),
(20, 6, 'RECIBIDO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '08:45:00'), 1),
(21, 6, 'EN_DIAGNOSTICO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '09:25:00'), 2),
(22, 6, 'EN_REPARACION', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '11:00:00'), 2),
(23, 7, 'RECIBIDO', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY), '08:45:00'), 1);

INSERT INTO `indea_xalapa`.`reparacion_refaccion` (`id_reparacion`, `id_refaccion`, `cantidad`, `costo_unitario`) VALUES
(1, 2, 1, 730.00),
(2, 1, 1, 520.00),
(3, 3, 1, 120.00),
(4, 4, 1, 255.00),
(4, 6, 1, 85.00),
(6, 7, 1, 205.00),
(2, 5, 1, 310.00);

INSERT INTO `indea_xalapa`.`ventas` (`id_venta`, `id_equipo`, `id_cliente`, `id_vendedor`, `precio_venta`, `fecha_venta`) VALUES
(1, 1, 4, 4, 4900, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 0 DAY), '13:20:00')),
(2, 2, 6, 4, 5350, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '13:20:00')),
(3, 3, 1, 4, 1790, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '13:20:00')),
(4, 4, 5, 4, 3100, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 8 DAY), '13:20:00')),
(5, 5, 2, 4, 4400, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 12 DAY), '13:20:00')),
(6, 6, 7, 4, 5200, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '13:20:00')),
(7, 7, 3, 4, 2450, TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '13:20:00'));

COMMIT;
