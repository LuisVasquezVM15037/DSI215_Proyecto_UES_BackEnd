package com.dentalcare.api.repositories;

import com.dentalcare.api.models.ListaEspera;
import com.dentalcare.api.models.enums.EstadoListaEspera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListaEsperaRepository extends JpaRepository<ListaEspera, Integer> {

    List<ListaEspera> findByEstadoOrderByFechaSolicitudAsc(EstadoListaEspera estado);

    List<ListaEspera> findAllByOrderByPrioridadAscFechaSolicitudAsc();

    @Query("SELECT le FROM ListaEspera le WHERE le.estado = :estado AND " +
           "(le.odontologo IS NULL OR le.odontologo.idOdontologo = :idOdontologo) " +
           "ORDER BY CASE le.prioridad WHEN 'ALTA' THEN 1 WHEN 'MEDIA' THEN 2 ELSE 3 END, le.fechaSolicitud ASC")
    List<ListaEspera> findCandidatosParaCupo(@Param("idOdontologo") Integer idOdontologo, 
                                            @Param("estado") EstadoListaEspera estado);
}
