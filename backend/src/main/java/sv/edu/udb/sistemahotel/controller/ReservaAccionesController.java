package sv.edu.udb.sistemahotel.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.sistemahotel.dto.CancelacionResponse;
import sv.edu.udb.sistemahotel.dto.CancelarRequest;
import sv.edu.udb.sistemahotel.dto.FechasRequest;
import sv.edu.udb.sistemahotel.dto.ReservaResponse;
import sv.edu.udb.sistemahotel.service.ReservaEstadoService;

@RestController
@RequestMapping("/api/reservas")
public class ReservaAccionesController {

    private final ReservaEstadoService reservaEstadoService;

    public ReservaAccionesController(ReservaEstadoService reservaEstadoService) {
        this.reservaEstadoService = reservaEstadoService;
    }

    @PostMapping("/{id}/cancelar")
    public CancelacionResponse cancelar(@PathVariable Integer id,
                                        @Valid @RequestBody CancelarRequest request,
                                        @AuthenticationPrincipal UserDetails actual) {
        return reservaEstadoService.cancelar(id, request, actual);
    }

    @PutMapping("/{id}/fechas")
    public ReservaResponse cambiarFechas(@PathVariable Integer id,
                                         @Valid @RequestBody FechasRequest request,
                                         @AuthenticationPrincipal UserDetails actual) {
        return reservaEstadoService.cambiarFechas(id, request, actual);
    }

    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA','ADMIN')")
    public ReservaResponse finalizar(@PathVariable Integer id,
                                     @AuthenticationPrincipal UserDetails actual) {
        return reservaEstadoService.finalizar(id, actual);
    }
}