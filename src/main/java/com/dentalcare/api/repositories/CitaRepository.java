package com.dentalcare.api.repositories;

import com.dentalcare.api.models.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {

    List<Cita> findAllByOrderByFechaCitaAscHoraInicioCitaAsc();

    @Query("SELECT c FROM Cita c WHERE c.odontologo.idOdontologo = :idOdontologo " +
           "AND c.fechaCita = :fecha " +
           "AND c.estadoCita != com.dentalcare.api.models.enums.EstadoCita.CANCELADA " +
           "ORDER BY c.horaInicioCita ASC")
    List<Cita> findCitasActivasPorOdontologoYFecha(
            @Param("idOdontologo") Integer idOdontologo,
            @Param("fecha") LocalDate fecha);

    @Query("SELECT c FROM Cita c WHERE c.odontologo.idOdontologo = :idOdontologo " +
           "AND c.fechaCita = :fecha " +
           "AND c.estadoCita != com.dentalcare.api.models.enums.EstadoCita.CANCELADA " +
           "AND c.horaInicioCita < :horaFin AND c.horaFinCita > :horaInicio")
    List<Cita> findConflictosOdontologo(
            @Param("idOdontologo") Integer idOdontologo,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalDateTime horaInicio,
            @Param("horaFin") LocalDateTime horaFin);

    @Query("SELECT c FROM Cita c WHERE c.odontologo.idOdontologo = :idOdontologo " +
           "AND c.fechaCita = :fecha " +
           "AND c.idCitas != :idCita " +
           "AND c.estadoCita != com.dentalcare.api.models.enums.EstadoCita.CANCELADA " +
           "AND c.horaInicioCita < :horaFin AND c.horaFinCita > :horaInicio")
    List<Cita> findConflictosOdontologoExcluyendo(
            @Param("idOdontologo") Integer idOdontologo,
            @Param("fecha") LocalDate fecha,
            @Param("idCita") Integer idCita,
            @Param("horaInicio") LocalDateTime horaInicio,
            @Param("horaFin") LocalDateTime horaFin);

    @Query("SELECT c FROM Cita c WHERE c.paciente.idPaciente = :idPaciente " +
           "AND c.fechaCita = :fecha " +
           "AND c.estadoCita != com.dentalcare.api.models.enums.EstadoCita.CANCELADA " +
           "AND c.horaInicioCita < :horaFin AND c.horaFinCita > :horaInicio")
    List<Cita> findConflictosPaciente(
            @Param("idPaciente") Integer idPaciente,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalDateTime horaInicio,
            @Param("horaFin") LocalDateTime horaFin);

    @Query("SELECT c FROM Cita c WHERE c.paciente.idPaciente = :idPaciente " +
           "AND c.fechaCita = :fecha " +
           "AND c.idCitas != :idCita " +
           "AND c.estadoCita != com.dentalcare.api.models.enums.EstadoCita.CANCELADA " +
           "AND c.horaInicioCita < :horaFin AND c.horaFinCita > :horaInicio")
    List<Cita> findConflictosPacienteExcluyendo(
            @Param("idPaciente") Integer idPaciente,
            @Param("fecha") LocalDate fecha,
            @Param("idCita") Integer idCita,
            @Param("horaInicio") LocalDateTime horaInicio,
            @Param("horaFin") LocalDateTime horaFin);
}
