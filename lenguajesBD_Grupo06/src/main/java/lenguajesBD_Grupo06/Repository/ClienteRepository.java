
package lenguajesBD_Grupo06.Repository;

import lenguajesBD_Grupo06.Domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.List;
import java.util.Optional;
 
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
 
    Optional<Cliente> findByCedula(String cedula);
 
    /** Misma condicion que usa SP_RESERVAR_CLASE para ubicar al cliente. */
    Optional<Cliente> findByCedulaAndEstado(String cedula, String estado);
 
    boolean existsByCedula(String cedula);
 
    Optional<Cliente> findByUsuarioIdUsuario(Long idUsuario);
 
    @Query("""
           SELECT c FROM Cliente c
           JOIN FETCH c.usuario u
           WHERE c.estado = :estado
           ORDER BY u.apellido, u.nombre
           """)
    List<Cliente> findByEstadoConUsuario(@Param("estado") String estado);
}