package sv.edu.udb.sistemahotel.dto;

import sv.edu.udb.sistemahotel.model.enums.EstadoGeneral;

public record HotelResponse(
        Integer id,
        String nombre,
        String nitRuc,
        EstadoGeneral estado
) {
}