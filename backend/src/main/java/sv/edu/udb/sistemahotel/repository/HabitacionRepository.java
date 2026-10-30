package sv.edu.udb.sistemahotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.sistemahotel.model.Habitacion;

import java.util.List;

public interface HabitacionRepository extends JpaRepository<Habitacion, Integer> {

    List<Habitacion> findBySucursalId(Integer sucursalId);

    boolean existsBySucursalId(Integer sucursalId);

    boolean existsByTipoHabitacionId(Integer tipoHabitacionId);

    boolean existsBySucursalIdAndNumeroHabitacion(Integer sucursalId, String numeroHabitacion);

    boolean existsBySucursalIdAndNumeroHabitacionAndIdNot(Integer sucursalId, String numeroHabitacion, Integer id);
}