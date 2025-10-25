package utp.edu.pe.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import utp.edu.pe.entity.CategoriaProducto;
import utp.edu.pe.entity.enums.*;

@Repository
public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, Long> {
	
	 	Optional<CategoriaProducto> findByNombre(String nombre);
	    List<CategoriaProducto> findByEstado(EstadoGeneral estado);
	    List<CategoriaProducto> findByTipo(TipoProducto tipo);
	    List<CategoriaProducto> findByTipoAndEstado(TipoProducto tipo, EstadoGeneral estado);
	    
	    @Query("SELECT c FROM CategoriaProducto c WHERE c.estado = \"Activo\"  ORDER BY c.nombre")
	    List<CategoriaProducto> findAllActive();
	    
	    boolean existsByNombre(String nombre);

}
