package lenguajesBD_Grupo06.Domain;


import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDate;
 
/**
 * Perfil de cliente. TRG_CLIENTE_VALIDAR_ROL exige que el usuario
 * asociado tenga rol CLIENTE activo (ORA-20011).
 */
@Entity
@Table(name = "CLIENTE")
@Getter @Setter @NoArgsConstructor
public class Cliente {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CLIENTE")
    private Long idCliente;
 
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_USUARIO", nullable = false, unique = true)
    private Usuario usuario;
 
    @Column(name = "CEDULA", nullable = false, length = 20, unique = true)
    private String cedula;
 
    @Column(name = "FECHA_NACIMIENTO")
    private LocalDate fechaNacimiento;
 
    @Column(name = "FECHA_REGISTRO", insertable = false, updatable = false)
    private LocalDate fechaRegistro;
 
    /** activo | inactivo */
    @Column(name = "ESTADO", nullable = false, length = 10)
    private String estado = "activo";
}
 