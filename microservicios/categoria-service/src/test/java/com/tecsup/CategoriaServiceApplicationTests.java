package com.tecsup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tecsup.exception.ResourceNotFoundException;
import com.tecsup.model.Categoria;
import com.tecsup.services.CategoriaService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CategoriaServiceApplicationTests {

    @Autowired
    private CategoriaService service;

    @Test
    void contextLoads() {
        assertNotNull(service);
    }

    @Test
    void guardaYObtieneUnaCategoria() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Tecnologia");
        categoria.setEstado("ACTIVO");

        Categoria guardada = service.guardar(categoria);

        assertNotNull(guardada.getId());
        assertEquals("Tecnologia", service.obtener(guardada.getId()).getNombre());
    }

    @Test
    void lanzaErrorCuandoLaCategoriaNoExiste() {
        assertThrows(ResourceNotFoundException.class, () -> service.obtener(999L));
    }
}
