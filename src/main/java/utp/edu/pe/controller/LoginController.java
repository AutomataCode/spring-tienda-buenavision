package utp.edu.pe.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

	@GetMapping("/login")
    public String mostrarFormularioLogin() {
        
      
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Si la autenticación es válida y no es el "usuario anónimo"
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            	
         
            return "redirect:/public"; 
        }

        // Si no está autenticado, muestra la página de login
        // Esto asume que tu archivo se llama "login.html"
        return "public/login"; 
    }
}
