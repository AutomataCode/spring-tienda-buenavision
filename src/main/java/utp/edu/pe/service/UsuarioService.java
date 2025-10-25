package utp.edu.pe.service;

import java.util.List;
import java.util.Optional;

import utp.edu.pe.entity.Usuario;
import utp.edu.pe.entity.enums.EstadoGeneral;

public interface UsuarioService {
	
	
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
  
    Usuario registrar(Usuario usuario);


    Optional<Usuario> findByResetToken(String resetToken);
    

    List<Usuario> findByEstado(EstadoGeneral estado);
    

    List<Usuario> findAll();
    Optional<Usuario> findById(Integer id);
    Usuario actualizar(Usuario usuario);
    void deleteById(Integer id);

}
