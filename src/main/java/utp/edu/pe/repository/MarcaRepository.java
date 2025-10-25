package utp.edu.pe.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import utp.edu.pe.entity.Marca;
import utp.edu.pe.entity.enums.EstadoGeneral;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarcaRepository extends JpaRepository<Marca, Long>{
	
    
    Optional<Marca> findByNombre(String nombre);
    List<Marca> findByEstado(EstadoGeneral estado);
    
    @Query("SELECT m FROM Marca m WHERE m.estado =\"Activo\"  ORDER BY m.nombre")
    List<Marca> findAllActive();
    
    boolean existsByNombre(String nombre);

}
