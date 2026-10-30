package sv.edu.udb.sistemahotel.dto;

public record SucursalResponse(
        Integer id,
        Integer hotelId,
        String hotelNombre,
        String nombre,
        String ciudad,
        String direccion,
        String telefono
) {
}