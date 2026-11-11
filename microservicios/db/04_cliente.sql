-- =====================================================================
-- cliente-service
-- =====================================================================

USE bd_cliente_dev;
CREATE TABLE IF NOT EXISTS clientes (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    telefono  VARCHAR(20),
    nro_dni   VARCHAR(8)   NOT NULL UNIQUE,
    estado    VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT chk_cliente_dni CHECK (CHAR_LENGTH(nro_dni) = 8)
);

USE bd_cliente;
CREATE TABLE IF NOT EXISTS clientes (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    telefono  VARCHAR(20),
    nro_dni   VARCHAR(8)   NOT NULL UNIQUE,
    estado    VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT chk_cliente_dni CHECK (CHAR_LENGTH(nro_dni) = 8)
);
