package lenguajesBD_Grupo06.Repository;

import lenguajesBD_Grupo06.Domain.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
/** TRG_RUTINA_VALIDAR_ENTRENADOR rechaza con ORA-20010 si el usuario no es entrenador activo. */
public interface RutinaRepository extends JpaRepository<Rutina, Long> {
 
    List<Rutina> findByEntrenadorIdUsuario(Long idEntrenador);
 
    List<Rutina> findByNivel(String nivel);
}
 