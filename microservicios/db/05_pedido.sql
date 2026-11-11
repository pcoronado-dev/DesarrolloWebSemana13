-- =====================================================================
-- pedido-service
-- cliente_id y producto_id son referencias logicas a otros servicios.
-- El pedido no se elimina fisicamente: cambia a estado ANULADO.
-- =====================================================================

USE bd_pedido_dev;
CREATE TABLE IF NOT EXISTS pedidos (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id  BIGINT      NOT NULL,
    producto_id BIGINT      NOT NULL,
    cantidad    INT         NOT NULL,
    estado      VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_pedido_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_pedido_estado   CHECK (estado IN ('PENDIENTE', 'ATENDIDO', 'ANULADO'))
);

USE bd_pedido;
CREATE TABLE IF NOT EXISTS pedidos (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id  BIGINT      NOT NULL,
    producto_id BIGINT      NOT NULL,
    cantidad    INT         NOT NULL,
    estado      VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_pedido_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_pedido_estado   CHECK (estado IN ('PENDIENTE', 'ATENDIDO', 'ANULADO'))
);
