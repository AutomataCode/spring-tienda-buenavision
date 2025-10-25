package utp.edu.pe.service;

import java.util.List;
import java.util.Optional;

import utp.edu.pe.entity.FormaMontura;

public interface FormaMonturaService {
	
		FormaMontura save(FormaMontura formaMontura);
	    List<FormaMontura> findAll();
	    List<FormaMontura> findAllActive();
	    Optional<FormaMontura> findById(Long id);
	    Optional<FormaMontura> findByNombre(String nombre);
	    void deleteById(Long id);
	    boolean existsByNombre(String nombre);

}
