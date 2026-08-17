
package lenguajesBD_Grupo06.Repository;

import lenguajesBD_Grupo06.Domain.AppRol;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
import java.util.Optional;
 
public interface AppRolRepository extends JpaRepository<AppRol, Long> {
 
    Optional<AppRol> findByNombreRolIgnoreCase(String nombreRol);
 
    List<AppRol> findByEstado(String estado);
}
 