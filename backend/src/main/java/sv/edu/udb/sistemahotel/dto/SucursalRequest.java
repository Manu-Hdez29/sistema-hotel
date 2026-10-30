package sv.edu.udb.sistemahotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SucursalRequest(
        @NotNull(message = "El hotel es obligatorio.")
        Integer hotelId,

        @NotBlank(message = "El nombre de la sucursal es obligatorio.")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres.")
        String nombre,

        @NotBlank(message = "La ciudad es obligatoria.")
        @Size(max = 60, message = "La ciudad no puede pasar de 60 caracteres.")
        String ciudad,

        @NotBlank(message = "La dirección es obligatoria.")
        @Size(max = 150, message = "La dirección no puede pasar de 150 caracteres.")
        String direccion,

        @Size(max = 15, message = "El teléfono no puede pasar de 15 caracteres.")
        String telefono
) {
}