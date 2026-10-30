package sv.edu.udb.sistemahotel.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.dto.CancelacionResponse;
import sv.edu.udb.sistemahotel.dto.CancelarRequest;
import sv.edu.udb.sistemahotel.dto.FechasRequest;
import sv.edu.udb.sistemahotel.dto.ReservaResponse;
import sv.edu.udb.sistemahotel.exception.ApiException;
import sv.edu.udb.sistemahotel.model.Cancelacion;
import sv.edu.udb.sistemahotel.model.Habitacion;
import sv.edu.udb.sistemahotel.model.Pago;
import sv.edu.udb.sistemahotel.model.PoliticaCancelacion;
import sv.edu.udb.sistemahotel.model.Reserva;
import sv.edu.udb.sistemahotel.model.ReservaServicio;
import sv.edu.udb.sistemahotel.model.Tarifa;
import sv.edu.udb.sistemahotel.model.TipoHabitacion;
import sv.edu.udb.sistemahotel.model.Usuario;
import sv.edu.udb.sistemahotel.model.enums.EstadoDisponibilidad;
import sv.edu.udb.sistemahotel.model.enums.EstadoHabitacion;
import sv.edu.udb.sistemahotel.model.enums.EstadoPago;
import sv.edu.udb.sistemahotel.model.enums.EstadoReserva;
import sv.edu.udb.sistemahotel.repository.CancelacionRepository;
import sv.edu.udb.sistemahotel.repository.DisponibilidadRepository;
import sv.edu.udb.sistemahotel.repository.PagoRepository;
import sv.edu.udb.sistemahotel.repository.PoliticaCancelacionRepository;
import sv.edu.udb.sistemahotel.repository.ReservaRepository;
import sv.edu.udb.sistemahotel.repository.ReservaServicioRepository;
import sv.edu.udb.sistemahotel.repository.TarifaRepository;
import sv.edu.udb.sistemahotel.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
public class ReservaEstadoService {

    private static final String ROL_HUESPED = "ROLE_HUESPED";

    private static final List<EstadoReserva> ESTADOS_ACTIVOS =
            List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);

    private static final List<EstadoDisponibilidad> ESTADOS_NO_DISPONIBLES =
            List.of(EstadoDisponibilidad.BLOQUEADA, EstadoDisponibilidad.OCUPADA);

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PagoRepository pagoRepository;
    private final PoliticaCancelacionRepository politicaRepository;
    private final CancelacionRepository cancelacionRepository;
    private final TarifaRepository tarifaRepository;
    private final DisponibilidadRepository disponibilidadRepository;
    private final ReservaServicioRepository reservaServicioRepository;
    private final ReservaService reservaService;

    public ReservaEstadoService(ReservaRepository reservaRepository,
                                UsuarioRepository usuarioRepository,
                                PagoRepository pagoRepository,
                                PoliticaCancelacionRepository politicaRepository,
                                CancelacionRepository cancelacionRepository,
                                TarifaRepository tarifaRepository,
                                DisponibilidadRepository disponibilidadRepository,
                                ReservaServicioRepository reservaServicioRepository,
                                ReservaService reservaService) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.pagoRepository = pagoRepository;
        this.politicaRepository = politicaRepository;
        this.cancelacionRepository = cancelacionRepository;
        this.tarifaRepository = tarifaRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.reservaServicioRepository = reservaServicioRepository;
        this.reservaService = reservaService;
    }

    @Transactional
    public CancelacionResponse cancelar(Integer id, CancelarRequest request, UserDetails actual) {
        Reserva reserva = buscarConAcceso(id, actual);

        if (!estaActiva(reserva)) {
            throw ApiException.conflicto("Solo se pueden cancelar reservas pendientes o confirmadas.");
        }

        long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), reserva.getFechaEntrada());
        if (diasRestantes < 0) {
            throw ApiException.conflicto("No se puede cancelar una reserva cuya fecha de entrada ya pasó.");
        }

        Integer hotelId = reserva.getHabitacion().getSucursal().getHotel().getId();
        PoliticaCancelacion politica = politicaAplicable(hotelId, (int) diasRestantes);

        BigDecimal porcentaje = politica == null ? BigDecimal.valueOf(100) : politica.getPorcentajeCargo();
        BigDecimal cargo = reserva.getPrecioTotal()
                .multiply(porcentaje)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal pagado = totalPagado(id);
        BigDecimal reembolso = pagado.subtract(cargo).max(BigDecimal.ZERO);

        Cancelacion cancelacion = new Cancelacion();
        cancelacion.setReserva(reserva);
        cancelacion.setPolitica(politica);
        cancelacion.setMotivo(request.motivo().trim());
        cancelacion.setMontoReembolso(reembolso);
        cancelacionRepository.save(cancelacion);

        reserva.setEstadoReserva(EstadoReserva.CANCELADA);

        return new CancelacionResponse(
                reserva.getId(),
                politica == null ? null : politica.getId(),
                porcentaje,
                cargo,
                pagado,
                reembolso);
    }

    @Transactional
    public ReservaResponse cambiarFechas(Integer id, FechasRequest request, UserDetails actual) {
        Reserva reserva = buscarConAcceso(id, actual);

        if (!estaActiva(reserva)) {
            throw ApiException.conflicto("Solo se pueden cambiar las fechas de reservas pendientes o confirmadas.");
        }

        LocalDate fechaEntrada = request.fechaEntrada();
        LocalDate fechaSalida = request.fechaSalida();
        validarFechas(fechaEntrada, fechaSalida);

        Habitacion habitacion = reserva.getHabitacion();
        if (!estaLibreExcluyendo(habitacion, fechaEntrada, fechaSalida, reserva.getId())) {
            throw ApiException.conflicto("La habitación no está disponible para las nuevas fechas.");
        }

        Tarifa tarifa = tarifaVigente(habitacion.getSucursal().getId(),
                habitacion.getTipoHabitacion().getId(), fechaEntrada);
        if (tarifa == null) {
            throw ApiException.solicitudInvalida("No hay tarifa vigente para la nueva fecha de entrada.");
        }

        BigDecimal totalServicios = reservaServicioRepository.findByReservaId(id).stream()
                .map(ReservaServicio::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int noches = (int) ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);

        reserva.setFechaEntrada(fechaEntrada);
        reserva.setFechaSalida(fechaSalida);
        reserva.setNumeroNoches(noches);
        reserva.setPrecioTotal(tarifa.getPrecioNoche()
                .multiply(BigDecimal.valueOf(noches))
                .add(totalServicios));

        return reservaService.obtener(id, actual);
    }

    @Transactional
    public ReservaResponse finalizar(Integer id, UserDetails actual) {
        Reserva reserva = buscar(id);

        if (reserva.getEstadoReserva() != EstadoReserva.CONFIRMADA) {
            throw ApiException.conflicto("Solo se pueden finalizar reservas confirmadas.");
        }

        BigDecimal saldo = reserva.getPrecioTotal().subtract(totalPagado(id));
        if (saldo.compareTo(BigDecimal.ZERO) > 0) {
            throw ApiException.conflicto(
                    "No se puede finalizar la reserva con saldo pendiente de $" + saldo + ".");
        }

        reserva.setEstadoReserva(EstadoReserva.FINALIZADA);
        return reservaService.obtener(id, actual);
    }

    private PoliticaCancelacion politicaAplicable(Integer hotelId, int diasRestantes) {
        return politicaRepository.findByHotelId(hotelId).stream()
                .filter(p -> p.getDiasPrevios() <= diasRestantes)
                .max(Comparator.comparing(PoliticaCancelacion::getDiasPrevios))
                .orElse(null);
    }

    private BigDecimal totalPagado(Integer reservaId) {
        return pagoRepository.findByReservaId(reservaId).stream()
                .filter(p -> p.getEstadoPago() == EstadoPago.COMPLETADO)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean estaLibreExcluyendo(Habitacion habitacion, LocalDate fechaEntrada,
                                        LocalDate fechaSalida, Integer reservaId) {
        if (habitacion.getEstado() != EstadoHabitacion.DISPONIBLE) {
            return false;
        }
        boolean ocupada = !reservaRepository.findIdsReservasOtrasEnRango(
                habitacion.getId(), ESTADOS_ACTIVOS, fechaEntrada, fechaSalida, reservaId).isEmpty();
        boolean noDisponible = disponibilidadRepository
                .findHabitacionIdsNoDisponiblesEnRango(ESTADOS_NO_DISPONIBLES, fechaEntrada, fechaSalida)
                .contains(habitacion.getId());
        return !ocupada && !noDisponible;
    }

    private Tarifa tarifaVigente(Integer sucursalId, Integer tipoHabitacionId, LocalDate fecha) {
        return tarifaRepository.findBySucursalIdAndTipoHabitacionId(sucursalId, tipoHabitacionId).stream()
                .filter(t -> !t.getFechaInicioVigencia().isAfter(fecha))
                .filter(t -> t.getFechaFinVigencia() == null || !t.getFechaFinVigencia().isBefore(fecha))
                .max(Comparator.comparing(Tarifa::getFechaInicioVigencia))
                .orElse(null);
    }

    private static void validarFechas(LocalDate fechaEntrada, LocalDate fechaSalida) {
        if (fechaEntrada.isBefore(LocalDate.now())) {
            throw ApiException.solicitudInvalida("La fecha de entrada no puede ser anterior a hoy.");
        }
        if (!fechaSalida.isAfter(fechaEntrada)) {
            throw ApiException.solicitudInvalida("La fecha de salida debe ser posterior a la fecha de entrada.");
        }
    }

    private static boolean estaActiva(Reserva reserva) {
        return reserva.getEstadoReserva() == EstadoReserva.PENDIENTE
                || reserva.getEstadoReserva() == EstadoReserva.CONFIRMADA;
    }

    private Reserva buscar(Integer id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró la reserva con id " + id + "."));
    }

    // Dueño o personal; a otro huésped le va a responder el codigo 404
    private Reserva buscarConAcceso(Integer id, UserDetails actual) {
        Reserva reserva = buscar(id);

        boolean staff = actual.getAuthorities().stream()
                .anyMatch(a -> !ROL_HUESPED.equals(a.getAuthority()));
        if (staff) {
            return reserva;
        }

        Usuario usuario = usuarioRepository.findByEmail(actual.getUsername())
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el usuario autenticado."));

        if (reserva.getUsuario() == null || !reserva.getUsuario().getId().equals(usuario.getId())) {
            throw ApiException.noEncontrado("No se encontró la reserva con id " + id + ".");
        }
        return reserva;
    }
}