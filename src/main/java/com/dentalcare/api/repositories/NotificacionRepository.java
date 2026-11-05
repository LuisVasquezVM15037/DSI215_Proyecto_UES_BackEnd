package com.dentalcare.api.repositories;

import com.dentalcare.api.models.Notificacion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    @Query("SELECT n FROM Notificacion n WHERE " +
           "(:usuarioId IS NOT NULL AND n.usuario.idUsuario = :usuarioId) " +
           "OR (n.usuario IS NULL AND (:rol IS NULL OR n.rolDestinatario = :rol OR n.rolDestinatario = 'TODOS' OR n.rolDestinatario IS NULL)) " +
           "ORDER BY n.fechaCreacion DESC")
    List<Notificacion> buscarPorUsuarioORol(@Param("usuarioId") Integer usuarioId, 
                                           @Param("rol") String rol, 
                                           Pageable pageable);

    @Query("SELECT n FROM Notificacion n WHERE n.leida = false AND (" +
           "(:usuarioId IS NOT NULL AND n.usuario.idUsuario = :usuarioId) " +
           "OR (n.usuario IS NULL AND (:rol IS NULL OR n.rolDestinatario = :rol OR n.rolDestinatario = 'TODOS' OR n.rolDestinatario IS NULL))) " +
           "ORDER BY n.fechaCreacion DESC")
    List<Notificacion> buscarNoLeidasPorUsuarioORol(@Param("usuarioId") Integer usuarioId, 
                                                   @Param("rol") String rol, 
                                                   Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.leida = false AND (" +
           "(:usuarioId IS NOT NULL AND n.usuario.idUsuario = :usuarioId) " +
           "OR (n.usuario IS NULL AND (:rol IS NULL OR n.rolDestinatario = :rol OR n.rolDestinatario = 'TODOS' OR n.rolDestinatario IS NULL)))")
    long contarNoLeidas(@Param("usuarioId") Integer usuarioId, @Param("rol") String rol);

    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.leida = false AND (" +
           "(:usuarioId IS NOT NULL AND n.usuario.idUsuario = :usuarioId) " +
           "OR (n.usuario IS NULL AND (:rol IS NULL OR n.rolDestinatario = :rol OR n.rolDestinatario = 'TODOS' OR n.rolDestinatario IS NULL)))")
    int marcarTodasComoLeidas(@Param("usuarioId") Integer usuarioId, @Param("rol") String rol);
}
