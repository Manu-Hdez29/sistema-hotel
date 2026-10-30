package sv.edu.udb.sistemahotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sv.edu.udb.sistemahotel.model.enums.EstadoHabitacion;

public record HabitacionRequest(
        @NotNull(message = "La sucursal es obligatoria.")
        Integer sucursalId,

        @NotNull(message = "El tipo de habitación es obligatorio.")
        Integer tipoHabitacionId,

        @NotBlank(message = "El número de habitación es obligatorio.")
        @Size(max = 10, message = "El número no puede pasar de 10 caracteres.")
        String numeroHabitacion,

        // Opcional: si no viene, al crear queda DISPONIBLE y al editar no cambia
        EstadoHabitacion estado
) {
}