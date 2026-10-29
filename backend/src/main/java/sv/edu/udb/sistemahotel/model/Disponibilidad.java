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
import sv.edu.udb.sistemahotel.model.enums.EstadoDisponibilidad;

import java.time.LocalDate;

@Entity
@Table(
        name = "disponibilidad",
        // Solo puede existir un registro por habitacion y día
        uniqueConstraints = @UniqueConstraint(
                name = "uk_disponibilidad_habitacion_fecha",
                columnNames = {"id_habitacion", "fecha"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Disponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_disponibilidad")
    private Integer id;

    // Habitacion a la que pertenece el registro (fk a habitacion.id_habitacion)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_habitacion", nullable = false)
    private Habitacion habitacion;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_disponibilidad", nullable = false, length = 15)
    private EstadoDisponibilidad estadoDisponibilidad;
}