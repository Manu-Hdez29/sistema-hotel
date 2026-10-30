package sv.edu.udb.sistemahotel.dto;

import sv.edu.udb.sistemahotel.model.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record EstadisticasResponse(
        LocalDate desde,
        LocalDate hasta,
        long reservasTotales,
        Map<EstadoReserva, Long> reservasPorEstado,
        long cancelaciones,
        BigDecimal ingresosTotales,
        BigDecimal reembolsosTotales,
        long nochesOcupadas,
        long nochesDisponibles,
        BigDecimal ocupacionPorcentaje
) {
}