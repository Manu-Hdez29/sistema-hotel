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
import sv.edu.udb.sistemahotel.dto.PoliticaCancelacionRequest;
import sv.edu.udb.sistemahotel.dto.PoliticaCancelacionResponse;
import sv.edu.udb.sistemahotel.service.PoliticaCancelacionService;

import java.util.List;

@RestController
@RequestMapping("/api/politicas-cancelacion")
public class PoliticaCancelacionController {

    private final PoliticaCancelacionService politicaService;

    public PoliticaCancelacionController(PoliticaCancelacionService politicaService) {
        this.politicaService = politicaService;
    }

    @GetMapping
    public List<PoliticaCancelacionResponse> listar(@RequestParam(required = false) Integer hotelId) {
        return politicaService.listar(hotelId);
    }

    @GetMapping("/{id}")
    public PoliticaCancelacionResponse obtener(@PathVariable Integer id) {
        return politicaService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public PoliticaCancelacionResponse crear(@Valid @RequestBody PoliticaCancelacionRequest request) {
        return politicaService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PoliticaCancelacionResponse actualizar(@PathVariable Integer id,
                                                  @Valid @RequestBody PoliticaCancelacionRequest request) {
        return politicaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Integer id) {
        politicaService.eliminar(id);
    }
}