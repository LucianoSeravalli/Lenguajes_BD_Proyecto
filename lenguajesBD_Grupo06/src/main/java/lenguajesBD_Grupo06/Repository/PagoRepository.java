
package lenguajesBD_Grupo06.Repository;

import lenguajesBD_Grupo06.Domain.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
 
/**
 * Solo lectura desde JPA. Las altas van por SP_REGISTRAR_PAGO para que
 * TRG_PAGO_SYNC_MEMBRESIA recalcule el estado de la membresia.
 */
public interface PagoRepository extends JpaRepository<Pago, Long> {
 
    @Query("""
           SELECT p FROM Pago p
           JOIN FETCH p.membresia
           WHERE p.membresia.idMembresia = :idMembresia
           ORDER BY p.fechaPago DESC
           """)
    List<Pago> findByMembresiaIdMembresiaOrderByFechaPagoDesc(@Param("idMembresia") Long idMembresia);
 
    @Query("""
           SELECT p FROM Pago p
           JOIN FETCH p.membresia m
           WHERE m.cliente.idCliente = :idCliente
           ORDER BY p.fechaPago DESC
           """)
    List<Pago> findHistorialPorCliente(@Param("idCliente") Long idCliente);
 
    boolean existsByMembresiaIdMembresiaAndEstado(Long idMembresia, String estado);
 
    @Query("""
           SELECT COALESCE(SUM(p.monto), 0) FROM Pago p
           WHERE p.estado = 'completado'
             AND p.fechaPago BETWEEN :desde AND :hasta
           """)
    BigDecimal totalRecaudado(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}