package sv.edu.udb.sistemahotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.sistemahotel.model.ReservaHuesped;

import java.util.List;

public interface ReservaHuespedRepository extends JpaRepository<ReservaHuesped, Integer> {

    boolean existsByHuespedId(Integer huespedId);

    List<ReservaHuesped> findByReservaId(Integer reservaId);
}