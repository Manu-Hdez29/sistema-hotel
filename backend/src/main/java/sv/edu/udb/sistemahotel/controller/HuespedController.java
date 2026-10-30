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
import sv.edu.udb.sistemahotel.dto.HuespedRequest;
import sv.edu.udb.sistemahotel.dto.HuespedResponse;
import sv.edu.udb.sistemahotel.service.HuespedService;

import java.util.List;

@RestController
@RequestMapping("/api/huespedes")
@PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
public class HuespedController {

    private final HuespedService huespedService;

    public HuespedController(HuespedService huespedService) {
        this.huespedService = huespedService;
    }

    @GetMapping
    public List<HuespedResponse> listar() {
        return huespedService.listar();
    }

    @GetMapping("/{id}")
    public HuespedResponse obtener(@PathVariable Integer id) {
        return huespedService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HuespedResponse crear(@Valid @RequestBody HuespedRequest request) {
        return huespedService.crear(request);
    }

    @PutMapping("/{id}")
    public HuespedResponse actualizar(@PathVariable Integer id, @Valid @RequestBody HuespedRequest request) {
        return huespedService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        huespedService.eliminar(id);
    }
}