package sv.edu.udb.sistemahotel.dto;

import sv.edu.udb.sistemahotel.model.enums.TipoServicio;

import java.math.BigDecimal;

public record ServicioResponse(
        Integer id,
        String nombre,
        BigDecimal precio,
        TipoServicio tipoServicio
) {
}