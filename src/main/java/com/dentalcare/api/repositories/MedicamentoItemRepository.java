package com.dentalcare.api.repositories;

import com.dentalcare.api.models.MedicamentoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicamentoItemRepository extends JpaRepository<MedicamentoItem, Integer> {

    Optional<MedicamentoItem> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Integer id);

    List<MedicamentoItem> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(String nombre, String codigo);

    List<MedicamentoItem> findAllByOrderByNombreAsc();
}
