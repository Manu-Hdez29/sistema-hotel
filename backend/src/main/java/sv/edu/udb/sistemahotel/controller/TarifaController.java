package sv.edu.udb.sistemahotel.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.sistemahotel.dto.TarifaRequest;
import sv.edu.udb.sistemahotel.dto.TarifaResponse;
import sv.edu.udb.sistemahotel.service.TarifaService;

import java.util.List;

@RestController
@RequestMapping("/api/tarifas")
public class TarifaController {

    private final TarifaService tarifaService;

    public TarifaController(TarifaService tarifaService) {
        this.tarifaService = tarifaService;
    }

    @GetMapping
    public List<TarifaResponse> listar(@RequestParam(required = false) Integer sucursalId,
                                       @RequestParam(required = false) Integer tipoHabitacionId) {
        return tarifaService.listar(sucursalId, tipoHabitacionId);
    }

    @GetMapping("/{id}")
    public TarifaResponse obtener(@PathVariable Integer id) {
        return tarifaService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TarifaResponse crear(@Valid @RequestBody TarifaRequest request) {
        return tarifaService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TarifaResponse actualizar(@PathVariable Integer id, @Valid @RequestBody TarifaRequest request) {
        return tarifaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Integer id) {
        tarifaService.eliminar(id);
    }
}