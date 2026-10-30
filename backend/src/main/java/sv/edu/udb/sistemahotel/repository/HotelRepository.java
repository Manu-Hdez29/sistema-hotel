package sv.edu.udb.sistemahotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.sistemahotel.model.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Integer> {

    boolean existsByNitRuc(String nitRuc);

    boolean existsByNitRucAndIdNot(String nitRuc, Integer id);
}