package sv.edu.udb.sistemahotel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres.")
        String nombreCompleto,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 100, message = "El correo no puede pasar de 100 caracteres.")
        String email,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, max = 60, message = "La contraseña debe tener entre 8 y 60 caracteres.")
        String password,

        @NotNull(message = "La fecha de nacimiento es obligatoria.")
        @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
        LocalDate fechaNacimiento,

        // DUI con formato ########-# o pasaporte alfanumérico de 6 a 20 caracteres
        @NotBlank(message = "El documento es obligatorio.")
        @Pattern(regexp = "^(\\d{8}-\\d|[A-Za-z0-9]{6,20})$",
                message = "El documento debe ser un DUI (########-#) o un pasaporte válido.")
        String documentoIdentidad,

        @NotBlank(message = "El teléfono es obligatorio.")
        @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "El teléfono debe tener formato ####-####.")
        String telefono
) {
}