package sv.edu.udb.sistemahotel.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.model.Usuario;
import sv.edu.udb.sistemahotel.model.enums.EstadoGeneral;
import sv.edu.udb.sistemahotel.repository.UsuarioRepository;

import java.util.List;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Transaccional para que el rol (LAZY) se pueda leer dentro de la sesión
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Un usuario inactivo no puede iniciar sesión. Spring lo rechaza
        boolean activo = usuario.getEstado() == EstadoGeneral.ACTIVO;
        List<GrantedAuthority> permisos = List.of(
                new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombreRol())
        );

        return new User(usuario.getEmail(), usuario.getPasswordHash(), activo, true, true, true, permisos);
    }
}