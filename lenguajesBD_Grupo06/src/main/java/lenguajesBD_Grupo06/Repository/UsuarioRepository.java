
package lenguajesBD_Grupo06.Repository;

import lenguajesBD_Grupo06.Domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
 
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
 
    /** El indice UQ_USUARIO_CORREO_LOWER hace la unicidad insensible a mayusculas. */
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
 
    boolean existsByCorreoIgnoreCase(String correo);
 
    /** Para el enlace de confirmacion: el backend hashea el token y busca por hash. */
    Optional<Usuario> findByTokenConfirmacionHashAndTokenExpiracionAfter(
            String tokenConfirmacionHash, LocalDateTime ahora);
 
    @Query("""
           SELECT u FROM Usuario u
           WHERE u.estado = 'activo'
             AND EXISTS (SELECT 1 FROM UsuarioRol ur
                          WHERE ur.usuario = u
                            AND UPPER(ur.rol.nombreRol) = UPPER(:nombreRol)
                            AND ur.rol.estado = 'A')
           ORDER BY u.apellido, u.nombre
           """)
    List<Usuario> findActivosPorRol(@Param("nombreRol") String nombreRol);
 
    @Query("""
           SELECT COUNT(ur) > 0 FROM UsuarioRol ur
           WHERE ur.usuario.idUsuario = :idUsuario
             AND ur.usuario.estado = 'activo'
             AND UPPER(ur.rol.nombreRol) = UPPER(:nombreRol)
             AND ur.rol.estado = 'A'
           """)
    boolean tieneRol(@Param("idUsuario") Long idUsuario, @Param("nombreRol") String nombreRol);
}
