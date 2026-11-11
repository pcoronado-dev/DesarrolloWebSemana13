-- =====================================================================
-- categoria-service
-- En dev Hibernate crea la tabla (ddl-auto=update). Este script es
-- obligatorio para prod (ddl-auto=validate).
-- =====================================================================

USE bd_categoria_dev;
CREATE TABLE IF NOT EXISTS categorias (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    estado VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO'
);

USE bd_categoria;
CREATE TABLE IF NOT EXISTS categorias (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    estado VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO'
);
