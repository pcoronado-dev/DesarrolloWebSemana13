# Informe de laboratorio GLAB-S13

**Curso:** Desarrollo de Aplicaciones Web  
**Tema:** Service Discovery y Gateway  
**Laboratorio:** GLAB-S13  
**Alumno:** Coello Palomino, Ricardo  
**Sección:** 4 - C24 - Sección A B  
**Fecha:** 11/11/2026

---

## 1. Capacidades

Implementar *service discovery* y *API Gateway* en una arquitectura de microservicios,
publicando los servicios de negocio a través de un único punto de entrada y resolviendo
sus ubicaciones dinámicamente mediante Eureka.

## 2. Seguridad

Se respetaron las normas del laboratorio: no se manipuló hardware ni conexiones de red,
no se ingresaron alimentos ni bebidas, y la mesa de trabajo quedó limpia y ordenada.

## 3. Fundamento teórico

### 3.1 Service Discovery

En una arquitectura de microservicios las instancias cambian de host y de puerto con
frecuencia. En lugar de codificar URLs fijas, cada servicio se registra en un **registro
de servicios** (Eureka Server) con un nombre lógico. Los consumidores consultan ese
registro y obtienen las instancias disponibles. Esto permite escalar horizontalmente y
cambiar la infraestructura sin recompilar a los clientes.

### 3.2 API Gateway

El **API Gateway** es el único punto de entrada para los clientes externos. Se encarga de:

- Enrutar cada petición al microservicio correspondiente.
- Resolver el nombre lógico del servicio contra Eureka (`lb://`).
- Aplicar preocupaciones transversales: autenticación, autorización, CORS, límites de
  tráfico, logging, métricas y reescritura de rutas.
- Ocultar la topología interna y reducir el acoplamiento del cliente.

### 3.3 Spring Cloud Gateway

Spring Cloud Gateway es el componente reactivo de Spring Cloud para construir gateways.
Se configura con **rutas** compuestas por:

- `id`: identificador de la ruta.
- `uri`: destino, por ejemplo `lb://cliente-service`.
- `predicates`: condiciones que deben cumplirse, por ejemplo `Path=/api/clientes/**`.
- `filters`: transformaciones opcionales (reescritura, cabeceras, límites, etc.).

El **Discovery Locator** puede generar rutas automáticamente a partir de los servicios
registrados en Eureka, usando el nombre del servicio como prefijo.

### 3.4 Spring Cloud LoadBalancer

Cuando la URI de una ruta usa el esquema `lb://`, Spring Cloud LoadBalancer consulta a
Eureka, obtiene las instancias `UP` del servicio y elige una para reenviar la petición.
Así no se hardcodean IPs ni puertos.

## 4. Normas empleadas

No aplica para este laboratorio.

## 5. Recursos

- Computadora con Windows 11.
- JDK 26 (compilación con `release 25`).
- Spring Boot 3.5.15 y Spring Cloud 2025.0.0.
- Maven Wrapper incluido en cada proyecto.
- MySQL 8 (configuración `dev`/`prod`) y H2 en memoria (demostración y pruebas).
- Eureka Server, Spring Cloud Gateway, Spring Cloud LoadBalancer, OpenFeign.
- Postman y PowerShell para las pruebas.

## 6. Metodología

El laboratorio se desarrolló de forma individual. Se reutilizaron los microservicios del
laboratorio anterior (`categoria-service`, `producto-service`, `cliente-service` y
`pedido-service`), se agregó `gateway-service` y se verificó el flujo completo a través
del puerto 8080.

## 7. Procedimiento

### 7.1 Creación del proyecto `gateway-service`

Se creó un proyecto Maven con las siguientes características:

| Parámetro | Valor |
| --- | --- |
| Project | Maven |
| Language | Java |
| Spring Boot | 3.5.15 |
| Group | `com.tecsup` |
| Artifact | `gateway-service` |
| Package | `com.tecsup` |
| Packaging | `jar` |
| Java | 25 |

Dependencias:

- Spring Cloud Gateway (`spring-cloud-starter-gateway-server-webflux`).
- Eureka Discovery Client.
- Spring Cloud LoadBalancer.
- Spring Boot Actuator.
- Spring Boot Test.

El proyecto se ubicó dentro de la carpeta `microservicios`, junto a los demás servicios.

### 7.2 Dependencias del `pom.xml`

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2025.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
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
</dependencies>
```

> El enunciado usa `spring-cloud-starter-gateway`. En Spring Cloud 2025.0.0 ese starter
> sigue existiendo como alias deprecado; se usó el starter vigente
> `spring-cloud-starter-gateway-server-webflux`.

### 7.3 Clase principal

```java
package com.tecsup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
```

`@EnableDiscoveryClient` registra el Gateway en Eureka y habilita la resolución de las
rutas `lb://`.

### 7.4 Configuración `application.yml`

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

logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    org.springframework.cloud.loadbalancer: DEBUG

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka}

management:
  endpoints:
    web:
      exposure:
        include: health,info,gateway
  endpoint:
    health:
      show-details: always
    gateway:
      access: read-only
```

> En Spring Cloud Gateway 4.3 las claves del enunciado
> (`spring.cloud.gateway.routes`) siguen funcionando como alias, pero el namespace
> recomendado es `spring.cloud.gateway.server.webflux.*`. Se usó este último para evitar
> advertencias de deprecación.

### 7.5 Ejecución de los servicios

Se ejecutaron los siguientes servicios, en este orden:

1. `eureka-server` (`:8761`).
2. `categoria-service` (`:8082`).
3. `cliente-service` (`:8081`).
4. `producto-service` (`:8083`).
5. `pedido-service` (`:8084`).
6. `gateway-service` (`:8080`).

Para la demostración sin MySQL se usó H2 en memoria con el perfil `dev`, conservando el
registro real en Eureka:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-local-h2.ps1
```

### 7.6 Verificación en Eureka

En <http://localhost:8761> se verificó el registro de los cuatro microservicios de negocio y
del Gateway. El servidor Eureka no se registra a sí mismo:

| Servicio | Puerto | Estado |
| --- | ---: | --- |
| `GATEWAY-SERVICE` | 8080 | UP |
| `CATEGORIA-SERVICE` | 8082 | UP |
| `CLIENTE-SERVICE` | 8081 | UP |
| `PRODUCTO-SERVICE` | 8083 | UP |
| `PEDIDO-SERVICE` | 8084 | UP |

### 7.7 Verificación de las rutas

```powershell
Invoke-RestMethod http://localhost:8080/actuator/gateway/routes
```

Resultado: las cuatro rutas explícitas (`categoria-service`, `producto-service`,
`cliente-service`, `pedido-service`) y las rutas generadas por el Discovery Locator.

### 7.8 Prueba de los endpoints públicos

```powershell
Invoke-RestMethod http://localhost:8080/api/categorias
Invoke-RestMethod http://localhost:8080/api/productos
Invoke-RestMethod http://localhost:8080/api/clientes
Invoke-RestMethod http://localhost:8080/api/pedidos
```

Todas las peticiones se atendieron correctamente a través del puerto 8080.

## 8. Actividad: publicar `cliente-service` y `pedido-service`

La actividad solicitada se resolvió agregando dos rutas explícitas al Gateway:

```yaml
- id: cliente-service
  uri: lb://cliente-service
  predicates:
    - Path=/api/clientes/**

- id: pedido-service
  uri: lb://pedido-service
  predicates:
    - Path=/api/pedidos/**
```

Con ello, los endpoints de clientes y pedidos quedan publicados en:

```text
http://localhost:8080/api/clientes
http://localhost:8080/api/clientes/{id}
http://localhost:8080/api/pedidos
http://localhost:8080/api/pedidos/{id}
http://localhost:8080/api/pedidos/{id}/estado
```

Además, el Discovery Locator permite acceder por nombre lógico:

```text
http://localhost:8080/cliente-service/api/clientes
http://localhost:8080/pedido-service/api/pedidos
```

### 8.1 Prueba de cliente a través del Gateway

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/clientes `
  -ContentType 'application/json' `
  -Body '{"nombre":"Carlos","apellidos":"Ramirez Torres","direccion":"Av. Javier Prado 123, Lima","telefono":"987654321","nroDni":"82976491","estado":"ACTIVO"}'
```

Resultado: `201 Created`, con `id` generado por `cliente-service`.

### 8.2 Prueba de pedido a través del Gateway

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/pedidos `
  -ContentType 'application/json' `
  -Body '{"clienteId":1,"productoId":1,"cantidad":2}'
```

Resultado: `201 Created`, estado `PENDIENTE`, `subtotal = 7000.0`, con `cliente` y
`producto` completados por OpenFeign.

### 8.3 Prueba de aceptación automatizada

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\prueba-aceptacion.ps1
```

Resultado:

```text
Resultado: 31 comprobaciones exitosas, 0 fallidas
```

La prueba verifica el registro en Eureka, las rutas del Gateway, el CRUD a través del
puerto 8080, el enriquecimiento por Feign, los cambios de estado, los errores `400`/`404`
y el acceso por nombre lógico. La evidencia completa está en
`docs/evidencia-prueba-aceptacion.md`.

## 9. Resultados

| Aspecto | Resultado |
| --- | --- |
| Gateway registrado en Eureka | Sí, `GATEWAY-SERVICE` en `:8080` |
| Rutas explícitas | 4 (`categorias`, `productos`, `clientes`, `pedidos`) |
| Ruta de `cliente-service` | `lb://cliente-service` |
| Ruta de `pedido-service` | `lb://pedido-service` |
| Acceso por nombre lógico | Funciona con el Discovery Locator |
| CRUD por Gateway | Funciona en los cuatro servicios |
| Enriquecimiento Feign | Cliente y producto completados |
| Errores `400` y `404` | Propagados correctamente |
| Prueba de aceptación | 31/31 comprobaciones |
| Pruebas del Gateway | 2/2 |

## 10. Conclusiones

1. **El API Gateway reduce el acoplamiento del cliente con la infraestructura.** Antes el
   cliente necesitaba conocer los puertos 8081, 8082, 8083 y 8084; ahora solo conoce el
   puerto 8080. La ubicación real de cada servicio puede cambiar sin afectar a los
   consumidores.

2. **Eureka y `lb://` permiten un enrutamiento dinámico.** Las rutas no contienen IPs ni
   puertos. Spring Cloud LoadBalancer resuelve el nombre lógico contra las instancias
   registradas y puede distribuir la carga si se levantan varias instancias del mismo
   servicio.

3. **El Gateway es el lugar adecuado para las preocupaciones transversales.** Seguridad,
   CORS, rate limiting, logging y métricas se pueden centralizar como filtros, evitando
   duplicar esa lógica en cada microservicio. En este laboratorio se configuraron rutas,
   logs y Actuator.

4. **El Gateway no reemplaza la resiliencia de los servicios.** Si no hay instancias
   disponibles, la ruta devuelve `503`; si el backend falla, el error se propaga. Para
   producción se deben agregar timeouts, reintentos controlados, circuit breaker y un
   formato de error uniforme.

5. **Las rutas explícitas son preferibles al Discovery Locator en producción.** El locator
   es útil para pruebas y diagnóstico, pero expone todos los servicios registrados. Las
   rutas explícitas documentan el contrato público y permiten aplicar filtros por
   servicio. Además, Spring Cloud Gateway 4.3 migró al namespace
   `spring.cloud.gateway.server.webflux.*`, por lo que conviene usar la configuración
   vigente.

## 11. Recomendaciones

- Deshabilitar el Discovery Locator en producción y mantener solo las rutas necesarias.
- Agregar autenticación y autorización en el Gateway (por ejemplo, validación de JWT).
- Configurar timeouts, reintentos y circuit breaker para tolerar fallos de los servicios.
- Centralizar CORS y los encabezados de correlación (`traceId`) en el Gateway.
- Monitorear el Gateway con Actuator, métricas y logs estructurados.
- Versionar las rutas públicas (`/api/v1/...`) para facilitar la evolución del contrato.
- Mantener las dependencias alineadas con la versión de Spring Cloud usada.
