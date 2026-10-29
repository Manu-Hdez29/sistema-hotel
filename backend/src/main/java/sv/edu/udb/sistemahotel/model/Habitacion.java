package sv.edu.udb.sistemahotel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sv.edu.udb.sistemahotel.model.enums.EstadoHabitacion;

@Entity
@Table(
        name = "habitacion",
        // El número de habitacion no puede repetirse dentro de la misma sucursal
        uniqueConstraints = @UniqueConstraint(
                name = "uk_habitacion_sucursal_numero",
                columnNames = {"id_sucursal", "numero_habitacion"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_habitacion")
    private Integer id;

    // Sucursal a la que pertenece la habitacion (tiene una fk a sucursal.id_sucursal)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    // Tipo de habitacion (tambien tiene una fk a tipo_habitacion.id_tipo_habitacion)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_habitacion", nullable = false)
    private TipoHabitacion tipoHabitacion;

    @Column(name = "numero_habitacion", nullable = false, length = 10)
    private String numeroHabitacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoHabitacion estado = EstadoHabitacion.DISPONIBLE;
}