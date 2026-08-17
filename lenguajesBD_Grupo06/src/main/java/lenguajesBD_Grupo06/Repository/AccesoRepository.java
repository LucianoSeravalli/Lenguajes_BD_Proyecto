
package lenguajesBD_Grupo06.Repository;

import lenguajesBD_Grupo06.Domain.Acceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.time.LocalDateTime;
import java.util.List;
 
public interface AccesoRepository extends JpaRepository<Acceso, Long> {
 
    List<Acceso> findByClienteIdClienteOrderByFechaHoraDesc(Long idCliente);
 
    List<Acceso> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime desde, LocalDateTime hasta);
 
    @Query("""
           SELECT COUNT(a) FROM Acceso a
           WHERE a.resultado = :resultado
             AND a.fechaHora BETWEEN :desde AND :hasta
           """)
    long contarPorResultado(@Param("resultado") String resultado,
                            @Param("desde") LocalDateTime desde,
                            @Param("hasta") LocalDateTime hasta);
}
 