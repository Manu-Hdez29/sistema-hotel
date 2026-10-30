package sv.edu.udb.sistemahotel.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.sistemahotel.dto.DisponibilidadResponse;
import sv.edu.udb.sistemahotel.service.DisponibilidadService;

import java.time.LocalDate;
import java.util.List;

// Cualquier usuario autenticado puede consultar disponibilidad
@RestController
@RequestMapping("/api/disponibilidad")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    @GetMapping
    public List<DisponibilidadResponse> buscar(@RequestParam LocalDate fechaEntrada,
                                               @RequestParam LocalDate fechaSalida,
                                               @RequestParam Integer huespedes,
                                               @RequestParam(required = false) String ciudad,
                                               @RequestParam(required = false) Integer tipoHabitacionId) {
        return disponibilidadService.buscar(fechaEntrada, fechaSalida, huespedes, ciudad, tipoHabitacionId);
    }
}