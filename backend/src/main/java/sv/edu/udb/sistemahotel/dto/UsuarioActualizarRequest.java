package sv.edu.udb.sistemahotel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioActualizarRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres.")
        String nombreCompleto,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 100, message = "El correo no puede pasar de 100 caracteres.")
        String email,

        @NotBlank(message = "El rol es obligatorio.")
        String rol,

        // Opcional: si viene vacío, la contraseña no cambia
        @Size(min = 8, max = 60, message = "La contraseña debe tener entre 8 y 60 caracteres.")
        String password
) {
}