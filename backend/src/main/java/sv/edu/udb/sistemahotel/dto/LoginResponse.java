package sv.edu.udb.sistemahotel.dto;

import java.time.Instant;

public record LoginResponse(
        String token,
        String tipo,
        Instant expiracion,
        UsuarioResumen usuario
) {
}