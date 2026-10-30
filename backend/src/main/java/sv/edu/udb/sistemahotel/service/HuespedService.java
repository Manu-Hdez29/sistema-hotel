package sv.edu.udb.sistemahotel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.dto.HuespedRequest;
import sv.edu.udb.sistemahotel.dto.HuespedResponse;
import sv.edu.udb.sistemahotel.exception.ApiException;
import sv.edu.udb.sistemahotel.model.Huesped;
import sv.edu.udb.sistemahotel.model.Usuario;
import sv.edu.udb.sistemahotel.model.enums.EstadoGeneral;
import sv.edu.udb.sistemahotel.repository.HuespedRepository;
import sv.edu.udb.sistemahotel.repository.ReclamoRepository;
import sv.edu.udb.sistemahotel.repository.ReservaHuespedRepository;

import java.util.List;

@Service
public class HuespedService {

    private final HuespedRepository huespedRepository;
    private final ReservaHuespedRepository reservaHuespedRepository;
    private final ReclamoRepository reclamoRepository;

    public HuespedService(HuespedRepository huespedRepository,
                          ReservaHuespedRepository reservaHuespedRepository,
                          ReclamoRepository reclamoRepository) {
        this.huespedRepository = huespedRepository;
        this.reservaHuespedRepository = reservaHuespedRepository;
        this.reclamoRepository = reclamoRepository;
    }

    @Transactional(readOnly = true)
    public List<HuespedResponse> listar() {
        return huespedRepository.findAll().stream()
                .map(HuespedService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HuespedResponse obtener(Integer id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public HuespedResponse crear(HuespedRequest request) {
        String documento = request.documentoIdentidad().trim();
        if (huespedRepository.existsByDocumentoIdentidad(documento)) {
            throw ApiException.conflicto("Ya existe un huésped registrado con ese documento.");
        }

        Huesped huesped = new Huesped();
        aplicar(huesped, request, documento);
        huespedRepository.save(huesped);
        return toResponse(huesped);
    }

    @Transactional
    public HuespedResponse actualizar(Integer id, HuespedRequest request) {
        Huesped huesped = buscar(id);

        String documento = request.documentoIdentidad().trim();
        if (huespedRepository.existsByDocumentoIdentidadAndIdNot(documento, id)) {
            throw ApiException.conflicto("Ya existe otro huésped con ese documento.");
        }

        aplicar(huesped, request, documento);
        return toResponse(huesped);
    }

    // Borra el perfil de huésped. Si tiene cuenta de acceso, la cuenta NO se borra:
    // se desactiva, para conservar el historial y que no quede una cuenta activa sin perfil.
    @Transactional
    public void eliminar(Integer id) {
        Huesped huesped = buscar(id);

        if (reservaHuespedRepository.existsByHuespedId(id) || reclamoRepository.existsByHuespedId(id)) {
            throw ApiException.conflicto(
                    "No se puede eliminar el huésped porque tiene reservas o reclamos asociados.");
        }

        Usuario cuenta = huesped.getUsuario();
        if (cuenta != null) {
            cuenta.setEstado(EstadoGeneral.INACTIVO);
        }

        huespedRepository.delete(huesped);
    }

    // Perfil de huésped de una cuenta. Si la cuenta no tiene perfil de huésped, responde 404.
    @Transactional(readOnly = true)
    public HuespedResponse perfilDeUsuario(Integer usuarioId) {
        return huespedRepository.findByUsuarioId(usuarioId)
                .map(HuespedService::toResponse)
                .orElseThrow(() -> ApiException.noEncontrado("Esta cuenta no tiene perfil de huésped."));
    }

    private Huesped buscar(Integer id) {
        return huespedRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró el huésped con id " + id + "."));
    }

    private static void aplicar(Huesped huesped, HuespedRequest request, String documento) {
        huesped.setNombreCompleto(request.nombreCompleto().trim());
        huesped.setFechaNacimiento(request.fechaNacimiento());
        huesped.setDocumentoIdentidad(documento);
        huesped.setTelefono(request.telefono().trim());
        huesped.setEmail(request.email().trim().toLowerCase());
    }

    private static HuespedResponse toResponse(Huesped huesped) {
        return new HuespedResponse(
                huesped.getId(),
                huesped.getNombreCompleto(),
                huesped.getFechaNacimiento(),
                huesped.getDocumentoIdentidad(),
                huesped.getTelefono(),
                huesped.getEmail(),
                huesped.getUsuario() != null ? huesped.getUsuario().getId() : null);
    }
}