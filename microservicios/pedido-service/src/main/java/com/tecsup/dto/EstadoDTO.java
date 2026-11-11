package com.tecsup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EstadoDTO(

        @NotBlank(message = "El estado es obligatorio")
        @Pattern(regexp = "PENDIENTE|ATENDIDO|ANULADO",
                message = "El estado debe ser PENDIENTE, ATENDIDO o ANULADO")
        String estado
) {
}
