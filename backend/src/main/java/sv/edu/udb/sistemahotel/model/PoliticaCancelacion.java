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
@Table(name = "politica_cancelacion")
// Los días previos no pueden ser negativos y el porcentaje va de 0 a 100
@Check(constraints = "dias_previos >= 0 AND porcentaje_cargo BETWEEN 0 AND 100")
@Getter
@Setter
@NoArgsConstructor
public class PoliticaCancelacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_politica")
    private Integer id;

    // Hotel al que aplica la política (fk a hotel.id_hotel)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_hotel", nullable = false)
    private Hotel hotel;

    // Anticipacion mínima exigida, en días
    @Column(name = "dias_previos", nullable = false)
    private Integer diasPrevios;

    // Porcentaje del total que se cobra si se aplica esta política
    @Column(name = "porcentaje_cargo", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeCargo;

    @Column(name = "descripcion", length = 200)
    private String descripcion;
}