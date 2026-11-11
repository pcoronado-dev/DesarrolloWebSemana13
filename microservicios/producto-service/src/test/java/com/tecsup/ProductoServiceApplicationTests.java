package com.tecsup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.tecsup.client.CategoriaClient;
import com.tecsup.dto.CategoriaDTO;
import com.tecsup.exception.ResourceNotFoundException;
import com.tecsup.model.Producto;
import com.tecsup.services.ProductoService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class ProductoServiceApplicationTests {

    @Autowired
    private ProductoService service;

    @MockitoBean
    private CategoriaClient categoriaClient;

    @Test
    void contextLoads() {
        assertNotNull(service);
    }

    @Test
    void guardaYCompletaLaCategoriaDesdeFeign() {
        CategoriaDTO categoria = new CategoriaDTO();
        categoria.setId(1L);
        categoria.setNombre("Tecnologia");
        categoria.setEstado("ACTIVO");

        when(categoriaClient.obtenerCategoria(1L)).thenReturn(categoria);

        Producto producto = new Producto();
        producto.setNombre("Laptop");
        producto.setPrecio(2500.0);
        producto.setStock(10);
        producto.setCategoriaId(1L);

        Producto guardado = service.guardar(producto);

        assertNotNull(guardado.getId());
        assertEquals("Tecnologia", guardado.getCategoria().getNombre());
    }

    @Test
    void lanzaErrorCuandoElProductoNoExiste() {
        assertThrows(ResourceNotFoundException.class, () -> service.obtener(999L));
    }
}
