package sv.edu.udb.sistemahotel.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.dto.ReclamoAtenderRequest;
import sv.edu.udb.sistemahotel.dto.ReclamoCrearRequest;
import sv.edu.udb.sistemahotel.dto.ReclamoResponse;
import sv.edu.udb.sistemahotel.exception.ApiException;
import sv.edu.udb.sistemahotel.model.Huesped;
import sv.edu.udb.sistemahotel.model.Reclamo;
import sv.edu.udb.sistemahotel.model.Reserva;
import sv.edu.udb.sistemahotel.model.Usuario;
import sv.edu.udb.sistemahotel.model.enums.EstadoReclamo;
import sv.edu.udb.sistemahotel.repository.HuespedRepository;
import sv.edu.udb.sistemahotel.repository.ReclamoRepository;
import sv.edu.udb.sistemahotel.repository.ReservaRepository;
import sv.edu.udb.sistemahotel.repository.UsuarioRepository;

import java.util.List;

@Service
public class ReclamoService {

    private static final String ROL_HUESPED = "ROLE_HUESPED";

    private final ReclamoRepository reclamoRepository;
    private final HuespedRepository huespedRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;

    public ReclamoService(ReclamoRepository reclamoRepository,
                          HuespedRepository huespedRepository,
                          ReservaRepository reservaRepository,
                          UsuarioRepository usuarioRepository) {
        this.reclamoRepository = reclamoRepository;
        this.huespedRepository = huespedRepository;
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ReclamoResponse crear(ReclamoCrearRequest request, UserDetails actual) {
        Usuario usuario = usuarioActual(actual);
        boolean staff = esStaff(actual);

        Huesped huesped = huespedRepository.findById(request.huespedId())
                .orElseThrow(() -> ApiException.noEncontrado(
                        "No se encontró el huésped con id " + request.huespedId() + "."));

        // Un huésped solo puede levantar reclamos con su propio perfil
        if (!staff && !esDelUsuario(huesped, usuario)) {
            throw ApiException.noEncontrado("No se encontró el huésped con id " + request.huespedId() + ".");
        }

        Reserva reserva = null;
        if (request.reservaId() != null) {
            reserva = reservaRepository.findById(request.reservaId())
                    .orElseThrow(() -> ApiException.noEncontrado(
                            "No se encontró la reserva con id " + request.reservaId() + "."));

            if (!staff && (reserva.getUsuario() == null || !reserva.getUsuario().getId().equals(usuario.getId()))) {
                throw ApiException.noEncontrado("No se encontró la reserva con id " + request.reservaId() + ".");
            }
        }

        Reclamo reclamo = new Reclamo();
        reclamo.setHuesped(huesped);
        reclamo.setReserva(reserva);
        reclamo.setDescripcion(request.descripcion().trim());
        reclamo.setEstado(EstadoReclamo.ABIERTO);
        reclamoRepository.save(reclamo);

        return toResponse(reclamo);
    }

    @Transactional(readOnly = true)
    public List<ReclamoResponse> listar(EstadoReclamo estado, UserDetails actual) {
        Usuario usuario = usuarioActual(actual);
        boolean staff = esStaff(actual);

        return reclamoRepository.findAll().stream()
                .filter(c -> staff || esDelUsuario(c.getHuesped(), usuario))
                .filter(c -> estado == null || c.getEstado() == estado)
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ReclamoResponse atender(Integer id, ReclamoAtenderRequest request, UserDetails actual) {
        Reclamo reclamo = reclamoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el reclamo con id " + id + "."));

        if (request.estado() == EstadoReclamo.ABIERTO) {
            throw ApiException.solicitudInvalida("El reclamo ya paso a su debido PROCESO o ya fue " +
                    "CERRADO.");
        }
        if (reclamo.getEstado() == EstadoReclamo.CERRADO) {
            throw ApiException.conflicto("El reclamo ya está cerrado.");
        }

        reclamo.setEstado(request.estado());
        reclamo.setRespuesta(request.respuesta().trim());
        reclamo.setUsuario(usuarioActual(actual));

        return toResponse(reclamo);
    }

    private Usuario usuarioActual(UserDetails actual) {
        return usuarioRepository.findByEmail(actual.getUsername())
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el usuario autenticado."));
    }

    private static boolean esStaff(UserDetails actual) {
        return actual.getAuthorities().stream()
                .anyMatch(a -> !ROL_HUESPED.equals(a.getAuthority()));
    }

    private static boolean esDelUsuario(Huesped huesped, Usuario usuario) {
        return huesped.getUsuario() != null && huesped.getUsuario().getId().equals(usuario.getId());
    }

    private ReclamoResponse toResponse(Reclamo reclamo) {
        return new ReclamoResponse(
                reclamo.getId(),
                reclamo.getHuesped().getId(),
                reclamo.getHuesped().getNombreCompleto(),
                reclamo.getReserva() != null ? reclamo.getReserva().getId() : null,
                reclamo.getDescripcion(),
                reclamo.getFecha(),
                reclamo.getEstado(),
                reclamo.getRespuesta(),
                reclamo.getUsuario() != null ? reclamo.getUsuario().getNombreCompleto() : null);
    }
}