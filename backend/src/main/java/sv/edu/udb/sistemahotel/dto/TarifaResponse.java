package sv.edu.udb.sistemahotel.dto;

import sv.edu.udb.sistemahotel.model.enums.NombreTipoHabitacion;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TarifaResponse(
        Integer id,
        Integer sucursalId,
        Integer tipoHabitacionId,
        NombreTipoHabitacion tipoHabitacion,
        BigDecimal precioNoche,
        LocalDate fechaInicioVigencia,
        LocalDate fechaFinVigencia
) {
}