package sv.edu.udb.sistemahotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.sistemahotel.model.TipoHabitacion;

public interface TipoHabitacionRepository extends JpaRepository<TipoHabitacion, Integer> {
}