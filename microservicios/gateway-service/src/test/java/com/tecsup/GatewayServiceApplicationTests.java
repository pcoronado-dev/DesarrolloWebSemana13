package com.tecsup;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GatewayServiceApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Test
    void contextLoads() {
        assertNotNull(context);
    }

    @Test
    void defineLasRutasDeLosCuatroServicios() {
        List<String> ids = routeDefinitionLocator.getRouteDefinitions()
                .map(RouteDefinition::getId)
                .collectList()
                .block(Duration.ofSeconds(10));

        assertNotNull(ids);
        assertTrue(ids.containsAll(List.of(
                "categoria-service",
                "producto-service",
                "cliente-service",
                "pedido-service")), "Rutas configuradas: " + ids);
    }
}
