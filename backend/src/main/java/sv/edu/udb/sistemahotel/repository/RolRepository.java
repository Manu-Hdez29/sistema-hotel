package sv.edu.udb.sistemahotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.sistemahotel.model.Rol;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombreRol(String nombreRol);
}