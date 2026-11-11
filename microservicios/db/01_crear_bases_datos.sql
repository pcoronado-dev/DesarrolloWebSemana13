-- =====================================================================
-- GLAB-S12 - Comunicacion entre Servicios
-- Crea las bases de datos de desarrollo y produccion.
-- Ejecutar con: mysql -u root -p --execute="source 01_crear_bases_datos.sql"
-- =====================================================================

-- Bases de datos de desarrollo
CREATE DATABASE IF NOT EXISTS bd_categoria_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS bd_producto_dev  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS bd_cliente_dev   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS bd_pedido_dev    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Bases de datos de produccion
CREATE DATABASE IF NOT EXISTS bd_categoria CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS bd_producto  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS bd_cliente   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS bd_pedido    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
