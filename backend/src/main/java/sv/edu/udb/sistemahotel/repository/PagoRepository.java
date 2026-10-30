package sv.edu.udb.sistemahotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.sistemahotel.model.Pago;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByReservaId(Integer reservaId);
}