package sv.edu.udb.sistemahotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.udb.sistemahotel.model.Disponibilidad;
import sv.edu.udb.sistemahotel.model.enums.EstadoDisponibilidad;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Integer> {

    // Habitaciones con algún día bloqueado u ocupado dentro de las noches del rango
    @Query("SELECT d.habitacion.id FROM Disponibilidad d " +
            "WHERE d.estadoDisponibilidad IN :estados " +
            "AND d.fecha >= :fechaEntrada AND d.fecha < :fechaSalida")
    List<Integer> findHabitacionIdsNoDisponiblesEnRango(@Param("estados") Collection<EstadoDisponibilidad> estados,
                                                        @Param("fechaEntrada") LocalDate fechaEntrada,
                                                        @Param("fechaSalida") LocalDate fechaSalida);
}