package sv.edu.udb.sistemahotel.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.dto.LoginRequest;
import sv.edu.udb.sistemahotel.dto.LoginResponse;
import sv.edu.udb.sistemahotel.dto.MensajeResponse;
import sv.edu.udb.sistemahotel.dto.RegistroRequest;
import sv.edu.udb.sistemahotel.dto.UsuarioResumen;
import sv.edu.udb.sistemahotel.exception.ApiException;
import sv.edu.udb.sistemahotel.model.Huesped;
import sv.edu.udb.sistemahotel.model.Rol;
import sv.edu.udb.sistemahotel.model.Usuario;
import sv.edu.udb.sistemahotel.model.enums.EstadoGeneral;
import sv.edu.udb.sistemahotel.repository.HuespedRepository;
import sv.edu.udb.sistemahotel.repository.RolRepository;
import sv.edu.udb.sistemahotel.repository.UsuarioRepository;
import sv.edu.udb.sistemahotel.security.JwtService;
import org.springframework.security.core.Authentication;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final HuespedRepository huespedRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UsuarioRepository usuarioRepository,
                       RolRepository rolRepository,
                       HuespedRepository huespedRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.huespedRepository = huespedRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = normalizarEmail(request.email());

        Authentication autenticacion;
        try {
            autenticacion = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (DisabledException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED,
                    "Su cuenta está inactiva. Contacte al administrador.");
        } catch (BadCredentialsException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos.");
        }

        UserDetails detalle = (UserDetails) autenticacion.getPrincipal();
        String token = jwtService.generarToken(detalle);
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.extraerExpiracion(token),
                toResumen(usuario));
    }

    @Transactional
    public MensajeResponse registrar(RegistroRequest request) {
        String email = normalizarEmail(request.email());
        String documento = request.documentoIdentidad().trim();

        if (usuarioRepository.existsByEmail(email)) {
            throw ApiException.conflicto("Ya existe una cuenta con ese correo.");
        }
        if (huespedRepository.existsByDocumentoIdentidad(documento)) {
            throw ApiException.conflicto("Ya existe un huésped registrado con ese documento.");
        }

        Rol rolHuesped = rolRepository.findByNombreRol("HUESPED")
                .orElseThrow(() -> new IllegalStateException("Falta el rol HUESPED en la base de datos."));

        Usuario usuario = new Usuario();
        usuario.setRol(rolHuesped);
        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setEstado(EstadoGeneral.ACTIVO);
        usuarioRepository.save(usuario);

        // Cada cuenta de huésped también tiene su fila en HUESPED, ligada al usuario
        Huesped huesped = new Huesped();
        huesped.setUsuario(usuario);
        huesped.setNombreCompleto(usuario.getNombreCompleto());
        huesped.setFechaNacimiento(request.fechaNacimiento());
        huesped.setDocumentoIdentidad(documento);
        huesped.setTelefono(request.telefono());
        huesped.setEmail(email);
        huespedRepository.save(huesped);

        return new MensajeResponse("Su cuenta fue creada con éxito. Ya puede iniciar sesión.");
    }

    @Transactional(readOnly = true)
    public UsuarioResumen obtenerUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el usuario."));
        return toResumen(usuario);
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    private static UsuarioResumen toResumen(Usuario usuario) {
        return new UsuarioResumen(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRol().getNombreRol());
    }
}