package sv.edu.udb.sistemahotel.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.dto.PagoRequest;
import sv.edu.udb.sistemahotel.dto.ReservaResponse;
import sv.edu.udb.sistemahotel.exception.ApiException;
import sv.edu.udb.sistemahotel.model.Pago;
import sv.edu.udb.sistemahotel.model.Reserva;
import sv.edu.udb.sistemahotel.model.Usuario;
import sv.edu.udb.sistemahotel.model.enums.EstadoPago;
import sv.edu.udb.sistemahotel.model.enums.EstadoReserva;
import sv.edu.udb.sistemahotel.repository.PagoRepository;
import sv.edu.udb.sistemahotel.repository.ReservaRepository;
import sv.edu.udb.sistemahotel.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PagoService {

    private static final String ROL_HUESPED = "ROLE_HUESPED";

    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaService reservaService;

    public PagoService(ReservaRepository reservaRepository,
                       PagoRepository pagoRepository,
                       UsuarioRepository usuarioRepository,
                       ReservaService reservaService) {
        this.reservaRepository = reservaRepository;
        this.pagoRepository = pagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaService = reservaService;
    }

    @Transactional
    public ReservaResponse registrarPago(Integer reservaId, PagoRequest request, UserDetails actual) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró la reserva con id " + reservaId + "."));

        verificarAcceso(reserva, actual);

        if (reserva.getEstadoReserva() != EstadoReserva.PENDIENTE
                && reserva.getEstadoReserva() != EstadoReserva.CONFIRMADA) {
            throw ApiException.conflicto("Solo se pueden registrar pagos en reservas pendientes o confirmadas.");
        }

        List<Pago> pagosExistentes = pagoRepository.findByReservaId(reservaId);
        BigDecimal totalPagado = pagosExistentes.stream()
                .filter(p -> p.getEstadoPago() == EstadoPago.COMPLETADO)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldo = reserva.getPrecioTotal().subtract(totalPagado);

        if (request.monto().compareTo(saldo) > 0) {
            throw ApiException.solicitudInvalida(
                    "El monto excede el saldo pendiente de $" + saldo + ".");
        }

        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setMetodoPago(request.metodo());
        pago.setMonto(request.monto());
        pago.setEstadoPago(EstadoPago.COMPLETADO);
        pagoRepository.save(pago);

        // Pago simulado, si se paga todo, pasa de pendiente  a confirmda
        boolean cubreSaldo = saldo.subtract(request.monto()).compareTo(BigDecimal.ZERO) == 0;
        if (cubreSaldo && reserva.getEstadoReserva() == EstadoReserva.PENDIENTE) {
            reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
        }

        return reservaService.obtener(reservaId, actual);
    }

    // Mismo control de acceso que las reservas: dueño o personal
    private void verificarAcceso(Reserva reserva, UserDetails actual) {
        boolean staff = actual.getAuthorities().stream()
                .anyMatch(a -> !ROL_HUESPED.equals(a.getAuthority()));
        if (staff) {
            return;
        }

        Usuario usuario = usuarioRepository.findByEmail(actual.getUsername())
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el usuario autenticado."));

        if (reserva.getUsuario() == null || !reserva.getUsuario().getId().equals(usuario.getId())) {
            throw ApiException.noEncontrado("No se encontró la reserva con id " + reserva.getId() + ".");
        }
    }
}