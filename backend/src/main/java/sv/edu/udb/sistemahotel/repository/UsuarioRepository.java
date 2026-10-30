package sv.edu.udb.sistemahotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.sistemahotel.model.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Integer id);
}