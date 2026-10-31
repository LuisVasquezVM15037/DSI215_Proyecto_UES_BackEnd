package com.dentalcare.api.repositories;

import com.dentalcare.api.models.PasswordResetToken;
import com.dentalcare.api.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordResetToken, Integer> {

    Optional<PasswordResetToken> findByTokenAndUtilizadoFalse(String token);

    List<PasswordResetToken> findByUsuarioAndUtilizadoFalse(Usuario usuario);
}
