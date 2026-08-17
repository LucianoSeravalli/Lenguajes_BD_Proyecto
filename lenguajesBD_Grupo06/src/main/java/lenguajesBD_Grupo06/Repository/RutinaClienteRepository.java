
package lenguajesBD_Grupo06.Repository;

import lenguajesBD_Grupo06.Domain.RutinaCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.time.LocalDate;
import java.util.List;
 
public interface RutinaClienteRepository extends JpaRepository<RutinaCliente, Long> {
 
    List<RutinaCliente> findByClienteIdClienteOrderByFechaAsignacionDesc(Long idCliente);
 
    /** Asignaciones sin fecha de fin o cuya vigencia no ha terminado. */
    @Query("""
           SELECT rc FROM RutinaCliente rc
           JOIN FETCH rc.rutina
           WHERE rc.cliente.idCliente = :idCliente
             AND (rc.fechaFin IS NULL OR rc.fechaFin >= :hoy)
           """)
    List<RutinaCliente> findVigentes(@Param("idCliente") Long idCliente, @Param("hoy") LocalDate hoy);
 
    default List<RutinaCliente> findVigentesHoy(Long idCliente) {
        return findVigentes(idCliente, LocalDate.now());
    }
}
 