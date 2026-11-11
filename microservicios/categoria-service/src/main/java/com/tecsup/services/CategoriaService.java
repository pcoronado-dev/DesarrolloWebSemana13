package com.tecsup.services;

import java.util.List;

import com.tecsup.exception.ResourceNotFoundException;
import com.tecsup.model.Categoria;
import com.tecsup.repository.CategoriaRepository;

import org.springframework.stereotype.Service;

@Service
public class CategoriaService {

    private final CategoriaRepository repo;

    public CategoriaService(CategoriaRepository repo) {
        this.repo = repo;
    }

    public List<Categoria> listar() {
        return repo.findAll();
    }

    public Categoria guardar(Categoria categoria) {
        return repo.save(categoria);
    }

    public Categoria obtener(Long id) {
        return repo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Categoria no encontrada con id: " + id));
    }

    public Categoria actualizar(Long id, Categoria datos) {
        Categoria existente = obtener(id);
        existente.setNombre(datos.getNombre());
        existente.setEstado(datos.getEstado());
        return repo.save(existente);
    }

    public void eliminar(Long id) {
        Categoria categoria = obtener(id);
        repo.delete(categoria);
    }
}
