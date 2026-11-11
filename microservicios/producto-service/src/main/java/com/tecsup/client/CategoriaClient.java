package com.tecsup.client;

import com.tecsup.dto.CategoriaDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente Feign hacia categoria-service. El nombre del servicio es el registrado
 * en Eureka, por lo que no se escribe una URL ni un puerto fijo.
 */
@FeignClient(name = "categoria-service")
public interface CategoriaClient {

    @GetMapping("/api/categorias/{id}")
    CategoriaDTO obtenerCategoria(@PathVariable("id") Long id);
}
