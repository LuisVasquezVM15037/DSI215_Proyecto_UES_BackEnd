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

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Consumo c " +
           "LEFT JOIN FETCH c.cita ci " +
           "LEFT JOIN FETCH ci.paciente " +
           "LEFT JOIN FETCH ci.odontologo o " +
           "LEFT JOIN FETCH o.usuario " +
           "LEFT JOIN FETCH c.usuarioRegistro " +
           "WHERE c.fechaConsumo BETWEEN :desde AND :hasta " +
           "ORDER BY c.fechaConsumo DESC")
    List<Consumo> findConsumosEntreFechas(
            @org.springframework.data.repository.query.Param("desde") java.time.LocalDateTime desde,
            @org.springframework.data.repository.query.Param("hasta") java.time.LocalDateTime hasta);

}
