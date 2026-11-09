package com.dentalcare.api.repositories;

import com.dentalcare.api.models.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Integer> {

    Optional<Material> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Integer id);

    List<Material> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(String nombre, String codigo);

    List<Material> findAllByOrderByNombreAsc();

    /**
     * Consulta los materiales que se encuentran en o por debajo de su stock mínimo de seguridad.
     * DENTALCARE-49 / DENTALCARE-73
     */
    @Query("SELECT m FROM Material m WHERE m.stockActual <= m.stockMinimo ORDER BY (m.stockMinimo - m.stockActual) DESC")
    List<Material> findMaterialesBajoStockMinimo();
}
