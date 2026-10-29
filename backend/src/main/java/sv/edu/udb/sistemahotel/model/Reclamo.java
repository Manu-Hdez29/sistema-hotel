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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import sv.edu.udb.sistemahotel.model.enums.EstadoReclamo;

import java.time.LocalDateTime;

@Entity
@Table(name = "reclamo")
@Getter
@Setter
@NoArgsConstructor
public class Reclamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reclamo")
    private Integer id;

    // Huesped que presenta el reclamo (fk a huesped.id_huesped)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_huesped", nullable = false)
    private Huesped huesped;

    // Reserva relacionada, si la hay
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    // Empleado que atiende el reclamo. Es nulo mientras nadie lo haya tomado.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "descripcion", nullable = false, length = 250)
    private String descripcion;

    // Hibernate la llena al crear el reclamo
    @CreationTimestamp
    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoReclamo estado = EstadoReclamo.ABIERTO;

    @Column(name = "respuesta", length = 250)
    private String respuesta;
}