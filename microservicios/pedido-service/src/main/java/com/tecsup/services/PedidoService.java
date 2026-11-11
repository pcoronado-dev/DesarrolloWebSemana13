package com.tecsup.services;

import java.time.LocalDateTime;
import java.util.List;

import com.tecsup.client.ClienteClient;
import com.tecsup.client.ProductoClient;
import com.tecsup.dto.ClienteDTO;
import com.tecsup.dto.PedidoRequestDTO;
import com.tecsup.dto.PedidoResponseDTO;
import com.tecsup.dto.ProductoDTO;
import com.tecsup.exception.BusinessRuleException;
import com.tecsup.exception.ResourceNotFoundException;
import com.tecsup.exception.ServiceUnavailableException;
import com.tecsup.model.Pedido;
import com.tecsup.repository.PedidoRepository;

import feign.FeignException;

import org.springframework.stereotype.Service;

@Service
public class PedidoService {

    private static final String PENDIENTE = "PENDIENTE";
    private static final String ATENDIDO = "ATENDIDO";
    private static final String ANULADO = "ANULADO";

    private final PedidoRepository repo;
    private final ClienteClient clienteClient;
    private final ProductoClient productoClient;

    public PedidoService(PedidoRepository repo,
                         ClienteClient clienteClient,
                         ProductoClient productoClient) {
        this.repo = repo;
        this.clienteClient = clienteClient;
        this.productoClient = productoClient;
    }

    public List<PedidoResponseDTO> listar(String estado, Long clienteId) {
        List<Pedido> pedidos;

        if (estado != null && clienteId != null) {
            pedidos = repo.findByEstadoAndClienteId(estado, clienteId);
        } else if (estado != null) {
            pedidos = repo.findByEstado(estado);
        } else if (clienteId != null) {
            pedidos = repo.findByClienteId(clienteId);
        } else {
            pedidos = repo.findAll();
        }

        return pedidos.stream().map(this::construirRespuesta).toList();
    }

    public PedidoResponseDTO obtener(Long id) {
        return construirRespuesta(buscar(id));
    }

    public PedidoResponseDTO guardar(PedidoRequestDTO dto) {
        // Se valida que las referencias existan antes de guardar.
        ClienteDTO cliente = obtenerCliente(dto.clienteId());
        ProductoDTO producto = obtenerProducto(dto.productoId());

        Pedido pedido = new Pedido();
        pedido.setClienteId(dto.clienteId());
        pedido.setProductoId(dto.productoId());
        pedido.setCantidad(dto.cantidad());
        pedido.setEstado(PENDIENTE);
        pedido.setFecha(LocalDateTime.now());

        Pedido guardado = repo.save(pedido);
        return construirRespuesta(guardado, cliente, producto);
    }

    public PedidoResponseDTO cambiarEstado(Long id, String nuevoEstado) {
        Pedido pedido = buscar(id);
        String estadoActual = pedido.getEstado();

        if (nuevoEstado.equals(estadoActual)) {
            return construirRespuesta(pedido);
        }

        boolean transicionPermitida = switch (estadoActual) {
            case PENDIENTE -> ATENDIDO.equals(nuevoEstado) || ANULADO.equals(nuevoEstado);
            case ATENDIDO -> ANULADO.equals(nuevoEstado);
            default -> false;
        };

        if (!transicionPermitida) {
            throw new BusinessRuleException(
                    "Transicion no permitida: " + estadoActual + " -> " + nuevoEstado);
        }

        pedido.setEstado(nuevoEstado);
        return construirRespuesta(repo.save(pedido));
    }

    /**
     * El pedido no se elimina fisicamente: se anula para conservar el historial.
     */
    public PedidoResponseDTO anular(Long id) {
        return cambiarEstado(id, ANULADO);
    }

    private Pedido buscar(Long id) {
        return repo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));
    }

    private PedidoResponseDTO construirRespuesta(Pedido pedido) {
        ClienteDTO cliente = obtenerCliente(pedido.getClienteId());
        ProductoDTO producto = obtenerProducto(pedido.getProductoId());
        return construirRespuesta(pedido, cliente, producto);
    }

    private PedidoResponseDTO construirRespuesta(Pedido pedido,
                                                 ClienteDTO cliente,
                                                 ProductoDTO producto) {
        Double subtotal = null;
        if (producto != null && producto.precio() != null && pedido.getCantidad() != null) {
            subtotal = producto.precio() * pedido.getCantidad();
        }

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.getProductoId(),
                pedido.getCantidad(),
                pedido.getEstado(),
                pedido.getFecha(),
                subtotal,
                cliente,
                producto
        );
    }

    private ClienteDTO obtenerCliente(Long id) {
        try {
            return clienteClient.obtenerCliente(id);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Cliente no encontrado con id: " + id);
        } catch (FeignException ex) {
            throw new ServiceUnavailableException(
                    "cliente-service no disponible. No se pudo obtener el cliente");
        }
    }

    private ProductoDTO obtenerProducto(Long id) {
        try {
            return productoClient.obtenerProducto(id);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Producto no encontrado con id: " + id);
        } catch (FeignException ex) {
            throw new ServiceUnavailableException(
                    "producto-service no disponible. No se pudo obtener el producto");
        }
    }
}
