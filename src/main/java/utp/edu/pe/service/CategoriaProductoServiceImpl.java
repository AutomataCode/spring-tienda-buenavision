package utp.edu.pe.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utp.edu.pe.entity.CategoriaProducto;
import utp.edu.pe.entity.enums.EstadoGeneral;
import utp.edu.pe.entity.enums.TipoProducto;
import utp.edu.pe.repository.CategoriaProductoRepository;



@Service
@Transactional
public class CategoriaProductoServiceImpl implements CategoriaProductoService {
	
	
		private final CategoriaProductoRepository categoriaRepository;
		
		public CategoriaProductoServiceImpl(CategoriaProductoRepository categoriaRepository	) {
			
			this.categoriaRepository = categoriaRepository;
		}
		
		

	@Override
	public CategoriaProducto save(CategoriaProducto categoria) {
		return categoriaRepository.save(categoria);
	}

	@Override
	public List<CategoriaProducto> findAll() {
		return categoriaRepository.findAll();
	}

	@Override
	public List<CategoriaProducto> findAllActive() {
		return categoriaRepository.findAllActive();
	}

	@Override
	public Optional<CategoriaProducto> findById(Long id) {
		return categoriaRepository.findById(id);
	}

	@Override
	public Optional<CategoriaProducto> findByNombre(String nombre) {
		return categoriaRepository.findByNombre(nombre);
	}

	@Override
	public List<CategoriaProducto> findByTipo(TipoProducto tipo) {
		return categoriaRepository.findByTipo(tipo);
	}

	@Override
	public List<CategoriaProducto> findByTipoAndActive(TipoProducto tipo) {
		return categoriaRepository.findByTipoAndEstado(tipo, EstadoGeneral.ACTIVO);
	}

	@Override
	public void deleteById(Long id) {
		categoriaRepository.deleteById(id);
	}

	@Override
	public void deactivate(Long id) {
		
		CategoriaProducto categoria=categoriaRepository.findById(id).
				orElseThrow(()-> new RuntimeException("Categoria no encontrada"));
		
		 categoria.setEstado(EstadoGeneral.INACTIVO);
		 categoriaRepository.save(categoria);
		 
	        
	}

	@Override
	public boolean existsByNombre(String nombre) {
		return categoriaRepository.existsByNombre(nombre);
	}

}
