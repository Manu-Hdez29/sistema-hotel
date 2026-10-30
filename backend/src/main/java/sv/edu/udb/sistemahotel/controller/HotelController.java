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
import sv.edu.udb.sistemahotel.dto.HotelRequest;
import sv.edu.udb.sistemahotel.dto.HotelResponse;
import sv.edu.udb.sistemahotel.service.HotelService;

import java.util.List;

@RestController
@RequestMapping("/api/hoteles")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    // Lectura. cualquier usuario autenticado
    @GetMapping
    public List<HotelResponse> listar() {
        return hotelService.listar();
    }

    @GetMapping("/{id}")
    public HotelResponse obtener(@PathVariable Integer id) {
        return hotelService.obtener(id);
    }

    // Escritura. solo para ADMIN
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public HotelResponse crear(@Valid @RequestBody HotelRequest request) {
        return hotelService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public HotelResponse actualizar(@PathVariable Integer id, @Valid @RequestBody HotelRequest request) {
        return hotelService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Integer id) {
        hotelService.eliminar(id);
    }
}