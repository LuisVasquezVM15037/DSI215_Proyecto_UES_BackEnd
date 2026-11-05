package com.dentalcare.api.repositories;

import com.dentalcare.api.models.ConfiguracionAlertaCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionAlertaCitaRepository extends JpaRepository<ConfiguracionAlertaCita, Integer> {
    Optional<ConfiguracionAlertaCita> findByUsuario_IdUsuario(Integer idUsuario);
}
