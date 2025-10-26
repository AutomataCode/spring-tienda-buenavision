package utp.edu.pe.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import utp.edu.pe.entity.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
	
	boolean existsByNumeroDocumento(String numeroDocumento);

	Optional<Cliente> findByUsuario_IdUsuario(Integer idUsuario);

}
