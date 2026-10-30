package sv.edu.udb.sistemahotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sv.edu.udb.sistemahotel.model.enums.EstadoReclamo;

public record ReclamoAtenderRequest(
        @NotNull(message = "El estado es obligatorio.")
        EstadoReclamo estado,

        @NotBlank(message = "La respuesta es obligatoria.")
        @Size(max = 250, message = "La respuesta no puede pasar de 250 caracteres.")
        String respuesta
) {
}