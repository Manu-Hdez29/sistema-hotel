package sv.edu.udb.sistemahotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.sistemahotel.model.Reclamo;

public interface ReclamoRepository extends JpaRepository<Reclamo, Integer> {

    boolean existsByUsuarioId(Integer usuarioId);

    boolean existsByHuespedId(Integer huespedId);
}