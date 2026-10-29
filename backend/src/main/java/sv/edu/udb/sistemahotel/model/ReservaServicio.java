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
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@Entity
@Table(name = "reserva_servicio")
// Al menos una unidad del servicio, y el subtotal no puede ser negativo
@Check(constraints = "cantidad >= 1 AND subtotal >= 0")
@Getter
@Setter
@NoArgsConstructor
public class ReservaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva_servicio")
    private Integer id;

    // Reserva a la que se agrega el servicio (fk a reserva.id_reserva)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    // Servicio contratado (fk a servicio.id_servicio)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 1;

    // Se calcula en el servicio como cantidad × precio unitario del servicio
    @Column(name = "subtotal", nullable = false, precision = 8, scale = 2)
    private BigDecimal subtotal;
}