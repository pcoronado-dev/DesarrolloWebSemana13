# Microservicios - GLAB-S13

Este directorio contiene los seis proyectos Maven del laboratorio:

| Proyecto | Puerto DEV | Responsabilidad |
| --- | ---: | --- |
| `eureka-server` | 8761 | Registro y descubrimiento de servicios |
| `gateway-service` | 8080 | API Gateway: único punto de entrada |
| `cliente-service` | 8081 | CRUD de clientes |
| `categoria-service` | 8082 | CRUD de categorías |
| `producto-service` | 8083 | CRUD de productos + Feign hacia categorías |
| `pedido-service` | 8084 | Pedidos + Feign hacia clientes y productos |

## Arranque rápido

```powershell
cd eureka-server;        .\mvnw.cmd spring-boot:run
cd ..\categoria-service; .\mvnw.cmd spring-boot:run
cd ..\cliente-service;   .\mvnw.cmd spring-boot:run
cd ..\producto-service;  .\mvnw.cmd spring-boot:run
cd ..\pedido-service;    .\mvnw.cmd spring-boot:run
cd ..\gateway-service;   .\mvnw.cmd spring-boot:run
```

El orden importa: el Gateway y los servicios con Feign resuelven a sus dependencias a
través de Eureka. Aun así, Eureka tolera que un servicio arranque antes que otro.

## Rutas del Gateway

```text
http://localhost:8080/api/categorias/**  -> lb://categoria-service
http://localhost:8080/api/productos/**   -> lb://producto-service
http://localhost:8080/api/clientes/**    -> lb://cliente-service
http://localhost:8080/api/pedidos/**     -> lb://pedido-service
```

También se puede usar el nombre lógico:

```text
http://localhost:8080/cliente-service/api/clientes
http://localhost:8080/pedido-service/api/pedidos
```

## Base de datos

Ejecuta los scripts de `db/` en orden. En `dev` basta con crear las bases vacías porque
Hibernate usa `ddl-auto=update`; los scripts completos son necesarios para `prod`.

## Pruebas

```powershell
cd gateway-service;   .\mvnw.cmd test
cd ..\categoria-service; .\mvnw.cmd test
cd ..\cliente-service;   .\mvnw.cmd test
cd ..\producto-service;  .\mvnw.cmd test
cd ..\pedido-service;    .\mvnw.cmd test
```

Cada servicio incluye su propio `mvnw` y `mvnw.cmd`, por lo que no necesitas instalar
Maven.
