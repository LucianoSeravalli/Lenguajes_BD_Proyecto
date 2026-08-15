package lenguajesBD_Grupo06.Domain;

import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "APP_ROL")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AppRol {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ROL")
    private Long idRol;
 
    /** CLIENTE, ENTRENADOR, ADMINISTRADOR */
    @Column(name = "NOMBRE_ROL", nullable = false, length = 30, unique = true)
    private String nombreRol;
 
    @Column(name = "DESCRIPCION", length = 200)
    private String descripcion;
 
    /** 'A' activo, 'I' inactivo */
    @Column(name = "ESTADO", nullable = false, length = 1)
    private String estado = "A";
}
 