package sv.edu.udb.sistemahotel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.dto.HabitacionRequest;
import sv.edu.udb.sistemahotel.dto.HabitacionResponse;
import sv.edu.udb.sistemahotel.exception.ApiException;
import sv.edu.udb.sistemahotel.model.Habitacion;
import sv.edu.udb.sistemahotel.model.Sucursal;
import sv.edu.udb.sistemahotel.model.TipoHabitacion;
import sv.edu.udb.sistemahotel.model.enums.EstadoHabitacion;
import sv.edu.udb.sistemahotel.repository.HabitacionRepository;
import sv.edu.udb.sistemahotel.repository.SucursalRepository;
import sv.edu.udb.sistemahotel.repository.TipoHabitacionRepository;

import java.util.List;

@Service
public class HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final SucursalRepository sucursalRepository;
    private final TipoHabitacionRepository tipoHabitacionRepository;

    public HabitacionService(HabitacionRepository habitacionRepository,
                             SucursalRepository sucursalRepository,
                             TipoHabitacionRepository tipoHabitacionRepository) {
        this.habitacionRepository = habitacionRepository;
        this.sucursalRepository = sucursalRepository;
        this.tipoHabitacionRepository = tipoHabitacionRepository;
    }

    @Transactional(readOnly = true)
    public List<HabitacionResponse> listar(Integer sucursalId) {
        List<Habitacion> habitaciones = sucursalId == null
                ? habitacionRepository.findAll()
                : habitacionRepository.findBySucursalId(sucursalId);
        return habitaciones.stream().map(HabitacionService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public HabitacionResponse obtener(Integer id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public HabitacionResponse crear(HabitacionRequest request) {
        Habitacion habitacion = new Habitacion();
        habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
        aplicar(habitacion, request);
        habitacionRepository.save(habitacion);
        return toResponse(habitacion);
    }

    @Transactional
    public HabitacionResponse actualizar(Integer id, HabitacionRequest request) {
        Habitacion habitacion = buscar(id);
        aplicar(habitacion, request);
        return toResponse(habitacion);
    }

    @Transactional
    public void eliminar(Integer id) {
        // Si la habitación tiene disponibilidad o reservas, la base de datos lo impedirá
        // y GlobalExceptionHandler responderá 409
        habitacionRepository.delete(buscar(id));
    }

    private Habitacion buscar(Integer id) {
        return habitacionRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró la habitación con id " + id + "."));
    }

    private void aplicar(Habitacion habitacion, HabitacionRequest request) {
        String numero = request.numeroHabitacion().trim();

        // El número no puede repetirse dentro de la misma sucursal
        boolean repetido = habitacion.getId() == null
                ? habitacionRepository.existsBySucursalIdAndNumeroHabitacion(request.sucursalId(), numero)
                : habitacionRepository.existsBySucursalIdAndNumeroHabitacionAndIdNot(
                request.sucursalId(), numero, habitacion.getId());

        if (repetido) {
            throw ApiException.conflicto("Ya existe la habitación " + numero + " en esa sucursal.");
        }

        Sucursal sucursal = sucursalRepository.findById(request.sucursalId())
                .orElseThrow(() -> ApiException.noEncontrado(
                        "No se encontró la sucursal con id " + request.sucursalId() + "."));

        TipoHabitacion tipo = tipoHabitacionRepository.findById(request.tipoHabitacionId())
                .orElseThrow(() -> ApiException.noEncontrado(
                        "No se encontró el tipo de habitación con id " + request.tipoHabitacionId() + "."));

        habitacion.setSucursal(sucursal);
        habitacion.setTipoHabitacion(tipo);
        habitacion.setNumeroHabitacion(numero);
        if (request.estado() != null) {
            habitacion.setEstado(request.estado());
        }
    }

    private static HabitacionResponse toResponse(Habitacion habitacion) {
        return new HabitacionResponse(
                habitacion.getId(),
                habitacion.getSucursal().getId(),
                habitacion.getSucursal().getNombreSucursal(),
                habitacion.getTipoHabitacion().getId(),
                habitacion.getTipoHabitacion().getNombre(),
                habitacion.getNumeroHabitacion(),
                habitacion.getEstado());
    }
}