package sv.edu.udb.sistemahotel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import sv.edu.udb.sistemahotel.model.enums.NombreTipoHabitacion;

@Entity
@Table(name = "tipo_habitacion")
// restriccion en la bd: la capacidad debe estar entre 1 y 10
@Check(constraints = "capacidad_maxima BETWEEN 1 AND 10")
@Getter
@Setter
@NoArgsConstructor
public class TipoHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_habitacion")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nombre", nullable = false, length = 40)
    private NombreTipoHabitacion nombre;

    @Column(name = "capacidad_maxima", nullable = false)
    private Integer capacidadMaxima;

    @Column(name = "descripcion", length = 200)
    private String descripcion;
}