package sv.edu.udb.sistemahotel.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TarifaRequest(
        @NotNull(message = "La sucursal es obligatoria.")
        Integer sucursalId,

        @NotNull(message = "El tipo de habitación es obligatorio.")
        Integer tipoHabitacionId,

        @NotNull(message = "El precio por noche es obligatorio.")
        @DecimalMin(value = "0.01", message = "El precio por noche debe ser mayor a 0.")
        @Digits(integer = 8, fraction = 2, message = "El precio solo puede tener 2 decimales.")
        BigDecimal precioNoche,

        @NotNull(message = "La fecha de inicio de vigencia es obligatoria.")
        LocalDate fechaInicioVigencia,

        // Opcional: si viene, debe ser igual o posterior a la fecha de inicio
        LocalDate fechaFinVigencia
) {
}