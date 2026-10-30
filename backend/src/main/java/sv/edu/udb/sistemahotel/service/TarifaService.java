package sv.edu.udb.sistemahotel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.sistemahotel.dto.TarifaRequest;
import sv.edu.udb.sistemahotel.dto.TarifaResponse;
import sv.edu.udb.sistemahotel.exception.ApiException;
import sv.edu.udb.sistemahotel.model.Sucursal;
import sv.edu.udb.sistemahotel.model.Tarifa;
import sv.edu.udb.sistemahotel.model.TipoHabitacion;
import sv.edu.udb.sistemahotel.repository.SucursalRepository;
import sv.edu.udb.sistemahotel.repository.TarifaRepository;
import sv.edu.udb.sistemahotel.repository.TipoHabitacionRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class TarifaService {

    private final TarifaRepository tarifaRepository;
    private final SucursalRepository sucursalRepository;
    private final TipoHabitacionRepository tipoHabitacionRepository;

    public TarifaService(TarifaRepository tarifaRepository,
                         SucursalRepository sucursalRepository,
                         TipoHabitacionRepository tipoHabitacionRepository) {
        this.tarifaRepository = tarifaRepository;
        this.sucursalRepository = sucursalRepository;
        this.tipoHabitacionRepository = tipoHabitacionRepository;
    }

    @Transactional(readOnly = true)
    public List<TarifaResponse> listar(Integer sucursalId, Integer tipoHabitacionId) {
        List<Tarifa> tarifas;
        if (sucursalId != null && tipoHabitacionId != null) {
            tarifas = tarifaRepository.findBySucursalIdAndTipoHabitacionId(sucursalId, tipoHabitacionId);
        } else if (sucursalId != null) {
            tarifas = tarifaRepository.findBySucursalId(sucursalId);
        } else if (tipoHabitacionId != null) {
            tarifas = tarifaRepository.findByTipoHabitacionId(tipoHabitacionId);
        } else {
            tarifas = tarifaRepository.findAll();
        }
        return tarifas.stream().map(TarifaService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TarifaResponse obtener(Integer id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public TarifaResponse crear(TarifaRequest request) {
        Tarifa tarifa = new Tarifa();
        aplicar(tarifa, request);
        tarifaRepository.save(tarifa);
        return toResponse(tarifa);
    }

    @Transactional
    public TarifaResponse actualizar(Integer id, TarifaRequest request) {
        Tarifa tarifa = buscar(id);
        aplicar(tarifa, request);
        return toResponse(tarifa);
    }

    @Transactional
    public void eliminar(Integer id) {
        tarifaRepository.delete(buscar(id));
    }

    private Tarifa buscar(Integer id) {
        return tarifaRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No se encontró la tarifa con id " + id + "."));
    }

    private void aplicar(Tarifa tarifa, TarifaRequest request) {
        if (request.fechaFinVigencia() != null
                && request.fechaFinVigencia().isBefore(request.fechaInicioVigencia())) {
            throw ApiException.solicitudInvalida(
                    "La fecha fin de vigencia no puede ser anterior a la fecha de inicio.");
        }

        Sucursal sucursal = sucursalRepository.findById(request.sucursalId())
                .orElseThrow(() -> ApiException.noEncontrado(
                        "No se encontró la sucursal con id " + request.sucursalId() + "."));

        TipoHabitacion tipo = tipoHabitacionRepository.findById(request.tipoHabitacionId())
                .orElseThrow(() -> ApiException.noEncontrado(
                        "No se encontró el tipo de habitación con id " + request.tipoHabitacionId() + "."));

        // Antes de guardar, reviso que las fechas no se crucen con otra tarifa del mismo tipo y sucursal
        validarSinTraslape(request, tarifa.getId());

        tarifa.setSucursal(sucursal);
        tarifa.setTipoHabitacion(tipo);
        tarifa.setPrecioNoche(request.precioNoche());
        tarifa.setFechaInicioVigencia(request.fechaInicioVigencia());
        tarifa.setFechaFinVigencia(request.fechaFinVigencia());
    }

    // Una fecha fin nula significa que la tarifa sigue vigente sin límite
    private void validarSinTraslape(TarifaRequest request, Integer idActual) {
        LocalDate inicio = request.fechaInicioVigencia();
        LocalDate fin = request.fechaFinVigencia();

        for (Tarifa otra : tarifaRepository.findBySucursalIdAndTipoHabitacionId(
                request.sucursalId(), request.tipoHabitacionId())) {
            if (idActual != null && idActual.equals(otra.getId())) {
                continue;
            }
            boolean empiezaAntesDelFinDeOtra = otra.getFechaFinVigencia() == null
                    || !inicio.isAfter(otra.getFechaFinVigencia());
            boolean terminaDespuesDelInicioDeOtra = fin == null
                    || !fin.isBefore(otra.getFechaInicioVigencia());

            if (empiezaAntesDelFinDeOtra && terminaDespuesDelInicioDeOtra) {
                throw ApiException.conflicto(
                        "Las fechas se cruzan con otra tarifa de este tipo en la misma sucursal.");
            }
        }
    }

    private static TarifaResponse toResponse(Tarifa tarifa) {
        return new TarifaResponse(
                tarifa.getId(),
                tarifa.getSucursal().getId(),
                tarifa.getTipoHabitacion().getId(),
                tarifa.getTipoHabitacion().getNombre(),
                tarifa.getPrecioNoche(),
                tarifa.getFechaInicioVigencia(),
                tarifa.getFechaFinVigencia());
    }
}