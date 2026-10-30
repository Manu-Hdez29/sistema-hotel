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
import sv.edu.udb.sistemahotel.dto.HabitacionRequest;
import sv.edu.udb.sistemahotel.dto.HabitacionResponse;
import sv.edu.udb.sistemahotel.service.HabitacionService;

import java.util.List;

@RestController
@RequestMapping("/api/habitaciones")
public class HabitacionController {

    private final HabitacionService habitacionService;

    public HabitacionController(HabitacionService habitacionService) {
        this.habitacionService = habitacionService;
    }

    @GetMapping
    public List<HabitacionResponse> listar(@RequestParam(required = false) Integer sucursalId) {
        return habitacionService.listar(sucursalId);
    }

    @GetMapping("/{id}")
    public HabitacionResponse obtener(@PathVariable Integer id) {
        return habitacionService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public HabitacionResponse crear(@Valid @RequestBody HabitacionRequest request) {
        return habitacionService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public HabitacionResponse actualizar(@PathVariable Integer id, @Valid @RequestBody HabitacionRequest request) {
        return habitacionService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Integer id) {
        habitacionService.eliminar(id);
    }
}