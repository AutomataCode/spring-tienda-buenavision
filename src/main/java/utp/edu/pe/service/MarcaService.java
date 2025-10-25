package utp.edu.pe.service;

import java.util.List;
import java.util.Optional;

import utp.edu.pe.entity.Marca;

public interface MarcaService {
	
		Marca save(Marca marca);
	    List<Marca> findAll();
	    List<Marca> findAllActive();
	    Optional<Marca> findById(Long id);
	    Optional<Marca> findByNombre(String nombre);
	    void deleteById(Long id);
	    void deactivate(Long id);
	    boolean existsByNombre(String nombre);

}
