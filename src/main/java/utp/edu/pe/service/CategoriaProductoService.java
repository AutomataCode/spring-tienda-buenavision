package utp.edu.pe.service;

import utp.edu.pe.entity.CategoriaProducto;
import utp.edu.pe.entity.enums.TipoProducto;

import java.util.List;
import java.util.Optional;

public interface CategoriaProductoService {
	
		CategoriaProducto save(CategoriaProducto categoria);
	    List<CategoriaProducto> findAll();
	    List<CategoriaProducto> findAllActive();
	    Optional<CategoriaProducto> findById(Long id);
	    Optional<CategoriaProducto> findByNombre(String nombre);
	    List<CategoriaProducto> findByTipo(TipoProducto tipo);
	    List<CategoriaProducto> findByTipoAndActive(TipoProducto tipo);
	    void deleteById(Long id);
	    void deactivate(Long id);
	    boolean existsByNombre(String nombre);

}
