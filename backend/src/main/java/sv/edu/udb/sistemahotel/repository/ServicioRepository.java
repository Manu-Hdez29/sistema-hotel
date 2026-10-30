package sv.edu.udb.sistemahotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.sistemahotel.model.Servicio;

public interface ServicioRepository extends JpaRepository<Servicio, Integer> {
}