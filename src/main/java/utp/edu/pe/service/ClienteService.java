package utp.edu.pe.service;

import java.util.List;
import java.util.Optional;

import utp.edu.pe.entity.Cliente;

public interface ClienteService {

	boolean existsByNumeroDocumento(String numeroDocumento);

	Cliente save(Cliente cliente);

	Optional<Cliente> findById(Integer id);

	Optional<Cliente> findByUsuarioId(Integer idUsuario);

	List<Cliente> findAll();

	void deleteById(Integer id);

}
