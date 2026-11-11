-- =====================================================================
-- Datos de prueba para el perfil dev.
-- Ejecutar despues de 02, 03, 04 y 05.
-- Los ids se fijan para que las referencias entre servicios coincidan.
-- =====================================================================

USE bd_categoria_dev;

INSERT INTO categorias (id, nombre, estado) VALUES
    (1, 'Tecnologia', 'ACTIVO'),
    (2, 'Accesorios', 'ACTIVO'),
    (3, 'Impresoras', 'ACTIVO')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), estado = VALUES(estado);

USE bd_producto_dev;

INSERT INTO productos (id, nombre, precio, stock, categoria_id) VALUES
    (1, 'Laptop Lenovo ThinkPad', 3500.00, 15, 1),
    (2, 'Mouse Logitech M185',      89.90, 50, 2),
    (3, 'Teclado Mecanico Redragon', 199.99, 25, 2)
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    precio = VALUES(precio),
    stock = VALUES(stock),
    categoria_id = VALUES(categoria_id);

USE bd_cliente_dev;

INSERT INTO clientes (id, nombre, apellidos, direccion, telefono, nro_dni, estado) VALUES
    (1, 'Carlos', 'Ramirez Torres', 'Av. Javier Prado 123, Lima', '987654321', '12345678', 'ACTIVO'),
    (2, 'Maria',  'Gomez Ruiz',     'Calle Los Olivos 456',      '912345678', '87654321', 'ACTIVO')
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    apellidos = VALUES(apellidos),
    direccion = VALUES(direccion),
    telefono = VALUES(telefono),
    estado = VALUES(estado);

USE bd_pedido_dev;

INSERT INTO pedidos (id, cliente_id, producto_id, cantidad, estado) VALUES
    (1, 1, 1, 2, 'PENDIENTE')
ON DUPLICATE KEY UPDATE
    cliente_id = VALUES(cliente_id),
    producto_id = VALUES(producto_id),
    cantidad = VALUES(cantidad),
    estado = VALUES(estado);
