package sv.edu.udb.sistemahotel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "huesped")
@Getter
@Setter
@NoArgsConstructor
public class Huesped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_huesped")
    private Integer id;

    // Cuenta de acceso del huésped
    // No lo marcamos como UNIQUE porque SQL Server solo permite un NULL en una columna única.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    // Que sea una fecha pasada se valida en el servicio, porque SQL Server no permite GETDATE() en un CHECK
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    // DUI o pasaporte
    @Column(name = "documento_identidad", nullable = false, unique = true, length = 20)
    private String documentoIdentidad;

    @Column(name = "telefono", nullable = false, length = 15)
    private String telefono;

    @Column(name = "email", nullable = false, length = 100)
    private String email;
}