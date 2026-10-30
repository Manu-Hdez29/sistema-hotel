package sv.edu.udb.sistemahotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.udb.sistemahotel.model.Reserva;
import sv.edu.udb.sistemahotel.model.enums.EstadoReserva;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    boolean existsByUsuarioId(Integer usuarioId);

    // Habitaciones con reservas activas que se cruzan con el rango
    @Query("SELECT r.habitacion.id FROM Reserva r " +
            "WHERE r.estadoReserva IN :estados " +
            "AND r.fechaEntrada < :fechaSalida AND r.fechaSalida > :fechaEntrada")
    List<Integer> findHabitacionIdsOcupadasEnRango(@Param("estados") Collection<EstadoReserva> estados,
                                                   @Param("fechaEntrada") LocalDate fechaEntrada,
                                                   @Param("fechaSalida") LocalDate fechaSalida);

    // Otras reservas activas de la misma habitación que se cruzan con el rango (excluye la reserva indicada)
    @Query("SELECT r.id FROM Reserva r " +
            "WHERE r.habitacion.id = :habitacionId " +
            "AND r.estadoReserva IN :estados " +
            "AND r.fechaEntrada < :fechaSalida AND r.fechaSalida > :fechaEntrada " +
            "AND r.id <> :reservaId")
    List<Integer> findIdsReservasOtrasEnRango(@Param("habitacionId") Integer habitacionId,
                                              @Param("estados") Collection<EstadoReserva> estados,
                                              @Param("fechaEntrada") LocalDate fechaEntrada,
                                              @Param("fechaSalida") LocalDate fechaSalida,
                                              @Param("reservaId") Integer reservaId);
}