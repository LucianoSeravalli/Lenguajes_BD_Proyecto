package lenguajesBD_Grupo06.Domain;

import jakarta.persistence.*;
import lombok.*;
 
/**
 * HORA_INICIO y HORA_FIN son VARCHAR2(5) en formato HH:MM validado por
 * REGEXP en la base, por eso se mapean como String y no como LocalTime.
 */
@Entity
@Table(name = "CLASE")
@Getter @Setter @NoArgsConstructor
public class Clase {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CLASE")
    private Long idClase;
 
    @Column(name = "NOMBRE", nullable = false, length = 60)
    private String nombre;
 
    @Column(name = "DESCRIPCION", length = 300)
    private String descripcion;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ENTRENADOR", nullable = false)
    private Usuario entrenador;
 
    @Column(name = "CUPO_MAXIMO", nullable = false)
    private Integer cupoMaximo;
 
    /** lunes ... domingo */
    @Column(name = "DIA_SEMANA", nullable = false, length = 10)
    private String diaSemana;
 
    @Column(name = "HORA_INICIO", nullable = false, length = 5)
    private String horaInicio;
 
    @Column(name = "HORA_FIN", nullable = false, length = 5)
    private String horaFin;
}