package com.tecsup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tecsup.exception.DuplicateResourceException;
import com.tecsup.model.Cliente;
import com.tecsup.services.ClienteService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ClienteServiceApplicationTests {

    @Autowired
    private ClienteService service;

    @Test
    void contextLoads() {
        assertNotNull(service);
    }

    @Test
    void guardaYObtieneUnCliente() {
        Cliente cliente = cliente("12345678", "juan@correo.com");

        Cliente guardado = service.guardar(cliente);

        assertNotNull(guardado.getId());
        assertEquals("Juan", service.obtener(guardado.getId()).getNombre());
    }

    @Test
    void rechazaDniDuplicado() {
        service.guardar(cliente("87654321", "maria@correo.com"));

        Cliente repetido = cliente("87654321", "otro@correo.com");

        assertThrows(DuplicateResourceException.class, () -> service.guardar(repetido));
    }

    private Cliente cliente(String dni, String correo) {
        Cliente cliente = new Cliente();
        cliente.setNombre("Juan");
        cliente.setApellidos("Perez Gomez");
        cliente.setDireccion("Av. Javier Prado 123, Lima");
        cliente.setTelefono("987654321");
        cliente.setNroDni(dni);
        cliente.setEstado("ACTIVO");
        return cliente;
    }
}
