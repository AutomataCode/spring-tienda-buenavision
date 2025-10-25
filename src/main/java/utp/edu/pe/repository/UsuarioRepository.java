package utp.edu.pe.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import utp.edu.pe.entity.Usuario;
import utp.edu.pe.entity.enums.EstadoGeneral;

public interface UsuarioRepository  extends JpaRepository<Usuario, Integer>  {
	Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);
    
    Optional<Usuario> findByResetToken(String resetToken);
    
 
    List<Usuario> findByEstado(EstadoGeneral estado); 
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

}
