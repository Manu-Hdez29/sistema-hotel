package sv.edu.udb.sistemahotel.dto;

import java.math.BigDecimal;

public record PoliticaCancelacionResponse(
        Integer id,
        Integer hotelId,
        String hotelNombre,
        Integer diasPrevios,
        BigDecimal porcentajeCargo,
        String descripcion
) {
}