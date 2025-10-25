package utp.edu.pe.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utp.edu.pe.entity.MaterialMontura;
import utp.edu.pe.repository.MaterialMonturaRepository;

@Service
@Transactional
public class MaterialMonturaServiceImpl implements MaterialMonturaService {
	
	   private final MaterialMonturaRepository materialRepository;
	    
	    public MaterialMonturaServiceImpl(MaterialMonturaRepository materialRepository) {
	        this.materialRepository = materialRepository;
	    }

		@Override
		public MaterialMontura save(MaterialMontura materialMontura) {
		    return materialRepository.save(materialMontura);
		}

		@Override
		public List<MaterialMontura> findAll() {
	        return materialRepository.findAll();
		}

		@Override
		public List<MaterialMontura> findAllActive() {
		       return materialRepository.findAllActive();
		}

		@Override
		public Optional<MaterialMontura> findById(Long id) {
	        return materialRepository.findById(id);
		}

		@Override
		public Optional<MaterialMontura> findByNombre(String nombre) {
			   return materialRepository.findByNombre(nombre);
		}

		@Override
		public void deleteById(Long id) {
	        materialRepository.deleteById(id);
			
		}

		@Override
		public boolean existsByNombre(String nombre) {
	        return materialRepository.existsByNombre(nombre);
		}

}
