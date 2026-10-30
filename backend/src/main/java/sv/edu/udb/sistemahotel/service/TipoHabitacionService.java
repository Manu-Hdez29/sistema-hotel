package sv.edu.udb.sistemahotel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.dto.TipoHabitacionRequest;
import sv.edu.udb.sistemahotel.dto.TipoHabitacionResponse;
import sv.edu.udb.sistemahotel.exception.ApiException;
import sv.edu.udb.sistemahotel.model.TipoHabitacion;
import sv.edu.udb.sistemahotel.repository.HabitacionRepository;
import sv.edu.udb.sistemahotel.repository.TarifaRepository;
import sv.edu.udb.sistemahotel.repository.TipoHabitacionRepository;

import java.util.List;

@Service
public class TipoHabitacionService {

    private final TipoHabitacionRepository tipoHabitacionRepository;
    private final HabitacionRepository habitacionRepository;
    private final TarifaRepository tarifaRepository;

    public TipoHabitacionService(TipoHabitacionRepository tipoHabitacionRepository,
                                 HabitacionRepository habitacionRepository,
                                 TarifaRepository tarifaRepository) {
        this.tipoHabitacionRepository = tipoHabitacionRepository;
        this.habitacionRepository = habitacionRepository;
        this.tarifaRepository = tarifaRepository;
    }

    @Transactional(readOnly = true)
    public List<TipoHabitacionResponse> listar() {
        return tipoHabitacionRepository.findAll().stream()
                .map(TipoHabitacionService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TipoHabitacionResponse obtener(Integer id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public TipoHabitacionResponse crear(TipoHabitacionRequest request) {
        TipoHabitacion tipo = new TipoHabitacion();
        aplicar(tipo, request);
        tipoHabitacionRepository.save(tipo);
        return toResponse(tipo);
    }

    @Transactional
    public TipoHabitacionResponse actualizar(Integer id, TipoHabitacionRequest request) {
        TipoHabitacion tipo = buscar(id);
        aplicar(tipo, request);
        return toResponse(tipo);
    }

    @Transactional
    public void eliminar(Integer id) {
        TipoHabitacion tipo = buscar(id);

        if (habitacionRepository.existsByTipoHabitacionId(id) || tarifaRepository.existsByTipoHabitacionId(id)) {
            throw ApiException.conflicto(
                    "No se puede eliminar el tipo porque tiene habitaciones o tarifas asociadas.");
        }

        tipoHabitacionRepository.delete(tipo);
    }

    private TipoHabitacion buscar(Integer id) {
        return tipoHabitacionRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el tipo de habitación con id " + id + "."));
    }

    private static void aplicar(TipoHabitacion tipo, TipoHabitacionRequest request) {
        tipo.setNombre(request.nombre());
        tipo.setCapacidadMaxima(request.capacidadMaxima());
        tipo.setDescripcion(request.descripcion().trim());
    }

    private static TipoHabitacionResponse toResponse(TipoHabitacion tipo) {
        return new TipoHabitacionResponse(
                tipo.getId(),
                tipo.getNombre(),
                tipo.getCapacidadMaxima(),
                tipo.getDescripcion());
    }
}