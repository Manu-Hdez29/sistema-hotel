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

@Entity
@Table(name = "sucursal")
@Getter
@Setter
@NoArgsConstructor
public class Sucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sucursal")
    private Integer id;

    // Cada sucursal pertenece a un hotel (FK a hotel.id_hotel)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_hotel", nullable = false)
    private Hotel hotel;

    @Column(name = "nombre_sucursal", nullable = false, length = 100)
    private String nombreSucursal;

    // La ciudad se usa como criterio de búsqueda en la reserva
    @Column(name = "ciudad", nullable = false, length = 60)
    private String ciudad;

    @Column(name = "direccion", nullable = false, length = 150)
    private String direccion;

    @Column(name = "telefono", length = 15)
    private String telefono;
}