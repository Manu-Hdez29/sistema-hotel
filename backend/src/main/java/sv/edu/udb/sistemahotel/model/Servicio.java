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
import sv.edu.udb.sistemahotel.model.enums.TipoServicio;

import java.math.BigDecimal;

@Entity
@Table(name = "servicio")
// El precio no puede ser negativo
@Check(constraints = "precio >= 0")
@Getter
@Setter
@NoArgsConstructor
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Integer id;

    // Nombre comercial del servicio, por ejemplo "desayuno buffet" o "lavado express"
    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @Column(name = "precio", nullable = false, precision = 8, scale = 2)
    private BigDecimal precio;

    // Categoría general del servicio (alimentacion, transporte, etc.)
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", nullable = false, length = 30)
    private TipoServicio tipoServicio;
}