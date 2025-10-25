package utp.edu.pe.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.repository.UsuarioRepository;


@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

	    
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	    Usuario usuario = usuarioRepository.findByUsername(username)
	            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
	    
	    return usuario;
	}
	
	public void actualizarUltimoLogin(Integer idUsuario) {
	    usuarioRepository.findById(idUsuario).ifPresent(usuario -> {
	        usuario.setUltimoLogin(LocalDateTime.now());
	        usuarioRepository.save(usuario);
	    });
	}
}
