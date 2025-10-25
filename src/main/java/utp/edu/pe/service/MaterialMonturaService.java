package utp.edu.pe.service;




import java.util.List;
import java.util.Optional;

import utp.edu.pe.entity.MaterialMontura;

public interface MaterialMonturaService {
	
    MaterialMontura save(MaterialMontura materialMontura);
    List<MaterialMontura> findAll();
    List<MaterialMontura> findAllActive();
    Optional<MaterialMontura> findById(Long id);
    Optional<MaterialMontura> findByNombre(String nombre);
    void deleteById(Long id);
    boolean existsByNombre(String nombre);

}
