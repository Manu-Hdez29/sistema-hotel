package sv.edu.udb.sistemahotel.dto;

import sv.edu.udb.sistemahotel.model.enums.EstadoHabitacion;
import sv.edu.udb.sistemahotel.model.enums.NombreTipoHabitacion;

public record HabitacionResponse(
        Integer id,
        Integer sucursalId,
        String sucursalNombre,
        Integer tipoHabitacionId,
        NombreTipoHabitacion tipoHabitacion,
        String numeroHabitacion,
        EstadoHabitacion estado
) {
}