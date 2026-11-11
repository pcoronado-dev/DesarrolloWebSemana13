package com.tecsup.services;

import java.util.List;

import com.tecsup.exception.DuplicateResourceException;
import com.tecsup.exception.ResourceNotFoundException;
import com.tecsup.model.Cliente;
import com.tecsup.repository.ClienteRepository;

import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    private final ClienteRepository repo;

    public ClienteService(ClienteRepository repo) {
        this.repo = repo;
    }

    public List<Cliente> listar() {
        return repo.findAll();
    }

    public Cliente obtener(Long id) {
        return repo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
    }

    public Cliente guardar(Cliente cliente) {
        if (repo.existsByNroDni(cliente.getNroDni())) {
            throw new DuplicateResourceException(
                    "Ya existe un cliente con el DNI: " + cliente.getNroDni());
        }
        if (cliente.getEstado() == null || cliente.getEstado().isBlank()) {
            cliente.setEstado("ACTIVO");
        }
        return repo.save(cliente);
    }

    public Cliente actualizar(Long id, Cliente datos) {
        Cliente existente = obtener(id);

        if (repo.existsByNroDniAndIdNot(datos.getNroDni(), id)) {
            throw new DuplicateResourceException(
                    "Ya existe un cliente con el DNI: " + datos.getNroDni());
        }

        existente.setNombre(datos.getNombre());
        existente.setApellidos(datos.getApellidos());
        existente.setDireccion(datos.getDireccion());
        existente.setTelefono(datos.getTelefono());
        existente.setNroDni(datos.getNroDni());
        existente.setEstado(datos.getEstado());
        return repo.save(existente);
    }

    public void eliminar(Long id) {
        Cliente cliente = obtener(id);
        repo.delete(cliente);
    }
}
