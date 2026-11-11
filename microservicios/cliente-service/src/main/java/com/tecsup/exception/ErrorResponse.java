package com.tecsup.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> errores
) {

    public static ErrorResponse of(HttpStatus status, String mensaje, String ruta) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                ruta,
                null
        );
    }

    public static ErrorResponse of(HttpStatus status, String mensaje, String ruta,
                                   Map<String, String> errores) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                ruta,
                errores
        );
    }
}
