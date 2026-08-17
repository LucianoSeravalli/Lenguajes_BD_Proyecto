package lenguajesBD_Grupo06.Repository;

import lenguajesBD_Grupo06.Domain.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.List;
 
/** TRG_RUTINA_VALIDAR_ENTRENADOR rechaza con ORA-20010 si el usuario no es entrenador activo. */
public interface RutinaRepository extends JpaRepository<Rutina, Long> {
 
    @Query("""
           SELECT r FROM Rutina r
           JOIN FETCH r.entrenador
           WHERE r.entrenador.idUsuario = :idEntrenador
           """)
    List<Rutina> findByEntrenadorIdUsuario(@Param("idEntrenador") Long idEntrenador);
 
    List<Rutina> findByNivel(String nivel);
}
 