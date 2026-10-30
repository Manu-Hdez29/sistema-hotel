package sv.edu.udb.sistemahotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.sistemahotel.model.Tarifa;

import java.util.List;

public interface TarifaRepository extends JpaRepository<Tarifa, Integer> {

    List<Tarifa> findBySucursalId(Integer sucursalId);

    List<Tarifa> findByTipoHabitacionId(Integer tipoHabitacionId);

    List<Tarifa> findBySucursalIdAndTipoHabitacionId(Integer sucursalId, Integer tipoHabitacionId);

    boolean existsBySucursalId(Integer sucursalId);

    boolean existsByTipoHabitacionId(Integer tipoHabitacionId);
}