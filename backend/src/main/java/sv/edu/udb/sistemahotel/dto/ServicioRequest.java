package sv.edu.udb.sistemahotel.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sv.edu.udb.sistemahotel.model.enums.TipoServicio;

import java.math.BigDecimal;

public record ServicioRequest(
        @NotBlank(message = "El nombre del servicio es obligatorio.")
        @Size(max = 60, message = "El nombre no puede pasar de 60 caracteres.")
        String nombre,

        @NotNull(message = "El precio es obligatorio.")
        @DecimalMin(value = "0.00", message = "El precio no puede ser negativo.")
        @Digits(integer = 6, fraction = 2, message = "El precio solo puede tener 2 decimales.")
        BigDecimal precio,

        @NotNull(message = "El tipo de servicio es obligatorio.")
        TipoServicio tipoServicio
) {
}