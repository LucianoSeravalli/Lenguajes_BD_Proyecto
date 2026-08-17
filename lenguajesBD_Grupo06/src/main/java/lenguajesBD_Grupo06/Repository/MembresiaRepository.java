
package lenguajesBD_Grupo06.Repository;

import lenguajesBD_Grupo06.Domain.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
 
public interface MembresiaRepository extends JpaRepository<Membresia, Long> {
 
    @Query("""
           SELECT m FROM Membresia m
           JOIN FETCH m.tipoMembresia
           WHERE m.cliente.idCliente = :idCliente
           ORDER BY m.fechaFin DESC
           """)
    List<Membresia> findByClienteIdClienteOrderByFechaFinDesc(@Param("idCliente") Long idCliente);
 
    /**
     * Membresia dentro de su rango de fechas y no cancelada. No comprueba pagos:
     * para eso esta FN_MEMBRESIA_ACTIVA, que es la fuente de verdad del negocio.
     */
    @Query("""
           SELECT m FROM Membresia m
           JOIN FETCH m.tipoMembresia
           WHERE m.cliente.idCliente = :idCliente
             AND m.estado <> 'cancelada'
             AND m.fechaInicio <= :hoy
             AND m.fechaFin >= :hoy
           ORDER BY m.fechaFin DESC
           """)
    List<Membresia> findVigentes(@Param("idCliente") Long idCliente, @Param("hoy") LocalDate hoy);
 
    default Optional<Membresia> findVigenteActual(Long idCliente) {
        return findVigentes(idCliente, LocalDate.now()).stream().findFirst();
    }
 
    /** Para el reporte de renovaciones proximas. */
    List<Membresia> findByEstadoAndFechaFinBetween(String estado, LocalDate desde, LocalDate hasta);
}