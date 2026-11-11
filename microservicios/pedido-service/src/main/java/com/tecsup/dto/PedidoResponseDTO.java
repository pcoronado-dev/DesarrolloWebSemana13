package com.tecsup.dto;

import java.time.LocalDateTime;

/**
 * Respuesta enriquecida de un pedido. Los datos de cliente y producto se obtienen
 * por OpenFeign desde sus respectivos microservicios.
 */
public record PedidoResponseDTO(
        Long id,
        Long clienteId,
        Long productoId,
        Integer cantidad,
        String estado,
        LocalDateTime fecha,
        Double subtotal,
        ClienteDTO cliente,
        ProductoDTO producto
) {
}
