# Diagrama de microservicios - GLAB-S13

## Vista de contexto

```text
                         +-----------------------------+
                         |  Postman / cliente externo  |
                         +--------------+--------------+
                                        |
                                        | HTTP :8080
                                        v
                         +-----------------------------+
                         |       gateway-service       |
                         |  Spring Cloud Gateway 4.3   |
                         |  @EnableDiscoveryClient     |
                         +--------------+--------------+
                                        |
                     lb:// (Spring Cloud LoadBalancer)
                                        |
          +-----------------+-----------+-----------+-----------------+
          |                 |                       |                 |
          v                 v                       v                 v
 +--------+-------+ +-------+--------+     +--------+-------+ +-------+--------+
 | categoria-     | | producto-      |     | cliente-       | | pedido-        |
 | service :8082  | | service :8083  |     | service :8081  | | service :8084  |
 +--------+-------+ +-------+--------+     +--------+-------+ +-------+--------+
          |                 |                       |                 |
          v                 v                       v                 v
   bd_categoria_dev  bd_producto_dev         bd_cliente_dev     bd_pedido_dev

          ^                 ^                       ^                 ^
          |                 |                       |                 |
          +-----------------+-----------+-----------+-----------------+
                                        |
                         +--------------+--------------+
                         |       eureka-server         |
                         |           :8761             |
                         |  registro y descubrimiento  |
                         +-----------------------------+
```

Todos los servicios de negocio, incluido el Gateway, se registran en Eureka. El Gateway
consume el registro para resolver las rutas `lb://`.

## Flujo de una petición

```text
1. El cliente llama a http://localhost:8080/api/pedidos
2. Gateway busca una ruta cuyo predicado Path coincida con /api/pedidos/**
3. La ruta declara uri: lb://pedido-service
4. LoadBalancer consulta Eureka y elige una instancia UP de PEDIDO-SERVICE
5. Gateway reenvía la petición a http://<host>:8084/api/pedidos
6. pedido-service responde y el Gateway devuelve la respuesta al cliente
```

El Gateway no modifica el path en este laboratorio: `/api/pedidos` llega igual al
microservicio, que ya expone ese prefijo.

## Flujo interno al crear un pedido

```text
Cliente
  |
  | POST /api/pedidos {clienteId, productoId, cantidad}
  v
gateway-service :8080
  |
  | lb://pedido-service
  v
pedido-service :8084
  |
  |-- Feign GET /api/clientes/{clienteId} ----> cliente-service :8081
  |
  |-- Feign GET /api/productos/{productoId} --> producto-service :8083
  |                                                   |
  |                                                   `-- Feign GET /api/categorias/{id}
  |                                                         -> categoria-service :8082
  |
  |-- Calcula subtotal = precio * cantidad
  |-- Guarda el pedido en bd_pedido_dev
  v
Respuesta enriquecida con cliente y producto
```

El Gateway solo interviene en el primer salto. Las llamadas Feign siguen ocurriendo
directamente entre microservicios, usando los nombres registrados en Eureka.

## Rutas explícitas

```text
/api/categorias/**  -> lb://categoria-service
/api/productos/**   -> lb://producto-service
/api/clientes/**    -> lb://cliente-service
/api/pedidos/**     -> lb://pedido-service
```

## Discovery Locator

Con `spring.cloud.gateway.server.webflux.discovery.locator.enabled=true`, el Gateway
genera además una ruta por cada servicio registrado:

```text
/cliente-service/**   -> lb://CLIENTE-SERVICE
/pedido-service/**    -> lb://PEDIDO-SERVICE
/producto-service/**  -> lb://PRODUCTO-SERVICE
/categoria-service/** -> lb://CATEGORIA-SERVICE
/gateway-service/**   -> lb://GATEWAY-SERVICE
```

Ejemplos:

```text
GET http://localhost:8080/cliente-service/api/clientes
GET http://localhost:8080/pedido-service/api/pedidos
```

En producción se recomienda deshabilitar el locator y mantener solo las rutas explícitas,
para no exponer servicios internos ni el propio Gateway.

## Puertos y bases de datos

| Servicio | DEV | PROD | Base de datos DEV |
| --- | ---: | ---: | --- |
| `eureka-server` | 8761 | 8761 | - |
| `gateway-service` | 8080 | 9080 | - |
| `cliente-service` | 8081 | 9081 | `bd_cliente_dev` |
| `categoria-service` | 8082 | 9082 | `bd_categoria_dev` |
| `producto-service` | 8083 | 9083 | `bd_producto_dev` |
| `pedido-service` | 8084 | 9084 | `bd_pedido_dev` |

## Dependencias entre proyectos

```text
gateway-service  -> eureka-client, loadbalancer, actuator
producto-service -> eureka-client, openfeign, loadbalancer, MySQL/H2
pedido-service   -> eureka-client, openfeign, loadbalancer, MySQL/H2
cliente-service  -> eureka-client, JPA, MySQL/H2
categoria-service-> eureka-client, JPA, MySQL/H2
eureka-server    -> eureka-server
```
