package com.tecsup.controller;

import java.util.List;

import com.tecsup.dto.EstadoDTO;
import com.tecsup.dto.PedidoRequestDTO;
import com.tecsup.dto.PedidoResponseDTO;
import com.tecsup.services.PedidoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long clienteId) {

        return ResponseEntity.ok(service.listar(estado, clienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> guardar(
            @Valid @RequestBody PedidoRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(dto));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoDTO dto) {

        return ResponseEntity.ok(service.cambiarEstado(id, dto.estado()));
    }

    /**
     * No elimina fisicamente: anula el pedido y devuelve el registro actualizado.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> anular(@PathVariable Long id) {
        return ResponseEntity.ok(service.anular(id));
    }
}
