package com.dentalcare.api.repositories;

import com.dentalcare.api.models.MedicamentoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    /**
     * Consulta los medicamentos que se encuentran en o por debajo de su stock mínimo de seguridad.
     * DENTALCARE-49 / DENTALCARE-73
     */
    @Query("SELECT m FROM MedicamentoItem m WHERE m.stockActual <= m.stockMinimo ORDER BY (m.stockMinimo - m.stockActual) DESC")
    List<MedicamentoItem> findMedicamentosBajoStockMinimo();
}
