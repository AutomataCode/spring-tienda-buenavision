package utp.edu.pe.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.repository.UsuarioRepository;
import utp.edu.pe.service.UsuarioService;
import utp.edu.pe.service.UsuarioServiceImpl;

import java.io.IOException;
import java.util.Optional;

@Component
public class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String username = request.getParameter("username"); // Obtiene el usuario del form
        
        // Buscamos al usuario en BD
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username); // O findByEmail según uses

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // Si la cuenta ya está activa y no bloqueada
            if (usuario.isEnabled() && usuario.isAccountNonLocked()) {
                if (usuario.getIntentosFallidos() < UsuarioServiceImpl.MAX_INTENTOS_FALLIDOS - 1) {
                    // Aumentar contador
                    usuarioService.aumentarIntentosFallidos(usuario);
                } else {
                    // Bloquear cuenta
                    usuarioService.bloquear(usuario);
                    exception = new LockedException("Tu cuenta ha sido bloqueada por múltiples intentos fallidos.");
                }
            } else if (!usuario.isAccountNonLocked()) {
                 exception = new LockedException("Tu cuenta está bloqueada. Contacta al soporte.");
            }
        }

        // Redirigir al login con error
        // Usamos super.setDefaultFailureUrl para mandar el mensaje correcto
        if (exception instanceof LockedException) {
             // Puedes crear un parámetro especial ?error=bloqueado
             super.setDefaultFailureUrl("/login?error=bloqueado"); 
        } else {
             super.setDefaultFailureUrl("/login?error=true");
        }

        super.onAuthenticationFailure(request, response, exception);
    }
}