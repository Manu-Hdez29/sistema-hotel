package sv.edu.udb.sistemahotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.sistemahotel.model.ReservaServicio;

import java.util.List;

public interface ReservaServicioRepository extends JpaRepository<ReservaServicio, Integer> {

    List<ReservaServicio> findByReservaId(Integer reservaId);
}