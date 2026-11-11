package com.tecsup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway del laboratorio.
 *
 * <p>Es el unico punto de entrada para los clientes. Resuelve los nombres logicos de los
 * microservicios contra Eureka y enruta las peticiones a traves de Spring Cloud Gateway.</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
