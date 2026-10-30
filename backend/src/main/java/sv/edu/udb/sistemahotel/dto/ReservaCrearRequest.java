package sv.edu.udb.sistemahotel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record ReservaCrearRequest(
        @NotNull(message = "La habitación es obligatoria.")
        Integer habitacionId,

        @NotNull(message = "La fecha de entrada es obligatoria.")
        LocalDate fechaEntrada,

        @NotNull(message = "La fecha de salida es obligatoria.")
        LocalDate fechaSalida,

        @Size(max = 250, message = "Las solicitudes no pueden pasar de 250 caracteres.")
        String solicitudesEspeciales,

        @NotNull(message = "Los datos del titular son obligatorios.")
        @Valid
        TitularRequest titular,

        // Huéspedes adicionales (el titular no va aquí)
        List<Integer> huespedesIds,

        @Valid
        List<ServicioSolicitud> servicios
) {

    public record TitularRequest(
            @NotBlank(message = "El nombre del titular es obligatorio.")
            @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres.")
            String nombreCompleto,

            @NotNull(message = "La fecha de nacimiento del titular es obligatoria.")
            @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
            LocalDate fechaNacimiento,

            @NotBlank(message = "El documento del titular es obligatorio.")
            @Pattern(regexp = "^(\\d{8}-\\d)$",
                    message = "El documento debe ser un DUI (########-#).")
            String documentoIdentidad,

            @NotBlank(message = "El teléfono del titular es obligatorio.")
            @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "El teléfono debe tener formato ####-####.")
            String telefono,

            @NotBlank(message = "El correo del titular es obligatorio.")
            @Email(message = "El correo no tiene un formato válido.")
            @Size(max = 100, message = "El correo no puede pasar de 100 caracteres.")
            String email
    ) {
    }

    public record ServicioSolicitud(
            @NotNull(message = "El servicio es obligatorio.")
            Integer servicioId,

            @NotNull(message = "La cantidad es obligatoria.")
            @Min(value = 1, message = "La cantidad mínima es 1.")
            Integer cantidad
    ) {
    }
}