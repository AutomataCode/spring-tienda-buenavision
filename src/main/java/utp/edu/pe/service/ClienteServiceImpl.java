package utp.edu.pe.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utp.edu.pe.entity.Cliente;
import utp.edu.pe.repository.ClienteRepository;


@Service
public class ClienteServiceImpl implements ClienteService {
	
	private final ClienteRepository clienteRepository;

    @Autowired
    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

	@Override
	public boolean existsByNumeroDocumento(String numeroDocumento) {
		return clienteRepository.existsByNumeroDocumento(numeroDocumento);
   
	}

	@Override
	public Cliente save(Cliente cliente) {
		return clienteRepository.save(cliente);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Cliente> findById(Integer id) {

		return clienteRepository.findById(id);
	}

	@Override
	public Optional<Cliente> findByUsuarioId(Integer idUsuario) {
		return clienteRepository.findByUsuario_IdUsuario(idUsuario);
	}

	@Override
	public List<Cliente> findAll() {
		return clienteRepository.findAll();
	}

	@Override
	public void deleteById(Integer id) {
		clienteRepository.deleteById(id);
		
	}

}
