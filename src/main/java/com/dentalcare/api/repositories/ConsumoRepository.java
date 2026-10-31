package com.dentalcare.api.repositories;

import com.dentalcare.api.models.Consumo;
import com.dentalcare.api.models.enums.TipoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConsumoRepository extends JpaRepository<Consumo, Integer> {

    List<Consumo> findByMesConsumoOrderByFechaConsumoDesc(String mesConsumo);

    List<Consumo> findAllByOrderByFechaConsumoDesc();

    boolean existsByCita_IdCitasAndTipoItemAndItemId(Integer idCitas, TipoItem tipoItem, Integer itemId);
}
