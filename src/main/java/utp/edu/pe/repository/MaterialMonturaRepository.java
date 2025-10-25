package utp.edu.pe.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import utp.edu.pe.entity.MaterialMontura;
import utp.edu.pe.entity.enums.EstadoGeneral;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialMonturaRepository extends JpaRepository<MaterialMontura, Long> {
	
    Optional<MaterialMontura> findByNombre(String nombre);
    List<MaterialMontura> findByEstado(EstadoGeneral estado);
    
    @Query("SELECT m FROM MaterialMontura m WHERE m.estado = 'ACTIVO' ORDER BY m.nombre")
    List<MaterialMontura> findAllActive();
    
    boolean existsByNombre(String nombre);

}
