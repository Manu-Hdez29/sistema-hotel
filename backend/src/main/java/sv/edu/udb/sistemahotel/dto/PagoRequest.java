package sv.edu.udb.sistemahotel.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import sv.edu.udb.sistemahotel.model.enums.MetodoPago;

import java.math.BigDecimal;

public record PagoRequest(
        @NotNull(message = "El método de pago es obligatorio.")
        MetodoPago metodo,

        @NotNull(message = "El monto es obligatorio.")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0.")
        @Digits(integer = 8, fraction = 2, message = "El monto solo puede tener 2 decimales.")
        BigDecimal monto
) {
}