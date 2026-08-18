package lenguajesBD_Grupo06.Domain;

import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDateTime;
 
/** Bitacora de entradas y salidas al gimnasio. */
@Entity
@Table(name = "ACCESO")
@Data
public class Acceso {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ACCESO")
    private Long idAcceso;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CLIENTE", nullable = false)
    private Cliente cliente;
 
    @Column(name = "FECHA_HORA", nullable = false)
    private LocalDateTime fechaHora;
 
    /** entrada | salida */
    @Column(name = "TIPO", nullable = false, length = 10)
    private String tipo;
 
    /** permitido | denegado */
    @Column(name = "RESULTADO", nullable = false, length = 10)
    private String resultado;
}
