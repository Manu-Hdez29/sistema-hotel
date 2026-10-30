package sv.edu.udb.sistemahotel.dto;

import java.math.BigDecimal;

public record CancelacionResponse(
        Integer reservaId,
        Integer politicaAplicadaId,
        BigDecimal porcentajeCargo,
        BigDecimal cargo,
        BigDecimal montoPagado,
        BigDecimal montoReembolso
) {
}