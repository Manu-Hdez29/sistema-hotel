package sv.edu.udb.sistemahotel.dto;

import sv.edu.udb.sistemahotel.model.enums.EstadoPago;
import sv.edu.udb.sistemahotel.model.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoListadoResponse(
        Integer id,
        Integer reservaId,
        MetodoPago metodo,
        BigDecimal monto,
        LocalDateTime fecha,
        EstadoPago estado
) {
}