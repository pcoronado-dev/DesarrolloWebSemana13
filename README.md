# GLAB-S13 - Service Discovery y API Gateway

Implementación del laboratorio **GLAB-S13: Service Discovery y Gateway** del curso
*Desarrollo de Aplicaciones Web*. El proyecto reutiliza los microservicios del laboratorio
anterior y agrega un **API Gateway** que se convierte en el único punto de entrada para los
clientes.

El caso de negocio sigue siendo una tienda que administra **categorías**, **productos**,
**clientes** y **pedidos**. Cada microservicio conserva su propia base de datos y su propio
puerto, pero ahora todas las peticiones de negocio se publican a través de
`gateway-service` en el puerto **8080**. El Gateway resuelve los nombres lógicos contra
**Eureka Server** usando `lb://` y **Spring Cloud LoadBalancer**.

## Objetivo

- Implementar un API Gateway con Spring Cloud Gateway.
- Registrar el Gateway como cliente de Eureka.
- Publicar los servicios `categoria-service`, `producto-service`, `cliente-service` y
  `pedido-service` detrás de un único puerto.
- Cumplir la actividad del laboratorio: publicar **`cliente-service`** y
  **`pedido-service`** mediante el Gateway.

## Arquitectura

```text
                         +---------------------------+
                         |      gateway-service      |
   Postman / cliente --->|          :8080            |
                         |  Spring Cloud Gateway     |
                         +-------------+-------------+
                                       |
                     lb:// (Spring Cloud LoadBalancer)
                                       |
             +-------------------------+-------------------------+
             |              |                    |                 |
             v              v                    v                 v
   +---------+----+ +-------+------+   +---------+------+  +-------+-------+
   | categoria-   | | producto-    |   | cliente-       |  | pedido-       |
   | service:8082 | | service:8083 |   | service:8081   |  | service:8084  |
   +------+-------+ +------+-------+   +-------+--------+  +-------+-------+
          |                |                   |                   |
          v                v                   v                   v
   bd_categoria_dev  bd_producto_dev     bd_cliente_dev      bd_pedido_dev

                    Todos se registran en Eureka Server :8761
```

Relaciones internas que se mantienen entre servicios:

```text
producto-service --Feign--> categoria-service   GET /api/categorias/{id}
pedido-service   --Feign--> cliente-service     GET /api/clientes/{id}
pedido-service   --Feign--> producto-service    GET /api/productos/{id}
```

El Gateway no reemplaza la comunicación Feign: solo es la puerta de entrada para los
clientes externos. Los microservicios siguen hablando entre sí por nombre lógico.

## Servicios y puertos

| Microservicio | DEV | PROD | Responsabilidad |
| --- | ---: | ---: | --- |
| `eureka-server` | 8761 | 8761 | Registro y descubrimiento |
| `gateway-service` | 8080 | 9080 | API Gateway y enrutamiento |
| `cliente-service` | 8081 | 9081 | CRUD de clientes |
| `categoria-service` | 8082 | 9082 | CRUD de categorías |
| `producto-service` | 8083 | 9083 | CRUD de productos + Feign a categorías |
| `pedido-service` | 8084 | 9084 | Pedidos + Feign a clientes y productos |

## Rutas publicadas por el Gateway

| Ruta pública | Destino | URI |
| --- | --- | --- |
| `/api/categorias/**` | `categoria-service` | `lb://categoria-service` |
| `/api/productos/**` | `producto-service` | `lb://producto-service` |
| `/api/clientes/**` | `cliente-service` | `lb://cliente-service` |
| `/api/pedidos/**` | `pedido-service` | `lb://pedido-service` |

Además, el **Discovery Locator** está habilitado. Permite invocar cualquier servicio
registrado usando su nombre lógico como prefijo:

```text
http://localhost:8080/cliente-service/api/clientes
http://localhost:8080/pedido-service/api/pedidos
http://localhost:8080/producto-service/api/productos
```

Las rutas explícitas son las recomendadas para producción porque permiten controlar el
contrato público, aplicar filtros y evitar exponer servicios internos que no deberían ser
accesibles.

## Tecnologías

- Java 25 (compilado con JDK 26 usando `release 25`).
- Spring Boot 3.5.15.
- Spring Cloud 2025.0.0.
- Spring Cloud Gateway 4.3.0 (`spring-cloud-starter-gateway-server-webflux`).
- Spring Cloud Netflix Eureka Client.
- Spring Cloud LoadBalancer.
- Spring Boot Actuator.
- Spring Web MVC, Spring Data JPA, Validation y DevTools en los microservicios.
- Spring Cloud OpenFeign en `producto-service` y `pedido-service`.
- MySQL 8 en `dev`/`prod` y H2 en memoria para pruebas y demostración.
- Maven Wrapper incluido en cada proyecto.

> **Nota sobre Spring Cloud Gateway 4.3:** el starter clásico
> `spring-cloud-starter-gateway` y las claves `spring.cloud.gateway.routes` siguen
> funcionando como alias, pero están deprecados. Este proyecto usa el starter
> `spring-cloud-starter-gateway-server-webflux` y el namespace
> `spring.cloud.gateway.server.webflux.*`, que es el recomendado para Spring Cloud
> 2025.0.0.

## Estructura del proyecto

```text
DESARROLLOSEMANA13/
|-- docs/
|   |-- diagrama-microservicios.md
|   |-- evidencia-prueba-aceptacion.md
|   |-- informe-GLAB-S13.md
|   `-- enunciado/
|       `-- GLAB-S13-RCOELLO-2026-01.md
|-- scripts/
|   |-- start-local-h2.ps1
|   |-- stop-local.ps1
|   `-- prueba-aceptacion.ps1
|-- microservicios/
|   |-- eureka-server/
|   |-- gateway-service/
|   |-- categoria-service/
|   |-- producto-service/
|   |-- cliente-service/
|   |-- pedido-service/
|   |-- db/
|   |   |-- 01_crear_bases_datos.sql
|   |   |-- 02_categoria.sql
|   |   |-- 03_producto.sql
|   |   |-- 04_cliente.sql
|   |   |-- 05_pedido.sql
|   |   `-- 06_datos_prueba.sql
|   |-- postman/
|   |   |-- Gateway-S13.postman_collection.json
|   |   `-- ComunicacionServicios-S12.postman_collection.json
|   `-- README.md
`-- README.md
```

## Implementación del Gateway

### 1. Dependencias (`gateway-service/pom.xml`)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 2. Clase principal

```java
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
```

`@EnableDiscoveryClient` registra el Gateway en Eureka y habilita la resolución de
instancias para las rutas `lb://`.

### 3. Configuración (`application.yml`)

```yaml
server:
  port: ${SERVER_PORT:8080}

spring:
  application:
    name: gateway-service
  cloud:
    gateway:
      server:
        webflux:
          discovery:
            locator:
              enabled: true
              lower-case-service-id: true
          routes:
            - id: categoria-service
              uri: lb://categoria-service
              predicates:
                - Path=/api/categorias/**
            - id: producto-service
              uri: lb://producto-service
              predicates:
                - Path=/api/productos/**
            - id: cliente-service
              uri: lb://cliente-service
              predicates:
                - Path=/api/clientes/**
            - id: pedido-service
              uri: lb://pedido-service
              predicates:
                - Path=/api/pedidos/**

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka}
```

La configuración completa, incluido Actuator y niveles de log, está en
[`microservicios/gateway-service/src/main/resources/application.yml`](microservicios/gateway-service/src/main/resources/application.yml).

## Puesta en marcha

### Opción A: MySQL (configuración real del laboratorio)

1. Tener MySQL 8 en ejecución.
2. Crear las bases de datos y tablas:

```powershell
cd microservicios\db
mysql -u root -p --execute="source 01_crear_bases_datos.sql"
mysql -u root -p --execute="source 02_categoria.sql"
mysql -u root -p --execute="source 03_producto.sql"
mysql -u root -p --execute="source 04_cliente.sql"
mysql -u root -p --execute="source 05_pedido.sql"
mysql -u root -p --execute="source 06_datos_prueba.sql"   # opcional
```

3. Configurar las variables de entorno de cada servicio (`PROFILE_ACTIVE`, `SERVER_PORT`,
   `DB_URL`, `DB_USER`, `DB_PASSWORD`, `EUREKA_URL`) o editar los valores por defecto de
   `application-dev.yml`.
4. Arrancar en este orden, cada uno en su propia terminal:

```powershell
cd microservicios\eureka-server;    .\mvnw.cmd spring-boot:run
cd microservicios\categoria-service;.\mvnw.cmd spring-boot:run
cd microservicios\cliente-service;  .\mvnw.cmd spring-boot:run
cd microservicios\producto-service; .\mvnw.cmd spring-boot:run
cd microservicios\pedido-service;   .\mvnw.cmd spring-boot:run
cd microservicios\gateway-service;  .\mvnw.cmd spring-boot:run
```

5. Verificar el registro en <http://localhost:8761> y el estado del Gateway en
   <http://localhost:8080/actuator/health>.

### Opción B: H2 en memoria (demostración sin MySQL)

El script de apoyo levanta los seis servicios con H2 en modo MySQL y deja los logs en
`%TEMP%\opencode\s13`:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-local-h2.ps1
```

Para detenerlos:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\stop-local.ps1
```

> El script usa el perfil `dev` para conservar el descubrimiento real de Eureka y solo
> sobrescribe el datasource con H2. No usa el perfil `test`, porque ese perfil desactiva
> Eureka y las llamadas Feign fallarían con `503`.

## Endpoints a través del Gateway

| Método | Endpoint público | Servicio destino |
| --- | --- | --- |
| GET | `/api/categorias` | `categoria-service` |
| GET | `/api/categorias/{id}` | `categoria-service` |
| POST | `/api/categorias` | `categoria-service` |
| PUT | `/api/categorias/{id}` | `categoria-service` |
| DELETE | `/api/categorias/{id}` | `categoria-service` |
| GET | `/api/clientes` | `cliente-service` |
| GET | `/api/clientes/{id}` | `cliente-service` |
| POST | `/api/clientes` | `cliente-service` |
| PUT | `/api/clientes/{id}` | `cliente-service` |
| DELETE | `/api/clientes/{id}` | `cliente-service` |
| GET | `/api/productos` | `producto-service` |
| GET | `/api/productos/{id}` | `producto-service` |
| POST | `/api/productos` | `producto-service` |
| PUT | `/api/productos/{id}` | `producto-service` |
| DELETE | `/api/productos/{id}` | `producto-service` |
| GET | `/api/pedidos` | `pedido-service` |
| GET | `/api/pedidos/{id}` | `pedido-service` |
| POST | `/api/pedidos` | `pedido-service` |
| PATCH | `/api/pedidos/{id}/estado` | `pedido-service` |
| DELETE | `/api/pedidos/{id}` | `pedido-service` |
| GET | `/actuator/health` | Gateway |
| GET | `/actuator/gateway/routes` | Gateway |

Ejemplos:

```powershell
# Listar clientes a través del Gateway
Invoke-RestMethod http://localhost:8080/api/clientes

# Crear un cliente
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/clientes `
  -ContentType 'application/json' `
  -Body '{"nombre":"Carlos","apellidos":"Ramirez Torres","direccion":"Av. Javier Prado 123, Lima","telefono":"987654321","nroDni":"12345678","estado":"ACTIVO"}'

# Crear un pedido
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/pedidos `
  -ContentType 'application/json' `
  -Body '{"clienteId":1,"productoId":1,"cantidad":2}'
```

Respuesta de un pedido creado a través del Gateway (los datos de cliente y producto los
completa `pedido-service` por Feign):

```json
{
  "id": 1,
  "clienteId": 1,
  "productoId": 1,
  "cantidad": 2,
  "estado": "PENDIENTE",
  "fecha": "2026-11-11T17:07:27",
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

## Pruebas

### Pruebas unitarias y de contexto

```powershell
cd microservicios\gateway-service;  .\mvnw.cmd test
cd microservicios\eureka-server;    .\mvnw.cmd test
cd microservicios\categoria-service;.\mvnw.cmd test
cd microservicios\cliente-service;  .\mvnw.cmd test
cd microservicios\producto-service; .\mvnw.cmd test
cd microservicios\pedido-service;   .\mvnw.cmd test
```

| Proyecto | Pruebas |
| --- | ---: |
| `gateway-service` | 2 |
| `eureka-server` | 1 |
| `categoria-service` | 3 |
| `cliente-service` | 3 |
| `producto-service` | 3 |
| `pedido-service` | 5 |

Las pruebas del Gateway verifican que el contexto reactive carga y que se definen las
cuatro rutas. Las pruebas de los microservicios usan H2 y `@MockitoBean` para no depender
de MySQL ni de Eureka.

### Prueba de aceptación completa

Con los seis servicios levantados:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\prueba-aceptacion.ps1
```

El script comprueba el registro en Eureka, las rutas del Gateway, el CRUD a través del
puerto 8080, el enriquecimiento por Feign, los cambios de estado, los errores `400`/`404`
y el acceso por nombre lógico.

Resultado de la última ejecución: **31 comprobaciones exitosas, 0 fallidas**. La evidencia
completa está en
[`docs/evidencia-prueba-aceptacion.md`](docs/evidencia-prueba-aceptacion.md).

## Colección Postman

Importar
[`microservicios/postman/Gateway-S13.postman_collection.json`](microservicios/postman/Gateway-S13.postman_collection.json).
La colección contiene 34 peticiones:

1. **00 - Gateway**: health, rutas publicadas y acceso por nombre lógico.
2. **01 - Categoria**: CRUD completo.
3. **02 - Cliente**: CRUD y validaciones de DNI.
4. **03 - Producto**: CRUD y validación de categoría.
5. **04 - Pedido**: creación, consulta enriquecida, estados y errores.
6. **05 - Eureka**: servicios registrados.

Todas las peticiones de negocio apuntan a `{{baseUrl}}` =
`http://localhost:8080`.

## Conclusiones

1. **El API Gateway centraliza el punto de entrada.** El cliente deja de conocer los
   puertos y la ubicación de cada microservicio: solo conoce `http://localhost:8080`.
   Esto simplifica el contrato público y permite cambiar la topología interna sin
   recompilar a los consumidores.

2. **`lb://` + Eureka desacoplan el enrutamiento de la infraestructura.** Las rutas no
   contienen IPs ni puertos; Spring Cloud LoadBalancer obtiene las instancias registradas
   y elige una. Si se levanta una segunda instancia de `pedido-service`, el Gateway puede
   distribuir la carga sin cambiar la configuración.

3. **El Gateway es el lugar natural para preocupaciones transversales.** Autenticación,
   autorización, CORS, rate limiting, logging, métricas y reescritura de rutas pueden
   aplicarse como filtros globales o por ruta. En este laboratorio se configuraron rutas y
   logging; en un entorno real se agregarían filtros de seguridad y resiliencia.

4. **El Gateway no elimina los fallos de los servicios.** Si no hay instancias de
   `cliente-service` en Eureka, la ruta devuelve `503 Service Unavailable`. Para producción
   se deben agregar timeouts, reintentos controlados, circuit breaker y una respuesta de
   error uniforme; el Gateway solo enruta, no repara la disponibilidad del backend.

5. **Las rutas explícitas son preferibles al Discovery Locator en producción.** El locator
   es cómodo para laboratorio y diagnóstico, pero expone todos los servicios registrados.
   Las rutas explícitas documentan el contrato público y permiten aplicar filtros por
   servicio; además, Spring Cloud Gateway 4.3 migró al namespace
   `spring.cloud.gateway.server.webflux.*`, por lo que conviene usar la configuración
   vigente y no la deprecada.

## Preparación para la defensa

**¿Cómo funciona el Gateway con Eureka y qué pasa si un servicio no está disponible?**

> `gateway-service` se registra en Eureka y, además, consume el registro. Cada ruta usa
> `uri: lb://nombre-servicio`; Spring Cloud LoadBalancer resuelve ese nombre contra las
> instancias `UP` de Eureka y reenvía la petición al puerto real. El cliente solo llama a
> `http://localhost:8080/api/...`. Si el servicio no está registrado o no tiene instancias
> disponibles, el Gateway responde `503`; si el servicio está caído pero sigue en el
> registro, el error se propaga hasta que Eureka lo expulsa. Por eso se recomienda
> combinar el Gateway con timeouts, reintentos y circuit breaker.

**¿Por qué no llamar directamente a los microservicios?**

> Porque el cliente tendría que conocer seis puertos, seis hosts y seis contratos, y
> cualquier cambio de infraestructura rompería a los consumidores. El Gateway reduce ese
> acoplamiento, permite versionar rutas y centraliza seguridad, métricas y límites de
> tráfico.

**¿Qué diferencia hay entre las rutas explícitas y el Discovery Locator?**

> Las rutas explícitas declaran únicamente los endpoints públicos y permiten aplicar
> filtros por ruta. El locator genera automáticamente una ruta por cada servicio
> registrado (`/cliente-service/**`, `/pedido-service/**`, etc.); es útil para pruebas,
> pero expone servicios internos y no permite un contrato público curado.

## Notas y soluciones frecuentes

- **`503 Service Unavailable` al llamar al Gateway**: verifica que el servicio destino
  aparezca como `UP` en <http://localhost:8761> y que `spring.application.name` coincida
  con el nombre usado en `lb://`.
- **`404` en `/actuator/gateway/routes`**: el endpoint está deshabilitado por defecto.
  En Spring Boot 3.5 se habilita con `management.endpoint.gateway.access=read-only` y se
  expone con `management.endpoints.web.exposure.include=gateway`.
- **Puerto 8080 ocupado**: cambia `SERVER_PORT` o detén el proceso con
  `Get-NetTCPConnection -LocalPort 8080` y `Stop-Process -Id <PID>`.
- **El Gateway no arranca por conflicto MVC/WebFlux**: no agregues
  `spring-boot-starter-web` al Gateway; Spring Cloud Gateway WebFlux necesita un
  contenedor reactivo (Reactor Netty).
- **Las claves `spring.cloud.gateway.routes` emiten advertencia**: usa
  `spring.cloud.gateway.server.webflux.routes` en Spring Cloud 2025.0.0.
- **MySQL no disponible**: usa `scripts/start-local-h2.ps1` para la demostración con H2.
