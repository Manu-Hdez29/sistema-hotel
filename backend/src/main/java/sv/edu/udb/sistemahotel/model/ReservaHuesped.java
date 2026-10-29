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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "reserva_huesped",
        // Un mismo huesped no puede aparecer dos veces en la misma reserva
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reserva_huesped",
                columnNames = {"id_reserva", "id_huesped"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ReservaHuesped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva_huesped")
    private Integer id;

    // Reserva a la que pertenece (fk a reserva.id_reserva)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    // Huesped que se hospeda (fk a huesped.id_huesped)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_huesped", nullable = false)
    private Huesped huesped;

    // Marca al titular de la reserva. Solo debe haber uno por reserva
    @Column(name = "es_titular", nullable = false)
    private Boolean esTitular = false;
}