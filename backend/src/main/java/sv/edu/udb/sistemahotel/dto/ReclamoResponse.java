package sv.edu.udb.sistemahotel.dto;

import sv.edu.udb.sistemahotel.model.enums.EstadoReclamo;

import java.time.LocalDateTime;

public record ReclamoResponse(
        Integer id,
        Integer huespedId,
        String huespedNombre,
        Integer reservaId,
        String descripcion,
        LocalDateTime fecha,
        EstadoReclamo estado,
        String respuesta,
        String atendidoPor
) {
}