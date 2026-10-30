package sv.edu.udb.sistemahotel.dto;

import sv.edu.udb.sistemahotel.model.enums.EstadoPago;
import sv.edu.udb.sistemahotel.model.enums.EstadoReserva;
import sv.edu.udb.sistemahotel.model.enums.MetodoPago;
import sv.edu.udb.sistemahotel.model.enums.NombreTipoHabitacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReservaResponse(
        Integer id,
        EstadoReserva estado,
        Integer habitacionId,
        String numeroHabitacion,
        String hotelNombre,
        String sucursalNombre,
        NombreTipoHabitacion tipoHabitacion,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        Integer noches,
        BigDecimal precioTotal,
        BigDecimal totalPagado,
        BigDecimal saldo,
        String solicitudesEspeciales,
        PersonaResumen titular,
        List<PersonaResumen> huespedes,
        List<ServicioReservado> servicios,
        List<PagoResumen> pagos,
        LocalDateTime fechaCreacion
) {

    public record PersonaResumen(Integer id, String nombreCompleto) {
    }

    public record ServicioReservado(Integer servicioId, String nombre, Integer cantidad, BigDecimal subtotal) {
    }

    public record PagoResumen(Integer id, MetodoPago metodo, BigDecimal monto,
                              LocalDateTime fecha, EstadoPago estado) {
    }
}