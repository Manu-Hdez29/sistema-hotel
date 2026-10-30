package sv.edu.udb.sistemahotel.dto;

import sv.edu.udb.sistemahotel.model.enums.NombreTipoHabitacion;

public record TipoHabitacionResponse(
        Integer id,
        NombreTipoHabitacion nombre,
        Integer capacidadMaxima,
        String descripcion
) {
}