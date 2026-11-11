package com.tecsup.services;

import java.util.List;

import com.tecsup.client.CategoriaClient;
import com.tecsup.dto.CategoriaDTO;
import com.tecsup.exception.ResourceNotFoundException;
import com.tecsup.exception.ServiceUnavailableException;
import com.tecsup.model.Producto;
import com.tecsup.repository.ProductoRepository;

import feign.FeignException;

import org.springframework.stereotype.Service;

@Service
public class ProductoService {

    private final ProductoRepository repo;
    private final CategoriaClient categoriaClient;

    public ProductoService(ProductoRepository repo, CategoriaClient categoriaClient) {
        this.repo = repo;
        this.categoriaClient = categoriaClient;
    }

    public List<Producto> listar() {
        List<Producto> productos = repo.findAll();
        productos.forEach(this::completarCategoria);
        return productos;
    }

    public Producto obtener(Long id) {
        Producto producto = repo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        completarCategoria(producto);
        return producto;
    }

    public Producto guardar(Producto producto) {
        validarCategoria(producto.getCategoriaId());
        Producto guardado = repo.save(producto);
        completarCategoria(guardado);
        return guardado;
    }

    public Producto actualizar(Long id, Producto datos) {
        Producto existente = obtener(id);
        validarCategoria(datos.getCategoriaId());

        existente.setNombre(datos.getNombre());
        existente.setPrecio(datos.getPrecio());
        existente.setStock(datos.getStock());
        existente.setCategoriaId(datos.getCategoriaId());

        Producto guardado = repo.save(existente);
        completarCategoria(guardado);
        return guardado;
    }

    public void eliminar(Long id) {
        Producto producto = obtener(id);
        repo.delete(producto);
    }

    /**
     * Valida que la categoria exista en categoria-service antes de guardar.
     * Si el servicio remoto no responde se devuelve 503.
     */
    private void validarCategoria(Long categoriaId) {
        try {
            categoriaClient.obtenerCategoria(categoriaId);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException(
                    "Categoria no encontrada con id: " + categoriaId);
        } catch (FeignException ex) {
            throw new ServiceUnavailableException(
                    "categoria-service no disponible. No se pudo validar la categoria");
        }
    }

    /**
     * Completa el campo transient categoria. Si el servicio remoto falla,
     * el producto se devuelve sin categoria para no romper la consulta.
     */
    private void completarCategoria(Producto producto) {
        try {
            CategoriaDTO categoria = categoriaClient.obtenerCategoria(producto.getCategoriaId());
            producto.setCategoria(categoria);
        } catch (Exception ex) {
            producto.setCategoria(null);
        }
    }
}
