package utp.edu.pe.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import utp.edu.pe.entity.FormaMontura;
import utp.edu.pe.entity.enums.EstadoGeneral;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormaMonturaRepository extends JpaRepository<FormaMontura, Long> {
	
	  Optional<FormaMontura> findByNombre(String nombre);
	    List<FormaMontura> findByEstado(EstadoGeneral estado);
	    
	    @Query("SELECT f FROM FormaMontura f WHERE f.estado = 'ACTIVO' ORDER BY f.nombre")
	    List<FormaMontura> findAllActive();
	    
	    boolean existsByNombre(String nombre);

}
