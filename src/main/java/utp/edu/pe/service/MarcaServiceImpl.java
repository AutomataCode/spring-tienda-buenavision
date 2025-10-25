package utp.edu.pe.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utp.edu.pe.entity.Marca;
import utp.edu.pe.entity.enums.EstadoGeneral;
import utp.edu.pe.repository.MarcaRepository;


@Service
@Transactional
public class MarcaServiceImpl implements MarcaService {
	
	  private final MarcaRepository marcaRepository;
	    
	    public MarcaServiceImpl(MarcaRepository marcaRepository) {
	        this.marcaRepository = marcaRepository;
	    }

	@Override
	public Marca save(Marca marca) {
		return marcaRepository.save(marca);
	}

	@Override
	public List<Marca> findAll() {
		 return marcaRepository.findAll();
	}

	@Override
	public List<Marca> findAllActive() {
		return marcaRepository.findAllActive();
	}

	@Override
	public Optional<Marca> findById(Long id) {
	       return marcaRepository.findById(id);
	}

	@Override
	public Optional<Marca> findByNombre(String nombre) {
        return marcaRepository.findByNombre(nombre);
	}

	@Override
	public void deleteById(Long id) {
        marcaRepository.deleteById(id);
	}

	@Override
	public void deactivate(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada"));
            marca.setEstado(EstadoGeneral.INACTIVO);
            marcaRepository.save(marca);
		
	}

	@Override
	public boolean existsByNombre(String nombre) {
		 return marcaRepository.existsByNombre(nombre);
	}

}
