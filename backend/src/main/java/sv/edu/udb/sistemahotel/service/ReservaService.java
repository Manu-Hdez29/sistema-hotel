package sv.edu.udb.sistemahotel.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.dto.ReservaCrearRequest;
import sv.edu.udb.sistemahotel.dto.ReservaResponse;
import sv.edu.udb.sistemahotel.exception.ApiException;
import sv.edu.udb.sistemahotel.model.Habitacion;
import sv.edu.udb.sistemahotel.model.Huesped;
import sv.edu.udb.sistemahotel.model.Pago;
import sv.edu.udb.sistemahotel.model.Reserva;
import sv.edu.udb.sistemahotel.model.ReservaHuesped;
import sv.edu.udb.sistemahotel.model.ReservaServicio;
import sv.edu.udb.sistemahotel.model.Servicio;
import sv.edu.udb.sistemahotel.model.Tarifa;
import sv.edu.udb.sistemahotel.model.Usuario;
import sv.edu.udb.sistemahotel.model.enums.EstadoDisponibilidad;
import sv.edu.udb.sistemahotel.model.enums.EstadoGeneral;
import sv.edu.udb.sistemahotel.model.enums.EstadoHabitacion;
import sv.edu.udb.sistemahotel.model.enums.EstadoPago;
import sv.edu.udb.sistemahotel.model.enums.EstadoReserva;
import sv.edu.udb.sistemahotel.repository.DisponibilidadRepository;
import sv.edu.udb.sistemahotel.repository.HabitacionRepository;
import sv.edu.udb.sistemahotel.repository.HuespedRepository;
import sv.edu.udb.sistemahotel.repository.PagoRepository;
import sv.edu.udb.sistemahotel.repository.ReservaHuespedRepository;
import sv.edu.udb.sistemahotel.repository.ReservaRepository;
import sv.edu.udb.sistemahotel.repository.ReservaServicioRepository;
import sv.edu.udb.sistemahotel.repository.ServicioRepository;
import sv.edu.udb.sistemahotel.repository.TarifaRepository;
import sv.edu.udb.sistemahotel.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    private static final List<EstadoReserva> ESTADOS_ACTIVOS =
            List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);

    private static final List<EstadoDisponibilidad> ESTADOS_NO_DISPONIBLES =
            List.of(EstadoDisponibilidad.BLOQUEADA, EstadoDisponibilidad.OCUPADA);

    private static final String ROL_HUESPED = "ROLE_HUESPED";

    private final ReservaRepository reservaRepository;
    private final HabitacionRepository habitacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final HuespedRepository huespedRepository;
    private final ServicioRepository servicioRepository;
    private final TarifaRepository tarifaRepository;
    private final DisponibilidadRepository disponibilidadRepository;
    private final ReservaHuespedRepository reservaHuespedRepository;
    private final ReservaServicioRepository reservaServicioRepository;
    private final PagoRepository pagoRepository;

    public ReservaService(ReservaRepository reservaRepository,
                          HabitacionRepository habitacionRepository,
                          UsuarioRepository usuarioRepository,
                          HuespedRepository huespedRepository,
                          ServicioRepository servicioRepository,
                          TarifaRepository tarifaRepository,
                          DisponibilidadRepository disponibilidadRepository,
                          ReservaHuespedRepository reservaHuespedRepository,
                          ReservaServicioRepository reservaServicioRepository,
                          PagoRepository pagoRepository) {
        this.reservaRepository = reservaRepository;
        this.habitacionRepository = habitacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.huespedRepository = huespedRepository;
        this.servicioRepository = servicioRepository;
        this.tarifaRepository = tarifaRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.reservaHuespedRepository = reservaHuespedRepository;
        this.reservaServicioRepository = reservaServicioRepository;
        this.pagoRepository = pagoRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listar(EstadoReserva estado, LocalDate desde, LocalDate hasta, UserDetails actual) {
        Usuario usuario = usuarioActual(actual);
        boolean staff = esStaff(actual);

        return reservaRepository.findAll().stream()
                .filter(r -> staff || esDuenoDe(r, usuario))
                .filter(r -> estado == null || r.getEstadoReserva() == estado)
                .filter(r -> desde == null || !r.getFechaEntrada().isBefore(desde))
                .filter(r -> hasta == null || !r.getFechaEntrada().isAfter(hasta))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtener(Integer id, UserDetails actual) {
        Reserva reserva = buscar(id);
        verificarAcceso(reserva, actual);
        return toResponse(reserva);
    }

    @Transactional
    public ReservaResponse crear(ReservaCrearRequest request, UserDetails actual) {
        Usuario usuario = usuarioActual(actual);
        boolean esHuesped = !esStaff(actual);

        LocalDate fechaEntrada = request.fechaEntrada();
        LocalDate fechaSalida = request.fechaSalida();
        validarFechas(fechaEntrada, fechaSalida);

        Habitacion habitacion = habitacionRepository.findById(request.habitacionId())
                .orElseThrow(() -> ApiException.noEncontrado(
                        "No se encontró la habitación con id " + request.habitacionId() + "."));

        // Un hotel INACTIVO no acepta reservas, aunque alguien conozca el id de la habitación
        if (habitacion.getSucursal().getHotel().getEstado() != EstadoGeneral.ACTIVO) {
            throw ApiException.conflicto("El hotel no está disponible para reservas en este momento.");
        }

        if (!estaLibre(habitacion, fechaEntrada, fechaSalida)) {
            throw ApiException.conflicto("La habitación no está disponible para las fechas indicadas.");
        }

        Tarifa tarifa = tarifaVigente(habitacion.getSucursal().getId(),
                habitacion.getTipoHabitacion().getId(), fechaEntrada);
        if (tarifa == null) {
            throw ApiException.solicitudInvalida("La habitación no tiene tarifa vigente para la fecha de entrada.");
        }

        Huesped titular = obtenerOCrearTitular(request.titular(), usuario, esHuesped);

        List<Integer> idsHuespedes = request.huespedesIds() == null ? List.of() : request.huespedesIds();
        if (idsHuespedes.contains(titular.getId())) {
            throw ApiException.solicitudInvalida("El titular no debe repetirse en la lista de huéspedes.");
        }

        List<Huesped> huespedes = new ArrayList<>();
        for (Integer idHuesped : idsHuespedes) {
            huespedes.add(huespedRepository.findById(idHuesped)
                    .orElseThrow(() -> ApiException.noEncontrado(
                            "No se encontró el huésped con id " + idHuesped + ".")));
        }

        int personas = 1 + huespedes.size();
        int capacidad = habitacion.getTipoHabitacion().getCapacidadMaxima();
        if (personas > capacidad) {
            throw ApiException.solicitudInvalida(
                    "La habitación admite máximo " + capacidad + " personas, contando al titular.");
        }

        List<ReservaServicio> servicios = new ArrayList<>();
        BigDecimal totalServicios = BigDecimal.ZERO;
        if (request.servicios() != null) {
            for (ReservaCrearRequest.ServicioSolicitud solicitud : request.servicios()) {
                Servicio servicio = servicioRepository.findById(solicitud.servicioId())
                        .orElseThrow(() -> ApiException.noEncontrado(
                                "No se encontró el servicio con id " + solicitud.servicioId() + "."));

                BigDecimal subtotal = servicio.getPrecio().multiply(BigDecimal.valueOf(solicitud.cantidad()));

                ReservaServicio reservaServicio = new ReservaServicio();
                reservaServicio.setServicio(servicio);
                reservaServicio.setCantidad(solicitud.cantidad());
                reservaServicio.setSubtotal(subtotal);
                servicios.add(reservaServicio);

                totalServicios = totalServicios.add(subtotal);
            }
        }

        int noches = (int) ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
        BigDecimal totalHospedaje = tarifa.getPrecioNoche().multiply(BigDecimal.valueOf(noches));

        Reserva reserva = new Reserva();
        reserva.setHabitacion(habitacion);
        reserva.setUsuario(esHuesped ? usuario : null);
        reserva.setFechaEntrada(fechaEntrada);
        reserva.setFechaSalida(fechaSalida);
        reserva.setNumeroNoches(noches);
        reserva.setSolicitudesEspeciales(limpiar(request.solicitudesEspeciales()));
        reserva.setPrecioTotal(totalHospedaje.add(totalServicios));
        reserva.setEstadoReserva(EstadoReserva.PENDIENTE);
        reservaRepository.save(reserva);

        guardarPersona(reserva, titular, true);
        for (Huesped huesped : huespedes) {
            guardarPersona(reserva, huesped, false);
        }
        for (ReservaServicio reservaServicio : servicios) {
            reservaServicio.setReserva(reserva);
            reservaServicioRepository.save(reservaServicio);
        }

        return toResponse(reserva);
    }

    private Huesped obtenerOCrearTitular(ReservaCrearRequest.TitularRequest datos, Usuario usuario, boolean esHuesped) {
        String documento = datos.documentoIdentidad().trim();

        // La cuenta solo se liga al perfil si el titular es la misma persona que reserva.
        // Si reserva para un familiar u otra persona, ese perfil no queda ligado a su cuenta.
        boolean titularEsQuienReserva = esHuesped && huespedRepository.findByUsuarioId(usuario.getId())
                .map(propio -> propio.getDocumentoIdentidad().equals(documento))
                .orElse(false);

        Optional<Huesped> existente = huespedRepository.findByDocumentoIdentidad(documento);
        if (existente.isPresent()) {
            Huesped huesped = existente.get();
            if (titularEsQuienReserva && huesped.getUsuario() == null) {
                huesped.setUsuario(usuario);
            }
            return huesped;
        }

        Huesped nuevo = new Huesped();
        nuevo.setNombreCompleto(datos.nombreCompleto().trim());
        nuevo.setFechaNacimiento(datos.fechaNacimiento());
        nuevo.setDocumentoIdentidad(documento);
        nuevo.setTelefono(datos.telefono().trim());
        nuevo.setEmail(datos.email().trim().toLowerCase());
        if (titularEsQuienReserva) {
            nuevo.setUsuario(usuario);
        }
        huespedRepository.save(nuevo);
        return nuevo;
    }

    private void guardarPersona(Reserva reserva, Huesped huesped, boolean titular) {
        ReservaHuesped reservaHuesped = new ReservaHuesped();
        reservaHuesped.setReserva(reserva);
        reservaHuesped.setHuesped(huesped);
        reservaHuesped.setEsTitular(titular);
        reservaHuespedRepository.save(reservaHuesped);
    }

    private boolean estaLibre(Habitacion habitacion, LocalDate fechaEntrada, LocalDate fechaSalida) {
        if (habitacion.getEstado() != EstadoHabitacion.DISPONIBLE) {
            return false;
        }
        boolean ocupada = reservaRepository
                .findHabitacionIdsOcupadasEnRango(ESTADOS_ACTIVOS, fechaEntrada, fechaSalida)
                .contains(habitacion.getId());
        boolean noDisponible = disponibilidadRepository
                .findHabitacionIdsNoDisponiblesEnRango(ESTADOS_NO_DISPONIBLES, fechaEntrada, fechaSalida)
                .contains(habitacion.getId());
        return !ocupada && !noDisponible;
    }

    private Tarifa tarifaVigente(Integer sucursalId, Integer tipoHabitacionId, LocalDate fecha) {
        return tarifaRepository.findBySucursalIdAndTipoHabitacionId(sucursalId, tipoHabitacionId).stream()
                .filter(t -> !t.getFechaInicioVigencia().isAfter(fecha))
                .filter(t -> t.getFechaFinVigencia() == null || !t.getFechaFinVigencia().isBefore(fecha))
                .max((a, b) -> a.getFechaInicioVigencia().compareTo(b.getFechaInicioVigencia()))
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

    private Reserva buscar(Integer id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró la reserva con id " + id + "."));
    }

    // Los huéspedes solo ven sus propias reservas
    private void verificarAcceso(Reserva reserva, UserDetails actual) {
        if (!esStaff(actual) && !esDuenoDe(reserva, usuarioActual(actual))) {
            throw ApiException.noEncontrado("No se encontró la reserva con id " + reserva.getId() + ".");
        }
    }

    private Usuario usuarioActual(UserDetails actual) {
        return usuarioRepository.findByEmail(actual.getUsername())
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el usuario autenticado."));
    }

    private static boolean esStaff(UserDetails actual) {
        return actual.getAuthorities().stream()
                .anyMatch(a -> !ROL_HUESPED.equals(a.getAuthority()));
    }

    private static boolean esDuenoDe(Reserva reserva, Usuario usuario) {
        return reserva.getUsuario() != null && reserva.getUsuario().getId().equals(usuario.getId());
    }

    private static String limpiar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private ReservaResponse toResponse(Reserva reserva) {
        List<ReservaHuesped> personas = reservaHuespedRepository.findByReservaId(reserva.getId());

        ReservaResponse.PersonaResumen titular = personas.stream()
                .filter(rh -> Boolean.TRUE.equals(rh.getEsTitular()))
                .map(rh -> persona(rh.getHuesped()))
                .findFirst()
                .orElse(null);

        List<ReservaResponse.PersonaResumen> huespedes = personas.stream()
                .map(rh -> persona(rh.getHuesped()))
                .toList();

        List<ReservaResponse.ServicioReservado> servicios = reservaServicioRepository
                .findByReservaId(reserva.getId()).stream()
                .map(rs -> new ReservaResponse.ServicioReservado(
                        rs.getServicio().getId(),
                        rs.getServicio().getNombre(),
                        rs.getCantidad(),
                        rs.getSubtotal()))
                .toList();

        List<Pago> pagosEntidad = pagoRepository.findByReservaId(reserva.getId());

        List<ReservaResponse.PagoResumen> pagos = pagosEntidad.stream()
                .map(p -> new ReservaResponse.PagoResumen(
                        p.getId(), p.getMetodoPago(), p.getMonto(), p.getFechaPago(), p.getEstadoPago()))
                .toList();

        BigDecimal totalPagado = pagosEntidad.stream()
                .filter(p -> p.getEstadoPago() == EstadoPago.COMPLETADO)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldo = reserva.getPrecioTotal().subtract(totalPagado).max(BigDecimal.ZERO);

        return new ReservaResponse(
                reserva.getId(),
                reserva.getEstadoReserva(),
                reserva.getHabitacion().getId(),
                reserva.getHabitacion().getNumeroHabitacion(),
                reserva.getHabitacion().getSucursal().getHotel().getNombre(),
                reserva.getHabitacion().getSucursal().getNombreSucursal(),
                reserva.getHabitacion().getTipoHabitacion().getNombre(),
                reserva.getFechaEntrada(),
                reserva.getFechaSalida(),
                reserva.getNumeroNoches(),
                reserva.getPrecioTotal(),
                totalPagado,
                saldo,
                reserva.getSolicitudesEspeciales(),
                titular,
                huespedes,
                servicios,
                pagos,
                reserva.getFechaCreacion());
    }

    private static ReservaResponse.PersonaResumen persona(Huesped huesped) {
        return new ReservaResponse.PersonaResumen(huesped.getId(), huesped.getNombreCompleto());
    }
}