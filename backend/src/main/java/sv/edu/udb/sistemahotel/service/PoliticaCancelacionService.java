package sv.edu.udb.sistemahotel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.dto.PoliticaCancelacionRequest;
import sv.edu.udb.sistemahotel.dto.PoliticaCancelacionResponse;
import sv.edu.udb.sistemahotel.exception.ApiException;
import sv.edu.udb.sistemahotel.model.Hotel;
import sv.edu.udb.sistemahotel.model.PoliticaCancelacion;
import sv.edu.udb.sistemahotel.repository.HotelRepository;
import sv.edu.udb.sistemahotel.repository.PoliticaCancelacionRepository;

import java.util.List;

@Service
public class PoliticaCancelacionService {

    private final PoliticaCancelacionRepository politicaRepository;
    private final HotelRepository hotelRepository;

    public PoliticaCancelacionService(PoliticaCancelacionRepository politicaRepository,
                                      HotelRepository hotelRepository) {
        this.politicaRepository = politicaRepository;
        this.hotelRepository = hotelRepository;
    }

    @Transactional(readOnly = true)
    public List<PoliticaCancelacionResponse> listar(Integer hotelId) {
        List<PoliticaCancelacion> politicas = hotelId == null
                ? politicaRepository.findAll()
                : politicaRepository.findByHotelId(hotelId);
        return politicas.stream().map(PoliticaCancelacionService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PoliticaCancelacionResponse obtener(Integer id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public PoliticaCancelacionResponse crear(PoliticaCancelacionRequest request) {
        PoliticaCancelacion politica = new PoliticaCancelacion();
        aplicar(politica, request);
        politicaRepository.save(politica);
        return toResponse(politica);
    }

    @Transactional
    public PoliticaCancelacionResponse actualizar(Integer id, PoliticaCancelacionRequest request) {
        PoliticaCancelacion politica = buscar(id);
        aplicar(politica, request);
        return toResponse(politica);
    }

    @Transactional
    public void eliminar(Integer id) {
        politicaRepository.delete(buscar(id));
    }

    private PoliticaCancelacion buscar(Integer id) {
        return politicaRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró la política con id " + id + "."));
    }

    private void aplicar(PoliticaCancelacion politica, PoliticaCancelacionRequest request) {
        Hotel hotel = hotelRepository.findById(request.hotelId())
                .orElseThrow(() -> ApiException.noEncontrado(
                        "No se encontró el hotel con id " + request.hotelId() + "."));

        politica.setHotel(hotel);
        politica.setDiasPrevios(request.diasPrevios());
        politica.setPorcentajeCargo(request.porcentajeCargo());
        politica.setDescripcion(request.descripcion().trim());
    }

    private static PoliticaCancelacionResponse toResponse(PoliticaCancelacion politica) {
        return new PoliticaCancelacionResponse(
                politica.getId(),
                politica.getHotel().getId(),
                politica.getHotel().getNombre(),
                politica.getDiasPrevios(),
                politica.getPorcentajeCargo(),
                politica.getDescripcion());
    }
}