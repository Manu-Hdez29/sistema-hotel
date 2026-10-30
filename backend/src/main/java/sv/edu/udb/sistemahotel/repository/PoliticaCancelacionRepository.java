package sv.edu.udb.sistemahotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.sistemahotel.model.PoliticaCancelacion;

import java.util.List;

public interface PoliticaCancelacionRepository extends JpaRepository<PoliticaCancelacion, Integer> {

    List<PoliticaCancelacion> findByHotelId(Integer hotelId);
}