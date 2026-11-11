package com.tecsup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.tecsup.client.ClienteClient;
import com.tecsup.client.ProductoClient;
import com.tecsup.dto.ClienteDTO;
import com.tecsup.dto.PedidoRequestDTO;
import com.tecsup.dto.PedidoResponseDTO;
import com.tecsup.dto.ProductoDTO;
import com.tecsup.exception.BusinessRuleException;
import com.tecsup.exception.ResourceNotFoundException;
import com.tecsup.services.PedidoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class PedidoServiceApplicationTests {

    @Autowired
    private PedidoService service;

    @MockitoBean
    private ClienteClient clienteClient;

    @MockitoBean
    private ProductoClient productoClient;

    @BeforeEach
    void configurarClientesRemotos() {
        when(clienteClient.obtenerCliente(1L)).thenReturn(
                new ClienteDTO(1L, "Carlos", "Ramirez", "Av. Lima 100",
                        "987654321", "12345678", "ACTIVO"));

        when(productoClient.obtenerProducto(1L)).thenReturn(
                new ProductoDTO(1L, "Laptop", 2500.0, 10, 1L));
    }

    @Test
    void contextLoads() {
        assertNotNull(service);
    }

    @Test
    void registraPedidoYCompletaClienteProductoYSubtotal() {
        PedidoResponseDTO respuesta = service.guardar(new PedidoRequestDTO(1L, 1L, 2));

        assertNotNull(respuesta.id());
        assertEquals("PENDIENTE", respuesta.estado());
        assertEquals("Carlos", respuesta.cliente().nombre());
        assertEquals("Laptop", respuesta.producto().nombre());
        assertEquals(5000.0, respuesta.subtotal());
    }

    @Test
    void eliminarAnulaElPedidoSinBorrarlo() {
        PedidoResponseDTO creado = service.guardar(new PedidoRequestDTO(1L, 1L, 1));

        PedidoResponseDTO anulado = service.anular(creado.id());

        assertEquals("ANULADO", anulado.estado());
        assertEquals("ANULADO", service.obtener(creado.id()).estado());
    }

    @Test
    void noPermitePasarDeAnuladoAAtendido() {
        PedidoResponseDTO creado = service.guardar(new PedidoRequestDTO(1L, 1L, 1));
        service.anular(creado.id());

        assertThrows(BusinessRuleException.class,
                () -> service.cambiarEstado(creado.id(), "ATENDIDO"));
    }

    @Test
    void lanzaErrorCuandoElPedidoNoExiste() {
        assertThrows(ResourceNotFoundException.class, () -> service.obtener(999L));
    }
}
