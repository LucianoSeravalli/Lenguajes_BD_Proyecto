package lenguajesBD_Grupo06.Repository;

import lenguajesBD_Grupo06.Domain.RutinaEjercicio;
import lenguajesBD_Grupo06.Domain.RutinaEjercicioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.List;
 
public interface RutinaEjercicioRepository
        extends JpaRepository<RutinaEjercicio, RutinaEjercicioId> {
 
    @Query("""
           SELECT re FROM RutinaEjercicio re
           JOIN FETCH re.ejercicio
           WHERE re.rutina.idRutina = :idRutina
           """)
    List<RutinaEjercicio> findDetalleDeRutina(@Param("idRutina") Long idRutina);
 
    void deleteByRutinaIdRutina(Long idRutina);
}
