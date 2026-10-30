package sv.edu.udb.sistemahotel.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.sistemahotel.dto.PagoRequest;
import sv.edu.udb.sistemahotel.dto.ReservaResponse;
import sv.edu.udb.sistemahotel.service.PagoService;

@RestController
@RequestMapping("/api/reservas/{id}/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse registrar(@PathVariable Integer id,
                                     @Valid @RequestBody PagoRequest request,
                                     @AuthenticationPrincipal UserDetails actual) {
        return pagoService.registrarPago(id, request, actual);
    }
}