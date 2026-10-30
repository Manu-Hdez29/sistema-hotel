package sv.edu.udb.sistemahotel.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.sistemahotel.dto.LoginRequest;
import sv.edu.udb.sistemahotel.dto.LoginResponse;
import sv.edu.udb.sistemahotel.dto.MensajeResponse;
import sv.edu.udb.sistemahotel.dto.RegistroRequest;
import sv.edu.udb.sistemahotel.dto.UsuarioResumen;
import sv.edu.udb.sistemahotel.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public MensajeResponse registro(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    // Requiere token: devuelve los datos del usuario que hizo la petición
    @GetMapping("/me")
    public UsuarioResumen me(@AuthenticationPrincipal UserDetails usuario) {
        return authService.obtenerUsuario(usuario.getUsername());
    }
}
