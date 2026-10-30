package sv.edu.udb.sistemahotel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.dto.EstadisticasResponse;
import sv.edu.udb.sistemahotel.dto.PagoListadoResponse;
import sv.edu.udb.sistemahotel.exception.ApiException;
import sv.edu.udb.sistemahotel.model.Cancelacion;
import sv.edu.udb.sistemahotel.model.Pago;
import sv.edu.udb.sistemahotel.model.Reserva;
import sv.edu.udb.sistemahotel.model.enums.EstadoHabitacion;
import sv.edu.udb.sistemahotel.model.enums.EstadoPago;
import sv.edu.udb.sistemahotel.model.enums.EstadoReserva;
import sv.edu.udb.sistemahotel.repository.CancelacionRepository;
import sv.edu.udb.sistemahotel.repository.HabitacionRepository;
import sv.edu.udb.sistemahotel.repository.PagoRepository;
import sv.edu.udb.sistemahotel.repository.ReservaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConsultasService {

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;
    private final CancelacionRepository cancelacionRepository;
    private final HabitacionRepository habitacionRepository;

    public ConsultasService(PagoRepository pagoRepository,
                            ReservaRepository reservaRepository,
                            CancelacionRepository cancelacionRepository,
                            HabitacionRepository habitacionRepository) {
        this.pagoRepository = pagoRepository;
        this.reservaRepository = reservaRepository;
        this.cancelacionRepository = cancelacionRepository;
        this.habitacionRepository = habitacionRepository;
    }

    @Transactional(readOnly = true)
    public List<PagoListadoResponse> listarPagos(LocalDate desde, LocalDate hasta) {
        return pagoRepository.findAll().stream()
                .filter(p -> desde == null || !p.getFechaPago().toLocalDate().isBefore(desde))
                .filter(p -> hasta == null || !p.getFechaPago().toLocalDate().isAfter(hasta))
                .map(p -> new PagoListadoResponse(
                        p.getId(),
                        p.getReserva().getId(),
                        p.getMetodoPago(),
                        p.getMonto(),
                        p.getFechaPago(),
                        p.getEstadoPago()))
                .toList();
    }

    @Transactional(readOnly = true)
    public EstadisticasResponse estadisticas(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) {
            throw ApiException.solicitudInvalida("La fecha hasta no puede ser anterior a la fecha desde.");
        }

        // Reservas cuya fecha de entrada cae dentro del rango
        List<Reserva> reservas = reservaRepository.findAll().stream()
                .filter(r -> !r.getFechaEntrada().isBefore(desde) && !r.getFechaEntrada().isAfter(hasta))
                .toList();

        Set<Integer> idsReservas = reservas.stream()
                .map(Reserva::getId)
                .collect(Collectors.toSet());

        Map<EstadoReserva, Long> porEstado = new EnumMap<>(EstadoReserva.class);
        for (EstadoReserva estado : EstadoReserva.values()) {
            porEstado.put(estado, 0L);
        }
        reservas.forEach(r -> porEstado.merge(r.getEstadoReserva(), 1L, Long::sum));

        long nochesOcupadas = reservas.stream()
                .filter(r -> r.getEstadoReserva() == EstadoReserva.CONFIRMADA
                        || r.getEstadoReserva() == EstadoReserva.FINALIZADA)
                .mapToLong(Reserva::getNumeroNoches)
                .sum();

        List<Cancelacion> cancelaciones = cancelacionRepository.findAll().stream()
                .filter(c -> idsReservas.contains(c.getReserva().getId()))
                .toList();

        BigDecimal ingresos = pagoRepository.findAll().stream()
                .filter(p -> p.getEstadoPago() == EstadoPago.COMPLETADO)
                .filter(p -> idsReservas.contains(p.getReserva().getId()))
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal reembolsos = cancelaciones.stream()
                .map(Cancelacion::getMontoReembolso)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Días del rango, contando desde y hasta
        long dias = ChronoUnit.DAYS.between(desde, hasta) + 1;
        // Solo cuentan las habitaciones que se pueden ofrecer; las de mantenimiento no suman
        long habitacionesOfrecibles = habitacionRepository.findAll().stream()
                .filter(h -> h.getEstado() == EstadoHabitacion.DISPONIBLE)
                .count();
        long nochesDisponibles = habitacionesOfrecibles * dias;

        BigDecimal ocupacion = nochesDisponibles == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(nochesOcupadas)
                  .multiply(BigDecimal.valueOf(100))
                  .divide(BigDecimal.valueOf(nochesDisponibles), 2, RoundingMode.HALF_UP);

        // Ingreso neto: lo cobrado menos lo devuelto por cancelaciones
        BigDecimal ingresosNetos = ingresos.subtract(reembolsos);

        return new EstadisticasResponse(
                desde,
                hasta,
                reservas.size(),
                porEstado,
                cancelaciones.size(),
                ingresosNetos,
                reembolsos,
                nochesOcupadas,
                nochesDisponibles,
                ocupacion);
    }
}