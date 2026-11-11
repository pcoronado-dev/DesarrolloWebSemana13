package com.tecsup.repository;

import com.tecsup.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByNroDni(String nroDni);

    boolean existsByNroDniAndIdNot(String nroDni, Long id);
}
