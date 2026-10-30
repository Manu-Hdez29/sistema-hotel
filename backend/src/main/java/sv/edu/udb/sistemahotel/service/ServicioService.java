package sv.edu.udb.sistemahotel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.dto.ServicioRequest;
import sv.edu.udb.sistemahotel.dto.ServicioResponse;
import sv.edu.udb.sistemahotel.exception.ApiException;
import sv.edu.udb.sistemahotel.model.Servicio;
import sv.edu.udb.sistemahotel.repository.ServicioRepository;

import java.util.List;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @Transactional(readOnly = true)
    public List<ServicioResponse> listar() {
        return servicioRepository.findAll().stream()
                .map(ServicioService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServicioResponse obtener(Integer id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public ServicioResponse crear(ServicioRequest request) {
        Servicio servicio = new Servicio();
        aplicar(servicio, request);
        servicioRepository.save(servicio);
        return toResponse(servicio);
    }

    @Transactional
    public ServicioResponse actualizar(Integer id, ServicioRequest request) {
        Servicio servicio = buscar(id);
        aplicar(servicio, request);
        return toResponse(servicio);
    }

    @Transactional
    public void eliminar(Integer id) {
        // Si el servicio ya se usó en una reserva, la base de datos lo impedirá
        // y GlobalExceptionHandler responderá 409
        servicioRepository.delete(buscar(id));
    }

    private Servicio buscar(Integer id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el servicio con id " + id + "."));
    }

    private static void aplicar(Servicio servicio, ServicioRequest request) {
        servicio.setNombre(request.nombre().trim());
        servicio.setPrecio(request.precio());
        servicio.setTipoServicio(request.tipoServicio());
    }

    private static ServicioResponse toResponse(Servicio servicio) {
        return new ServicioResponse(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getPrecio(),
                servicio.getTipoServicio());
    }
}