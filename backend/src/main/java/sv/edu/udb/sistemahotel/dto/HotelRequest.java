package sv.edu.udb.sistemahotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import sv.edu.udb.sistemahotel.model.enums.EstadoGeneral;

public record HotelRequest(
        @NotBlank(message = "El nombre del hotel es obligatorio.")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres.")
        String nombre,

        @Size(max = 20, message = "El NIT/RUC no puede pasar de 20 caracteres.")
        String nitRuc,

        // Si no viene, al crear queda ACTIVO y al editar no cambia
        EstadoGeneral estado
) {
}