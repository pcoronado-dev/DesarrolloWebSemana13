package com.tecsup.dto;

public record ProductoDTO(
        Long id,
        String nombre,
        Double precio,
        Integer stock,
        Long categoriaId
) {
}
