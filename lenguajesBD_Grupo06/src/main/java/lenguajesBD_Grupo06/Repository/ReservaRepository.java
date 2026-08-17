
package lenguajesBD_Grupo06.Repository;

import lenguajesBD_Grupo06.Domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.List;
 
/**
 * Solo lectura y cambios de estado. Las altas van por SP_RESERVAR_CLASE:
 * ahi estan el bloqueo FOR UPDATE y las validaciones de cupo y duplicados.
 */
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
 
    long countByClaseIdClaseAndEstado(Long idClase, String estado);
 
    boolean existsByClienteIdClienteAndClaseIdClaseAndEstado(
            Long idCliente, Long idClase, String estado);
 
    @Query("""
           SELECT r FROM Reserva r
           JOIN FETCH r.clase c
           JOIN FETCH c.entrenador
           WHERE r.cliente.idCliente = :idCliente
             AND r.estado = :estado
           ORDER BY r.fechaReserva DESC
           """)
    List<Reserva> findPorClienteYEstado(@Param("idCliente") Long idCliente,
                                        @Param("estado") String estado);
 
    @Query("""
           SELECT r FROM Reserva r
           JOIN FETCH r.cliente cl
           JOIN FETCH cl.usuario
           WHERE r.clase.idClase = :idClase
             AND r.estado = 'confirmada'
           """)
    List<Reserva> findConfirmadasDeClase(@Param("idClase") Long idClase);
}