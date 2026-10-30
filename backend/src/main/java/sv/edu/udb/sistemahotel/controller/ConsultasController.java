package sv.edu.udb.sistemahotel.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.sistemahotel.dto.EstadisticasResponse;
import sv.edu.udb.sistemahotel.dto.PagoListadoResponse;
import sv.edu.udb.sistemahotel.service.ConsultasService;

import java.time.LocalDate;
import java.util.List;

// Listado de pagos y estadísticas
@RestController
public class ConsultasController {

    private final ConsultasService consultasService;

    public ConsultasController(ConsultasService consultasService) {
        this.consultasService = consultasService;
    }

    @GetMapping("/api/pagos")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public List<PagoListadoResponse> listarPagos(@RequestParam(required = false) LocalDate desde,
                                                 @RequestParam(required = false) LocalDate hasta) {
        return consultasService.listarPagos(desde, hasta);
    }

    @GetMapping("/api/estadisticas")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    public EstadisticasResponse estadisticas(@RequestParam LocalDate desde,
                                             @RequestParam LocalDate hasta) {
        return consultasService.estadisticas(desde, hasta);
    }
}