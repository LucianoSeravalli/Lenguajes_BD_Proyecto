package lenguajesBD_Grupo06.Repository;

import lenguajesBD_Grupo06.Domain.Ejercicio;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
public interface EjercicioRepository extends JpaRepository<Ejercicio, Long> {
 
    List<Ejercicio> findByGrupoMuscularIgnoreCase(String grupoMuscular);
 
    List<Ejercicio> findByNombreContainingIgnoreCase(String texto);
}
 