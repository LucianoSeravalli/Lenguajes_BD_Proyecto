
package lenguajesBD_Grupo06.Repository;

import lenguajesBD_Grupo06.Domain.Usuario_rol;
import lenguajesBD_Grupo06.Domain.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
/**
 * Ojo al borrar: TRG_USUARIO_ROL_PROTEGER lanza ORA-20012 si el usuario
 * todavia tiene perfil de cliente o rutinas/clases asignadas.
 */
public interface UsuarioRolRepository extends JpaRepository<Usuario_rol, UsuarioRolId> {
 
    List<Usuario_rol> findByUsuarioIdUsuario(Long idUsuario);
 
    boolean existsByUsuarioIdUsuarioAndRolNombreRolIgnoreCase(Long idUsuario, String nombreRol);
}
 