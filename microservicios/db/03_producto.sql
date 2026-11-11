-- =====================================================================
-- producto-service
-- categoria_id es una referencia logica a categoria-service.
-- No se declara llave foranea porque las bases son independientes.
-- =====================================================================

USE bd_producto_dev;
CREATE TABLE IF NOT EXISTS productos (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(150) NOT NULL,
    precio       DOUBLE       NOT NULL,
    stock        INT          NOT NULL DEFAULT 0,
    categoria_id BIGINT       NOT NULL,
    CONSTRAINT chk_producto_precio CHECK (precio > 0),
    CONSTRAINT chk_producto_stock  CHECK (stock >= 0)
);

USE bd_producto;
CREATE TABLE IF NOT EXISTS productos (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(150) NOT NULL,
    precio       DOUBLE       NOT NULL,
    stock        INT          NOT NULL DEFAULT 0,
    categoria_id BIGINT       NOT NULL,
    CONSTRAINT chk_producto_precio CHECK (precio > 0),
    CONSTRAINT chk_producto_stock  CHECK (stock >= 0)
);
