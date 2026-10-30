package sv.edu.udb.sistemahotel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.dto.SucursalRequest;
import sv.edu.udb.sistemahotel.dto.SucursalResponse;
import sv.edu.udb.sistemahotel.exception.ApiException;
import sv.edu.udb.sistemahotel.model.Hotel;
import sv.edu.udb.sistemahotel.model.Sucursal;
import sv.edu.udb.sistemahotel.repository.HabitacionRepository;
import sv.edu.udb.sistemahotel.repository.HotelRepository;
import sv.edu.udb.sistemahotel.repository.SucursalRepository;
import sv.edu.udb.sistemahotel.repository.TarifaRepository;

import java.util.List;

@Service
public class SucursalService {

    private final SucursalRepository sucursalRepository;
    private final HotelRepository hotelRepository;
    private final HabitacionRepository habitacionRepository;
    private final TarifaRepository tarifaRepository;

    public SucursalService(SucursalRepository sucursalRepository,
                           HotelRepository hotelRepository,
                           HabitacionRepository habitacionRepository,
                           TarifaRepository tarifaRepository) {
        this.sucursalRepository = sucursalRepository;
        this.hotelRepository = hotelRepository;
        this.habitacionRepository = habitacionRepository;
        this.tarifaRepository = tarifaRepository;
    }

    @Transactional(readOnly = true)
    public List<SucursalResponse> listar(Integer hotelId) {
        List<Sucursal> sucursales = hotelId == null
                ? sucursalRepository.findAll()
                : sucursalRepository.findByHotelId(hotelId);
        return sucursales.stream().map(SucursalService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SucursalResponse obtener(Integer id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public SucursalResponse crear(SucursalRequest request) {
        Sucursal sucursal = new Sucursal();
        aplicar(sucursal, request);
        sucursalRepository.save(sucursal);
        return toResponse(sucursal);
    }

    @Transactional
    public SucursalResponse actualizar(Integer id, SucursalRequest request) {
        Sucursal sucursal = buscar(id);
        aplicar(sucursal, request);
        return toResponse(sucursal);
    }

    @Transactional
    public void eliminar(Integer id) {
        Sucursal sucursal = buscar(id);

        if (habitacionRepository.existsBySucursalId(id) || tarifaRepository.existsBySucursalId(id)) {
            throw ApiException.conflicto(
                    "No se puede eliminar la sucursal porque tiene habitaciones o tarifas asociadas.");
        }

        sucursalRepository.delete(sucursal);
    }

    private Sucursal buscar(Integer id) {
        return sucursalRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró la sucursal con id " + id + "."));
    }

    private void aplicar(Sucursal sucursal, SucursalRequest request) {
        Hotel hotel = hotelRepository.findById(request.hotelId())
                .orElseThrow(() -> ApiException.noEncontrado(
                        "No se encontró el hotel con id " + request.hotelId() + "."));

        sucursal.setHotel(hotel);
        sucursal.setNombreSucursal(request.nombre().trim());
        sucursal.setCiudad(request.ciudad().trim());
        sucursal.setDireccion(request.direccion().trim());
        sucursal.setTelefono(request.telefono() == null || request.telefono().isBlank()
                ? null
                : request.telefono().trim());
    }

    private static SucursalResponse toResponse(Sucursal sucursal) {
        return new SucursalResponse(
                sucursal.getId(),
                sucursal.getHotel().getId(),
                sucursal.getHotel().getNombre(),
                sucursal.getNombreSucursal(),
                sucursal.getCiudad(),
                sucursal.getDireccion(),
                sucursal.getTelefono());
    }
}