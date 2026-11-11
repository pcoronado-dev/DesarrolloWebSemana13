package com.tecsup.repository;

import java.util.List;

import com.tecsup.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByEstado(String estado);

    List<Pedido> findByClienteId(Long clienteId);

    List<Pedido> findByEstadoAndClienteId(String estado, Long clienteId);
}
