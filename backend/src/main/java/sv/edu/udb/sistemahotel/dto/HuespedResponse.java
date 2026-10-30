package sv.edu.udb.sistemahotel.dto;

import java.time.LocalDate;

public record HuespedResponse(
        Integer id,
        String nombreCompleto,
        LocalDate fechaNacimiento,
        String documentoIdentidad,
        String telefono,
        String email,
        Integer usuarioId
) {
}