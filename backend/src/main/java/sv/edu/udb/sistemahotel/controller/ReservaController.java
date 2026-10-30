package sv.edu.udb.sistemahotel.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.sistemahotel.dto.ReservaCrearRequest;
import sv.edu.udb.sistemahotel.dto.ReservaResponse;
import sv.edu.udb.sistemahotel.model.enums.EstadoReserva;
import sv.edu.udb.sistemahotel.service.ReservaService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public List<ReservaResponse> listar(@RequestParam(required = false) EstadoReserva estado,
                                        @RequestParam(required = false) LocalDate desde,
                                        @RequestParam(required = false) LocalDate hasta,
                                        @AuthenticationPrincipal UserDetails actual) {
        return reservaService.listar(estado, desde, hasta, actual);
    }

    @GetMapping("/{id}")
    public ReservaResponse obtener(@PathVariable Integer id, @AuthenticationPrincipal UserDetails actual) {
        return reservaService.obtener(id, actual);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse crear(@Valid @RequestBody ReservaCrearRequest request,
                                 @AuthenticationPrincipal UserDetails actual) {
        return reservaService.crear(request, actual);
    }
}