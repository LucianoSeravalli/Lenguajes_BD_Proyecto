
package lenguajesBD_Grupo06.Repository;

import lenguajesBD_Grupo06.Domain.Acceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.time.LocalDateTime;
import java.util.List;
 
public interface AccesoRepository extends JpaRepository<Acceso, Long> {
 
    @Query("""
           SELECT a FROM Acceso a
           JOIN FETCH a.cliente c
           JOIN FETCH c.usuario
           WHERE c.idCliente = :idCliente
           ORDER BY a.fechaHora DESC
           """)
    List<Acceso> findByClienteIdClienteOrderByFechaHoraDesc(@Param("idCliente") Long idCliente);
 
    @Query("""
           SELECT a FROM Acceso a
           JOIN FETCH a.cliente c
           JOIN FETCH c.usuario
           WHERE a.fechaHora BETWEEN :desde AND :hasta
           ORDER BY a.fechaHora DESC
           """)
    List<Acceso> findByFechaHoraBetweenOrderByFechaHoraDesc(
            @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
 
    @Query("""
           SELECT COUNT(a) FROM Acceso a
           WHERE a.resultado = :resultado
             AND a.fechaHora BETWEEN :desde AND :hasta
           """)
    long contarPorResultado(@Param("resultado") String resultado,
                            @Param("desde") LocalDateTime desde,
                            @Param("hasta") LocalDateTime hasta);
}