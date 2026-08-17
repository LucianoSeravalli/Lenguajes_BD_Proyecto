
package lenguajesBD_Grupo06.Repository;

import lenguajesBD_Grupo06.Domain.Clase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.List;
 
/** TRG_CLASE_VALIDAR_ENTRENADOR rechaza con ORA-20010 si el usuario no es entrenador activo. */
public interface ClaseRepository extends JpaRepository<Clase, Long> {
 
    List<Clase> findByDiaSemanaOrderByHoraInicio(String diaSemana);
 
    List<Clase> findByEntrenadorIdUsuario(Long idEntrenador);
 
    /**
     * Detecta choques de horario del mismo entrenador antes de guardar la clase.
     * Las horas son VARCHAR2 'HH:MM', que ordena igual que el tiempo real.
     */
    @Query("""
           SELECT COUNT(c) > 0 FROM Clase c
           WHERE c.entrenador.idUsuario = :idEntrenador
             AND c.diaSemana = :diaSemana
             AND (:idClase IS NULL OR c.idClase <> :idClase)
             AND c.horaInicio < :horaFin
             AND c.horaFin > :horaInicio
           """)
    boolean existeChoqueHorario(@Param("idEntrenador") Long idEntrenador,
                                @Param("diaSemana") String diaSemana,
                                @Param("horaInicio") String horaInicio,
                                @Param("horaFin") String horaFin,
                                @Param("idClase") Long idClase);
}