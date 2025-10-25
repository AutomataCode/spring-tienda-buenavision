package utp.edu.pe.service;

import java.util.List;
import java.util.Optional;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utp.edu.pe.entity.Usuario;
import utp.edu.pe.entity.enums.EstadoGeneral;
import utp.edu.pe.repository.UsuarioRepository;


@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService{
	
	private final UsuarioRepository usuarioRepository;
	
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
	@Override
    @Transactional(readOnly = true) 
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    public Usuario registrar(Usuario usuario) {
        // La lógica de negocio (codificar contraseña, setear rol)
        // se maneja en el controlador por ahora.
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByResetToken(String resetToken) {
        return usuarioRepository.findByResetToken(resetToken);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findByEstado(EstadoGeneral estado) {
        return usuarioRepository.findByEstado(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Integer id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Usuario actualizar(Usuario usuario) {
        // Este método simplemente guarda.
        // La lógica para actualizar (ej. verificar si la contraseña cambió)
        // debe manejarse en el controlador o en un método de servicio más complejo.
        return usuarioRepository.save(usuario);
    }

    @Override
    public void deleteById(Integer id) {
        usuarioRepository.deleteById(id);
    }
}


