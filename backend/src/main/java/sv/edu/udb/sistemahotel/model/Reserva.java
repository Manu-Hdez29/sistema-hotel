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
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import sv.edu.udb.sistemahotel.model.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reserva")
// La salida debe ser posterior a la entrada, debe haber al menos una noche y el total no puede ser negativo
@Check(constraints = "fecha_salida > fecha_entrada AND numero_noches >= 1 AND precio_total >= 0")
@Getter
@Setter
@NoArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Integer id;

    // Habitacion reservada (fk a habitacion.id_habitacion).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_habitacion", nullable = false)
    private Habitacion habitacion;

    // Empleado que gestiono la reserva. Es nulo cuando la reserva la hace el huesped desde la web.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "fecha_entrada", nullable = false)
    private LocalDate fechaEntrada;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    // Se calcula en el servicio como fechaSalida - fechaEntrada
    @Column(name = "numero_noches", nullable = false)
    private Integer numeroNoches;

    @Column(name = "solicitudes_especiales", length = 250)
    private String solicitudesEspeciales;

    // Suma de tarifa × noches más los servicios contratados
    @Column(name = "precio_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_reserva", nullable = false, length = 15)
    private EstadoReserva estadoReserva = EstadoReserva.PENDIENTE;

    // Hibernate llena esta fecha automaticamente al guardar
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
}