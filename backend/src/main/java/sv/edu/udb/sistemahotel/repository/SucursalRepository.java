package sv.edu.udb.sistemahotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.sistemahotel.model.Sucursal;

import java.util.List;

public interface SucursalRepository extends JpaRepository<Sucursal, Integer> {

    boolean existsByHotelId(Integer hotelId);

    List<Sucursal> findByHotelId(Integer hotelId);
}