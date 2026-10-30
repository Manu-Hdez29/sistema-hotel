package sv.edu.udb.sistemahotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReclamoCrearRequest(
        @NotNull(message = "El huésped es obligatorio.")
        Integer huespedId,

        // Opcional: la reserva relacionada con el reclamo
        Integer reservaId,

        @NotBlank(message = "La descripción es obligatoria.")
        @Size(max = 250, message = "La descripción no puede pasar de 250 caracteres.")
        String descripcion
) {
}