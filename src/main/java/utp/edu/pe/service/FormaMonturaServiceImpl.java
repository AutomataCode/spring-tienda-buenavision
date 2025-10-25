package utp.edu.pe.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utp.edu.pe.entity.FormaMontura;
import utp.edu.pe.repository.FormaMonturaRepository;


@Service
@Transactional
public class FormaMonturaServiceImpl implements FormaMonturaService{
	
	  private final FormaMonturaRepository formaRepository;
	    
	 public FormaMonturaServiceImpl(FormaMonturaRepository formaRepository) {
	        this.formaRepository = formaRepository;
	}

	@Override
	public FormaMontura save(FormaMontura formaMontura) {
		  return formaRepository.save(formaMontura);
	}

	@Override
	public List<FormaMontura> findAll() {
		 return formaRepository.findAll();
	}

	@Override
	public List<FormaMontura> findAllActive() {
        return formaRepository.findAllActive();
	}

	@Override
	public Optional<FormaMontura> findById(Long id) {
		   return formaRepository.findById(id);
	}

	@Override
	public Optional<FormaMontura> findByNombre(String nombre) {
	      return formaRepository.findByNombre(nombre);
	}

	@Override
	public void deleteById(Long id) {
        formaRepository.deleteById(id);	
	}

	@Override
	public boolean existsByNombre(String nombre) {
        return formaRepository.existsByNombre(nombre);
	}

}
