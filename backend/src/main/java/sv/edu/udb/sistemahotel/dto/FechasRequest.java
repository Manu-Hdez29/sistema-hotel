package sv.edu.udb.sistemahotel.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FechasRequest(
        @NotNull(message = "La fecha de entrada es obligatoria.")
        LocalDate fechaEntrada,

        @NotNull(message = "La fecha de salida es obligatoria.")
        LocalDate fechaSalida
) {
}