package sv.edu.udb.sistemahotel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.dto.HotelRequest;
import sv.edu.udb.sistemahotel.dto.HotelResponse;
import sv.edu.udb.sistemahotel.exception.ApiException;
import sv.edu.udb.sistemahotel.model.Hotel;
import sv.edu.udb.sistemahotel.model.enums.EstadoGeneral;
import sv.edu.udb.sistemahotel.repository.HotelRepository;
import sv.edu.udb.sistemahotel.repository.SucursalRepository;

import java.util.List;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private final SucursalRepository sucursalRepository;

    public HotelService(HotelRepository hotelRepository, SucursalRepository sucursalRepository) {
        this.hotelRepository = hotelRepository;
        this.sucursalRepository = sucursalRepository;
    }

    @Transactional(readOnly = true)
    public List<HotelResponse> listar() {
        return hotelRepository.findAll().stream()
                .map(HotelService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HotelResponse obtener(Integer id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public HotelResponse crear(HotelRequest request) {
        String nit = normalizarNit(request.nitRuc());
        if (nit != null && hotelRepository.existsByNitRuc(nit)) {
            throw ApiException.conflicto("Ya existe un hotel con ese NIT/RUC.");
        }

        Hotel hotel = new Hotel();
        hotel.setEstado(EstadoGeneral.ACTIVO);
        aplicar(hotel, request, nit);
        hotelRepository.save(hotel);
        return toResponse(hotel);
    }

    @Transactional
    public HotelResponse actualizar(Integer id, HotelRequest request) {
        Hotel hotel = buscar(id);

        String nit = normalizarNit(request.nitRuc());
        if (nit != null && hotelRepository.existsByNitRucAndIdNot(nit, id)) {
            throw ApiException.conflicto("Ya existe otro hotel con ese NIT/RUC.");
        }

        aplicar(hotel, request, nit);
        return toResponse(hotel);
    }

    @Transactional
    public void eliminar(Integer id) {
        Hotel hotel = buscar(id);

        if (sucursalRepository.existsByHotelId(id)) {
            throw ApiException.conflicto(
                    "No se puede eliminar el hotel porque tiene sucursales. Elimínelas primero.");
        }

        hotelRepository.delete(hotel);
    }

    private Hotel buscar(Integer id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el hotel con id " + id + "."));
    }

    private static void aplicar(Hotel hotel, HotelRequest request, String nit) {
        hotel.setNombre(request.nombre().trim());
        hotel.setNitRuc(nit);
        if (request.estado() != null) {
            hotel.setEstado(request.estado());
        }
    }

    private static String normalizarNit(String nit) {
        if (nit == null || nit.isBlank()) {
            return null;
        }
        return nit.trim();
    }

    private static HotelResponse toResponse(Hotel hotel) {
        return new HotelResponse(
                hotel.getId(),
                hotel.getNombre(),
                hotel.getNitRuc(),
                hotel.getEstado());
    }
}