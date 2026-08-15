package lenguajesBD_Grupo06.Domain;

import jakarta.persistence.*;
import lombok.*;
 
import java.math.BigDecimal;
 
@Entity
@Table(name = "TIPO_MEMBRESIA")
@Getter @Setter @NoArgsConstructor
public class TipoMembresia {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TIPO_MEMBRESIA")
    private Long idTipoMembresia;
 
    /** Mensual, Trimestral, Anual... */
    @Column(name = "NOMBRE", nullable = false, length = 30, unique = true)
    private String nombre;
 
    @Column(name = "DURACION_MESES", nullable = false)
    private Integer duracionMeses;
 
    @Column(name = "PRECIO", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
 
    @Column(name = "BENEFICIOS", length = 300)
    private String beneficios;
}