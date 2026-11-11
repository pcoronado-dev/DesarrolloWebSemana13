# Evidencia de la prueba de aceptación - GLAB-S13

## Entorno

| Elemento | Valor |
| --- | --- |
| Sistema operativo | Windows 11 |
| JDK | 26.0.2.1 (compilación `release 25`) |
| Spring Boot | 3.5.15 |
| Spring Cloud | 2025.0.0 |
| Spring Cloud Gateway | 4.3.0 (`spring-cloud-starter-gateway-server-webflux`) |
| Base de datos | H2 2.3.232 en memoria, modo MySQL (sustituto de MySQL 8.0) |
| Eureka | <http://localhost:8761> |
| Gateway | <http://localhost:8080> |
| Perfil | `dev` |
| Fecha de ejecución | 2026-11-11 |

> **Nota sobre la base de datos:** la ejecución registrada se realizó con H2 en modo
> compatibilidad MySQL porque en el equipo no se dispone de la contraseña del usuario
> `root` de MySQL. La configuración `dev`/`prod`, los scripts SQL y las dependencias
> siguen apuntando a MySQL 8.0; para reproducir con MySQL basta levantar los scripts de
> `microservicios/db/` y arrancar cada servicio sin los *overrides* de H2.
>
> El perfil usado fue `dev` (no `test`) para conservar el registro y el descubrimiento
> reales en Eureka. Solo se sobrescribió el datasource con H2.

## Servicios

| Servicio | Puerto | Estado |
| --- | ---: | --- |
| `eureka-server` | 8761 | UP |
| `gateway-service` | 8080 | UP |
| `categoria-service` | 8082 | UP |
| `cliente-service` | 8081 | UP |
| `producto-service` | 8083 | UP |
| `pedido-service` | 8084 | UP |

### Registro en Eureka

Salida de `GET http://localhost:8761/eureka/apps`:

| Servicio | Host | IP | Puerto | Estado |
| --- | --- | --- | ---: | --- |
| `CLIENTE-SERVICE` | DESKTOP-8618A78 | 192.168.1.73 | 8081 | UP |
| `PRODUCTO-SERVICE` | DESKTOP-8618A78 | 192.168.1.73 | 8083 | UP |
| `PEDIDO-SERVICE` | DESKTOP-8618A78 | 192.168.1.73 | 8084 | UP |
| `GATEWAY-SERVICE` | DESKTOP-8618A78 | 192.168.1.73 | 8080 | UP |
| `CATEGORIA-SERVICE` | DESKTOP-8618A78 | 192.168.1.73 | 8082 | UP |

## Rutas publicadas

Salida de `GET http://localhost:8080/actuator/gateway/routes` (solo rutas explícitas):

| Ruta | URI | Predicado |
| --- | --- | --- |
| `categoria-service` | `lb://categoria-service` | `Path=/api/categorias/**` |
| `producto-service` | `lb://producto-service` | `Path=/api/productos/**` |
| `cliente-service` | `lb://cliente-service` | `Path=/api/clientes/**` |
| `pedido-service` | `lb://pedido-service` | `Path=/api/pedidos/**` |

El endpoint también devuelve las rutas generadas por el Discovery Locator:

```text
ReactiveCompositeDiscoveryClient_CLIENTE-SERVICE   -> lb://CLIENTE-SERVICE
ReactiveCompositeDiscoveryClient_PRODUCTO-SERVICE  -> lb://PRODUCTO-SERVICE
ReactiveCompositeDiscoveryClient_PEDIDO-SERVICE    -> lb://PEDIDO-SERVICE
ReactiveCompositeDiscoveryClient_GATEWAY-SERVICE   -> lb://GATEWAY-SERVICE
ReactiveCompositeDiscoveryClient_CATEGORIA-SERVICE -> lb://CATEGORIA-SERVICE
```

## Casos verificados

| # | Caso | Resultado esperado | Resultado obtenido |
| ---: | --- | --- | --- |
| 1 | Eureka responde | `200 OK` | OK - `200` |
| 2 | `CATEGORIA-SERVICE` registrado | `UP` en Eureka | OK |
| 3 | `CLIENTE-SERVICE` registrado | `UP` en Eureka | OK |
| 4 | `PRODUCTO-SERVICE` registrado | `UP` en Eureka | OK |
| 5 | `PEDIDO-SERVICE` registrado | `UP` en Eureka | OK |
| 6 | `GATEWAY-SERVICE` registrado | `UP` en Eureka | OK |
| 7 | Health del Gateway | `200`, estado `UP` | OK |
| 8 | Endpoint de rutas | `200` | OK |
| 9 | Ruta `categoria-service` | Publicada | OK |
| 10 | Ruta `producto-service` | Publicada | OK |
| 11 | Ruta `cliente-service` | Publicada | OK |
| 12 | Ruta `pedido-service` | Publicada | OK |
| 13 | `GET /api/categorias` por Gateway | `200` | OK |
| 14 | `POST /api/categorias` por Gateway | `201` | OK - id 2 |
| 15 | `POST /api/clientes` por Gateway | `201` | OK - DNI `82976491` |
| 16 | `POST /api/productos` por Gateway | `201` | OK |
| 17 | Producto enriquecido con categoría | `categoria.id` correcto | OK - categoría 2 por Feign |
| 18 | `POST /api/pedidos` por Gateway | `201` | OK |
| 19 | Pedido enriquecido con cliente | `cliente.id` correcto | OK |
| 20 | Pedido enriquecido con producto | `producto.id` correcto | OK |
| 21 | Subtotal del pedido | `7000.0` | OK |
| 22 | Estado inicial | `PENDIENTE` | OK |
| 23 | `GET /api/pedidos/{id}` por Gateway | `200` con datos remotos | OK |
| 24 | Cambio a `ATENDIDO` | `200` | OK |
| 25 | Cambio a `ANULADO` | `200` | OK |
| 26 | Transición `ANULADO -> ATENDIDO` | `400 Bad Request` | OK - `400` |
| 27 | Pedido con `cantidad=0` | `400 Bad Request` | OK - `400` |
| 28 | Pedido con cliente inexistente | `404 Not Found` | OK - `404` |
| 29 | Cliente con DNI de 7 dígitos | `400 Bad Request` | OK - `400` |
| 30 | `GET /cliente-service/api/clientes` (locator) | `200` | OK |
| 31 | `GET /pedido-service/api/pedidos` (locator) | `200` | OK |

**Resultado global: 31 comprobaciones exitosas, 0 fallidas.**

## Respuesta representativa

`GET http://localhost:8080/api/pedidos` a través del Gateway:

```json
{
  "id": 1,
  "clienteId": 1,
  "productoId": 1,
  "cantidad": 2,
  "estado": "ANULADO",
  "fecha": "2026-11-11T17:07:27.304026",
  "subtotal": 7000.0,
  "cliente": {
    "id": 1,
    "nombre": "Carlos",
    "apellidos": "Ramirez Torres",
    "direccion": "Av. Javier Prado 123, Lima",
    "telefono": "987654321",
    "nroDni": "43545674",
    "estado": "ACTIVO"
  },
  "producto": {
    "id": 1,
    "nombre": "Laptop Lenovo ThinkPad",
    "precio": 3500.0,
    "stock": 15,
    "categoriaId": 1
  }
}
```

El pedido se consultó a través del puerto 8080, pero los datos de `cliente` y `producto`
fueron completados por `pedido-service` mediante OpenFeign y Eureka. El estado `ANULADO`
corresponde a la secuencia de cambios de estado ejecutada por la prueba.

> Los identificadores y el DNI cambian en cada ejecución. La respuesta anterior se
> incluye como ejemplo del enriquecimiento por Feign; la tabla de casos registra los
> valores de la última corrida.

## Ejecución

```powershell
# 1. Levantar los seis servicios con H2 en memoria
powershell -ExecutionPolicy Bypass -File .\scripts\start-local-h2.ps1

# 2. Ejecutar la prueba de aceptación
powershell -ExecutionPolicy Bypass -File .\scripts\prueba-aceptacion.ps1

# 3. Detener los servicios
powershell -ExecutionPolicy Bypass -File .\scripts\stop-local.ps1
```

Salida completa de la ejecución:

```text
============================================================
 1. Servicios registrados en Eureka
============================================================
OK   Eureka responde 200
OK   CATEGORIA-SERVICE registrado -> CLIENTE-SERVICE, PRODUCTO-SERVICE, PEDIDO-SERVICE, GATEWAY-SERVICE, CATEGORIA-SERVICE
OK   CLIENTE-SERVICE registrado -> CLIENTE-SERVICE, PRODUCTO-SERVICE, PEDIDO-SERVICE, GATEWAY-SERVICE, CATEGORIA-SERVICE
OK   PRODUCTO-SERVICE registrado -> CLIENTE-SERVICE, PRODUCTO-SERVICE, PEDIDO-SERVICE, GATEWAY-SERVICE, CATEGORIA-SERVICE
OK   PEDIDO-SERVICE registrado -> CLIENTE-SERVICE, PRODUCTO-SERVICE, PEDIDO-SERVICE, GATEWAY-SERVICE, CATEGORIA-SERVICE
OK   GATEWAY-SERVICE registrado -> CLIENTE-SERVICE, PRODUCTO-SERVICE, PEDIDO-SERVICE, GATEWAY-SERVICE, CATEGORIA-SERVICE

============================================================
 2. Gateway y rutas publicadas
============================================================
OK   Gateway health UP
OK   Endpoint de rutas responde 200
OK   Ruta categoria-service publicada
OK   Ruta producto-service publicada
OK   Ruta cliente-service publicada
OK   Ruta pedido-service publicada

============================================================
 3. CRUD a traves del Gateway (puerto 8080)
============================================================
OK   GET /api/categorias
OK   POST /api/categorias -> 201
OK   POST /api/clientes -> DNI 82976491
OK   POST /api/productos
OK   Producto enriquecido con categoria via Feign -> categoriaId 2
OK   POST /api/pedidos
OK   Pedido enriquecido con cliente
OK   Pedido enriquecido con producto
OK   Subtotal calculado -> 7000.0
OK   Estado inicial PENDIENTE
OK   GET /api/pedidos/{id}
OK   PATCH estado a ATENDIDO
OK   PATCH estado a ANULADO
OK   Transicion ANULADO -> ATENDIDO rechazada -> 400

============================================================
 4. Casos de error a traves del Gateway
============================================================
OK   Pedido con cantidad 0 devuelve 400 -> 400
OK   Pedido con cliente inexistente devuelve 404 -> 404
OK   Cliente con DNI de 7 digitos devuelve 400 -> 400

============================================================
 5. Descubrimiento por nombre logico (locator)
============================================================
OK   GET /cliente-service/api/clientes
OK   GET /pedido-service/api/pedidos

============================================================
 Resultado: 31 comprobaciones exitosas, 0 fallidas
============================================================
```

## Pruebas automatizadas del Gateway

```text
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Las dos pruebas verifican:

1. Que el contexto reactive de Spring Boot carga correctamente.
2. Que se definen las cuatro rutas: `categoria-service`, `producto-service`,
   `cliente-service` y `pedido-service`.

## Evidencias sugeridas para el informe

1. Panel de Eureka con los cinco servicios de negocio y el Gateway en estado `UP`.
2. `GET /actuator/gateway/routes` mostrando las cuatro rutas `lb://`.
3. `GET /actuator/health` del Gateway en `UP`.
4. CRUD de clientes a través de `http://localhost:8080/api/clientes`.
5. CRUD de pedidos a través de `http://localhost:8080/api/pedidos`.
6. Pedido enriquecido con cliente y producto por Feign.
7. Acceso por nombre lógico: `/cliente-service/api/clientes` y
   `/pedido-service/api/pedidos`.
8. Casos de error `400` y `404` a través del Gateway.
9. Ejecución de `scripts/prueba-aceptacion.ps1` con 31 comprobaciones exitosas.
10. Ejecución de `mvnw test` en `gateway-service`.
