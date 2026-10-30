package sv.edu.udb.sistemahotel.dto;

public record UsuarioResumen(
        Integer id,
        String nombreCompleto,
        String email,
        String rol
) {
}