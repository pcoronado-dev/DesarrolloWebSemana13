package com.tecsup.dto;

public record ClienteDTO(
        Long id,
        String nombre,
        String apellidos,
        String direccion,
        String telefono,
        String nroDni,
        String estado
) {
}
