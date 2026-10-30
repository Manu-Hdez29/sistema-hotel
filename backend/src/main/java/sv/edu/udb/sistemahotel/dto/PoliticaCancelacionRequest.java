package sv.edu.udb.sistemahotel.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PoliticaCancelacionRequest(
        @NotNull(message = "El hotel es obligatorio.")
        Integer hotelId,

        @NotNull(message = "Los días previos son obligatorios.")
        @Min(value = 0, message = "Los días previos no pueden ser negativos.")
        Integer diasPrevios,

        @NotNull(message = "El porcentaje de cargo es obligatorio.")
        @DecimalMin(value = "0", message = "El porcentaje no puede ser menor a 0.")
        @DecimalMax(value = "100", message = "El porcentaje no puede ser mayor a 100.")
        @Digits(integer = 3, fraction = 2, message = "El porcentaje solo puede tener 2 decimales.")
        BigDecimal porcentajeCargo,

        @NotBlank(message = "La descripción es obligatoria.")
        @Size(max = 200, message = "La descripción no puede pasar de 200 caracteres.")
        String descripcion
) {
}