package sv.edu.udb.sistemahotel.dto;

import sv.edu.udb.sistemahotel.model.enums.NombreTipoHabitacion;

import java.math.BigDecimal;
import java.util.List;

public record DisponibilidadResponse(
        Integer habitacionId,
        String numeroHabitacion,
        Integer hotelId,
        String hotelNombre,
        Integer sucursalId,
        String sucursalNombre,
        String ciudad,
        NombreTipoHabitacion tipoHabitacion,
        Integer capacidadMaxima,
        BigDecimal precioNoche,
        int noches,
        BigDecimal precioTotal,
        List<String> serviciosIncluidos
) {
}