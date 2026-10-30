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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.sistemahotel.dto.TipoHabitacionRequest;
import sv.edu.udb.sistemahotel.dto.TipoHabitacionResponse;
import sv.edu.udb.sistemahotel.service.TipoHabitacionService;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-habitacion")
public class TipoHabitacionController {

    private final TipoHabitacionService tipoHabitacionService;

    public TipoHabitacionController(TipoHabitacionService tipoHabitacionService) {
        this.tipoHabitacionService = tipoHabitacionService;
    }

    @GetMapping
    public List<TipoHabitacionResponse> listar() {
        return tipoHabitacionService.listar();
    }

    @GetMapping("/{id}")
    public TipoHabitacionResponse obtener(@PathVariable Integer id) {
        return tipoHabitacionService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TipoHabitacionResponse crear(@Valid @RequestBody TipoHabitacionRequest request) {
        return tipoHabitacionService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TipoHabitacionResponse actualizar(@PathVariable Integer id,
                                             @Valid @RequestBody TipoHabitacionRequest request) {
        return tipoHabitacionService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Integer id) {
        tipoHabitacionService.eliminar(id);
    }
}