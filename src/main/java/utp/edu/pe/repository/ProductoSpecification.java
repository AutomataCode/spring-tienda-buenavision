package utp.edu.pe.repository;


import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import utp.edu.pe.dto.ProductoFilterDTO;
import utp.edu.pe.entity.Producto;
import utp.edu.pe.entity.enums.EstadoProducto;

import java.util.ArrayList;
import java.util.List;


public class ProductoSpecification implements Specification<Producto> {
	
	private final ProductoFilterDTO filter;
	
	public ProductoSpecification(ProductoFilterDTO filter) {
		this.filter=filter;
	}

	@Override
	public Predicate toPredicate(Root<Producto> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		List<Predicate> predicates = new ArrayList<>();

        //  productos ACTIVOS ---
        predicates.add(cb.equal(root.get("estado"), EstadoProducto.ACTIVO));

        // --- Filtros Dinámicos ---

        // Filtro por Tipo  
        if (filter.getTipo() != null) {
            predicates.add(cb.equal(root.get("tipo"), filter.getTipo()));
        }

        // Filtro por Marca
        if (filter.getMarcaId() != null) {
 
            predicates.add(cb.equal(root.get("marca").get("idMarca"), filter.getMarcaId()));
        }

        // Filtro por Forma
        if (filter.getFormaId() != null) {
 
            predicates.add(cb.equal(root.get("forma").get("idForma"), filter.getFormaId()));
        }

        // Filtro por Material
        if (filter.getMaterialId() != null) {
 
            predicates.add(cb.equal(root.get("material").get("idMaterial"), filter.getMaterialId()));
        }

        if (filter.getNombre() != null && !filter.getNombre().isEmpty()) {
            // Buscamos ignorando mayúsculas/minúsculas (LIKE %texto%)
            predicates.add(cb.like(cb.lower(root.get("nombre")), "%" + filter.getNombre().toLowerCase() + "%"));
        }
        
        return cb.and(predicates.toArray(new Predicate[0]));
	}

}
