package sv.edu.udb.sistemahotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelarRequest(
        @NotBlank(message = "El motivo de la cancelación es obligatorio.")
        @Size(max = 200, message = "El motivo no puede pasar de 200 caracteres.")
        String motivo
) {
}