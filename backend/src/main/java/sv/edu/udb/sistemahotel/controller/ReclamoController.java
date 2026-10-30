package sv.edu.udb.sistemahotel.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.sistemahotel.dto.ReclamoAtenderRequest;
import sv.edu.udb.sistemahotel.dto.ReclamoCrearRequest;
import sv.edu.udb.sistemahotel.dto.ReclamoResponse;
import sv.edu.udb.sistemahotel.model.enums.EstadoReclamo;
import sv.edu.udb.sistemahotel.service.ReclamoService;

import java.util.List;

@RestController
@RequestMapping("/api/reclamos")
public class ReclamoController {

    private final ReclamoService reclamoService;

    public ReclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReclamoResponse crear(@Valid @RequestBody ReclamoCrearRequest request,
                                 @AuthenticationPrincipal UserDetails actual) {
        return reclamoService.crear(request, actual);
    }

    @GetMapping
    public List<ReclamoResponse> listar(@RequestParam(required = false) EstadoReclamo estado,
                                        @AuthenticationPrincipal UserDetails actual) {
        return reclamoService.listar(estado, actual);
    }

    @PatchMapping("/{id}/atender")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA','ADMIN')")
    public ReclamoResponse atender(@PathVariable Integer id,
                                   @Valid @RequestBody ReclamoAtenderRequest request,
                                   @AuthenticationPrincipal UserDetails actual) {
        return reclamoService.atender(id, request, actual);
    }
}