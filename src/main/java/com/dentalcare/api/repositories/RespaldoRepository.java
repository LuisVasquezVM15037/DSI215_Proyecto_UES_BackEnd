package com.dentalcare.api.repositories;

import com.dentalcare.api.models.Respaldo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespaldoRepository extends JpaRepository<Respaldo, Integer> {

    List<Respaldo> findAllByOrderByCreadoEnDesc();
}
