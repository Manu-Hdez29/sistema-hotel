package sv.edu.udb.sistemahotel.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.sistemahotel.dto.HuespedResponse;
import sv.edu.udb.sistemahotel.dto.UsuarioResumen;
import sv.edu.udb.sistemahotel.service.AuthService;
import sv.edu.udb.sistemahotel.service.HuespedService;

// Perfil del propio usuario
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final AuthService authService;
    private final HuespedService huespedService;

    public PerfilController(AuthService authService, HuespedService huespedService) {
        this.authService = authService;
        this.huespedService = huespedService;
    }

    // Devuelve los datos de huésped de la cuenta que hace la petición (404 si no tiene perfil de huésped)
    @GetMapping
    public HuespedResponse miPerfil(@AuthenticationPrincipal UserDetails usuario) {
        UsuarioResumen cuenta = authService.obtenerUsuario(usuario.getUsername());
        return huespedService.perfilDeUsuario(cuenta.id());
    }
}