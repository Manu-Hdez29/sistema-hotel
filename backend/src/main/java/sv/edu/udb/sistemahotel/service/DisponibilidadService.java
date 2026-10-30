package sv.edu.udb.sistemahotel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.dto.DisponibilidadResponse;
import sv.edu.udb.sistemahotel.exception.ApiException;
import sv.edu.udb.sistemahotel.model.Habitacion;
import sv.edu.udb.sistemahotel.model.Sucursal;
import sv.edu.udb.sistemahotel.model.Tarifa;
import sv.edu.udb.sistemahotel.model.TipoHabitacion;
import sv.edu.udb.sistemahotel.model.enums.EstadoDisponibilidad;
import sv.edu.udb.sistemahotel.model.enums.EstadoGeneral;
import sv.edu.udb.sistemahotel.model.enums.EstadoHabitacion;
import sv.edu.udb.sistemahotel.model.enums.EstadoReserva;
import sv.edu.udb.sistemahotel.repository.DisponibilidadRepository;
import sv.edu.udb.sistemahotel.repository.HabitacionRepository;
import sv.edu.udb.sistemahotel.repository.ReservaRepository;
import sv.edu.udb.sistemahotel.repository.TarifaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DisponibilidadService {

    private static final List<EstadoReserva> ESTADOS_ACTIVOS =
            List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);

    private static final List<EstadoDisponibilidad> ESTADOS_NO_DISPONIBLES =
            List.of(EstadoDisponibilidad.BLOQUEADA, EstadoDisponibilidad.OCUPADA);

    private final HabitacionRepository habitacionRepository;
    private final ReservaRepository reservaRepository;
    private final DisponibilidadRepository disponibilidadRepository;
    private final TarifaRepository tarifaRepository;

    public DisponibilidadService(HabitacionRepository habitacionRepository,
                                 ReservaRepository reservaRepository,
                                 DisponibilidadRepository disponibilidadRepository,
                                 TarifaRepository tarifaRepository) {
        this.habitacionRepository = habitacionRepository;
        this.reservaRepository = reservaRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.tarifaRepository = tarifaRepository;
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadResponse> buscar(LocalDate fechaEntrada,
                                               LocalDate fechaSalida,
                                               Integer huespedes,
                                               String ciudad,
                                               Integer tipoHabitacionId) {
        validar(fechaEntrada, fechaSalida, huespedes);

        // Habitaciones que no se pueden ofrecer en el rango
        Set<Integer> noDisponibles = new HashSet<>(
                reservaRepository.findHabitacionIdsOcupadasEnRango(ESTADOS_ACTIVOS, fechaEntrada, fechaSalida));
        noDisponibles.addAll(
                disponibilidadRepository.findHabitacionIdsNoDisponiblesEnRango(
                        ESTADOS_NO_DISPONIBLES, fechaEntrada, fechaSalida));

        int noches = (int) ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
        String ciudadFiltro = (ciudad == null || ciudad.isBlank()) ? null : ciudad.trim();

        List<DisponibilidadResponse> resultado = new ArrayList<>();

        for (Habitacion habitacion : habitacionRepository.findAll()) {
            TipoHabitacion tipo = habitacion.getTipoHabitacion();
            Sucursal sucursal = habitacion.getSucursal();

            boolean cumple = habitacion.getEstado() == EstadoHabitacion.DISPONIBLE
                    // Un hotel INACTIVO no se ofrece en la búsqueda
                    && sucursal.getHotel().getEstado() == EstadoGeneral.ACTIVO
                    && tipo.getCapacidadMaxima() >= huespedes
                    && (tipoHabitacionId == null || tipo.getId().equals(tipoHabitacionId))
                    && (ciudadFiltro == null || sucursal.getCiudad().equalsIgnoreCase(ciudadFiltro))
                    && !noDisponibles.contains(habitacion.getId());

            if (!cumple) {
                continue;
            }

            // Sin tarifa vigente el día de entrada, la habitación no aparece
            Tarifa tarifa = tarifaVigente(sucursal.getId(), tipo.getId(), fechaEntrada);
            if (tarifa == null) {
                continue;
            }

            resultado.add(new DisponibilidadResponse(
                    habitacion.getId(),
                    habitacion.getNumeroHabitacion(),
                    sucursal.getHotel().getId(),
                    sucursal.getHotel().getNombre(),
                    sucursal.getId(),
                    sucursal.getNombreSucursal(),
                    sucursal.getCiudad(),
                    tipo.getNombre(),
                    tipo.getCapacidadMaxima(),
                    tarifa.getPrecioNoche(),
                    noches,
                    tarifa.getPrecioNoche().multiply(BigDecimal.valueOf(noches)),
                    List.of()));
        }

        return resultado;
    }

    private Tarifa tarifaVigente(Integer sucursalId, Integer tipoHabitacionId, LocalDate fecha) {
        return tarifaRepository.findBySucursalIdAndTipoHabitacionId(sucursalId, tipoHabitacionId).stream()
                .filter(t -> !t.getFechaInicioVigencia().isAfter(fecha))
                .filter(t -> t.getFechaFinVigencia() == null || !t.getFechaFinVigencia().isBefore(fecha))
                .max(Comparator.comparing(Tarifa::getFechaInicioVigencia))
                .orElse(null);
    }

    private static void validar(LocalDate fechaEntrada, LocalDate fechaSalida, Integer huespedes) {
        if (fechaEntrada.isBefore(LocalDate.now())) {
            throw ApiException.solicitudInvalida("La fecha de entrada no puede ser anterior a hoy.");
        }
        if (!fechaSalida.isAfter(fechaEntrada)) {
            throw ApiException.solicitudInvalida("La fecha de salida debe ser posterior a la fecha de entrada.");
        }
        if (huespedes < 1 || huespedes > 10) {
            throw ApiException.solicitudInvalida("La cantidad de huéspedes debe estar entre 1 y 10.");
        }
    }
}