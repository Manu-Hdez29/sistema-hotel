package sv.edu.udb.sistemahotel.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.dto.UsuarioActualizarRequest;
import sv.edu.udb.sistemahotel.dto.UsuarioCrearRequest;
import sv.edu.udb.sistemahotel.dto.UsuarioResponse;
import sv.edu.udb.sistemahotel.exception.ApiException;
import sv.edu.udb.sistemahotel.model.Rol;
import sv.edu.udb.sistemahotel.model.Usuario;
import sv.edu.udb.sistemahotel.model.enums.EstadoGeneral;
import sv.edu.udb.sistemahotel.repository.HuespedRepository;
import sv.edu.udb.sistemahotel.repository.ReclamoRepository;
import sv.edu.udb.sistemahotel.repository.ReservaRepository;
import sv.edu.udb.sistemahotel.repository.RolRepository;
import sv.edu.udb.sistemahotel.repository.UsuarioRepository;

import java.util.List;

@Service
public class UsuarioService {

    private static final String ROL_HUESPED = "HUESPED";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final HuespedRepository huespedRepository;
    private final ReservaRepository reservaRepository;
    private final ReclamoRepository reclamoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          HuespedRepository huespedRepository,
                          ReservaRepository reservaRepository,
                          ReclamoRepository reclamoRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.huespedRepository = huespedRepository;
        this.reservaRepository = reservaRepository;
        this.reclamoRepository = reclamoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioService::toResponse)
                .toList();
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCrearRequest request) {
        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw ApiException.conflicto("Ya existe una cuenta con ese correo.");
        }

        Usuario usuario = new Usuario();
        usuario.setRol(resolverRolPersonal(request.rol()));
        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setEstado(EstadoGeneral.ACTIVO);
        usuarioRepository.save(usuario);

        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse actualizar(Integer id, UsuarioActualizarRequest request) {
        Usuario usuario = buscar(id);

        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmailAndIdNot(email, id)) {
            throw ApiException.conflicto("Ya existe otra cuenta con ese correo.");
        }

        String rolPedido = request.rol().trim().toUpperCase();
        String rolActual = usuario.getRol().getNombreRol();

        if (ROL_HUESPED.equals(rolActual)) {
            if (!ROL_HUESPED.equals(rolPedido)) {
                throw ApiException.solicitudInvalida(
                        "Los usuarios huéspedes no pueden cambiar de rol desde esta opción.");
            }
        } else {
            usuario.setRol(resolverRolPersonal(rolPedido));
        }

        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setEmail(email);

        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        // Si el usuario tiene perfil de huésped, se mantiene su nombre y correo sincronizados
        huespedRepository.findByUsuarioId(id).ifPresent(huesped -> {
            huesped.setNombreCompleto(usuario.getNombreCompleto());
            huesped.setEmail(email);
        });

        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse activar(Integer id) {
        Usuario usuario = buscar(id);
        usuario.setEstado(EstadoGeneral.ACTIVO);
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse desactivar(Integer id, String emailActual) {
        Usuario usuario = buscar(id);

        if (usuario.getEmail().equals(emailActual)) {
            throw ApiException.solicitudInvalida("No puede desactivar su propia cuenta.");
        }

        usuario.setEstado(EstadoGeneral.INACTIVO);
        return toResponse(usuario);
    }

    @Transactional
    public void eliminar(Integer id, String emailActual) {
        Usuario usuario = buscar(id);

        if (usuario.getEmail().equals(emailActual)) {
            throw ApiException.solicitudInvalida("No puede eliminar su propia cuenta.");
        }

        if (reservaRepository.existsByUsuarioId(id)
                || reclamoRepository.existsByUsuarioId(id)
                || huespedRepository.findByUsuarioId(id).isPresent()) {
            throw ApiException.conflicto(
                    "No se puede eliminar el usuario porque tiene reservas, reclamos o perfil de huésped. Desactívelo en su lugar.");
        }

        usuarioRepository.delete(usuario);
    }

    private Usuario buscar(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el usuario con id " + id + "."));
    }

    private Rol resolverRolPersonal(String nombreRol) {
        String nombre = nombreRol.trim().toUpperCase();

        if (!nombre.equals("ADMIN") && !nombre.equals("RECEPCIONISTA")) {
            throw ApiException.solicitudInvalida(
                    "El rol debe ser ADMIN o RECEPCIONISTA. Los huéspedes se registran desde el registro público.");
        }

        return rolRepository.findByNombreRol(nombre)
                .orElseThrow(() -> ApiException.noEncontrado("No existe el rol " + nombre + " en la base de datos."));
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    private static UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRol().getNombreRol(),
                usuario.getEstado());
    }
}