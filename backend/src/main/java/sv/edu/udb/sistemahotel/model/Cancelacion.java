package sv.edu.udb.sistemahotel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancelacion")
// El reembolso no puede ser negativo
@Check(constraints = "monto_reembolso >= 0")
@Getter
@Setter
@NoArgsConstructor
public class Cancelacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cancelacion")
    private Integer id;

    // Una reserva solo puede cancelarse una vez. Por eso es OneToOne y la columna es UNIQUE.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reserva", nullable = false, unique = true)
    private Reserva reserva;

    // Política que se aplico para calcular el cargo (fk a politica_cancelacion.id_politica)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_politica", nullable = false)
    private PoliticaCancelacion politica;

    // Hibernate la llena al guardar
    @CreationTimestamp
    @Column(name = "fecha_cancelacion", nullable = false, updatable = false)
    private LocalDateTime fechaCancelacion;

    @Column(name = "motivo", length = 200)
    private String motivo;

    // Lo que se devuelve al huesped: total pagado menos el cargo de la política
    @Column(name = "monto_reembolso", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoReembolso;
}