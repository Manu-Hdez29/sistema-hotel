package sv.edu.udb.sistemahotel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sv.edu.udb.sistemahotel.model.enums.NombreTipoHabitacion;

public record TipoHabitacionRequest(
        @NotNull(message = "El nombre del tipo es obligatorio.")
        NombreTipoHabitacion nombre,

        @NotNull(message = "La capacidad máxima es obligatoria.")
        @Min(value = 1, message = "La capacidad mínima es 1.")
        @Max(value = 10, message = "La capacidad máxima permitida es 10.")
        Integer capacidadMaxima,

        @NotBlank(message = "La descripción es obligatoria.")
        @Size(max = 200, message = "La descripción no puede pasar de 200 caracteres.")
        String descripcion
) {
}