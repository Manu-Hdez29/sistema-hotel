package sv.edu.udb.sistemahotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.sistemahotel.model.Huesped;

import java.util.Optional;

public interface HuespedRepository extends JpaRepository<Huesped, Integer> {

    Optional<Huesped> findByDocumentoIdentidad(String documentoIdentidad);

    Optional<Huesped> findByUsuarioId(Integer usuarioId);

    boolean existsByDocumentoIdentidad(String documentoIdentidad);

    boolean existsByDocumentoIdentidadAndIdNot(String documentoIdentidad, Integer id);
}