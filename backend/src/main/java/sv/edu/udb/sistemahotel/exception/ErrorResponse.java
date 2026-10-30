package sv.edu.udb.sistemahotel.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

// Es el formato único de error
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        List<String> detalles
) {

    public static ErrorResponse of(HttpStatus status, String mensaje, List<String> detalles) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), etiqueta(status), mensaje, detalles);
    }

    private static String etiqueta(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Solicitud inválida";
            case UNAUTHORIZED -> "No autorizado";
            case FORBIDDEN -> "Acceso denegado";
            case NOT_FOUND -> "No encontrado";
            case CONFLICT -> "Conflicto";
            default -> "Error interno";
        };
    }
}