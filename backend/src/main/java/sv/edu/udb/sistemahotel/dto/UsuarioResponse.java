package sv.edu.udb.sistemahotel.dto;

import sv.edu.udb.sistemahotel.model.enums.EstadoGeneral;

public record UsuarioResponse(
        Integer id,
        String nombreCompleto,
        String email,
        String rol,
        EstadoGeneral estado
) {
}