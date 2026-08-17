
package lenguajesBD_Grupo06.Repository;

import lenguajesBD_Grupo06.Domain.TipoMembresia;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
import java.util.Optional;
 
public interface TipoMembresiaRepository extends JpaRepository<TipoMembresia, Long> {
 
    Optional<TipoMembresia> findByNombreIgnoreCase(String nombre);
 
    List<TipoMembresia> findAllByOrderByDuracionMesesAsc();
}
 