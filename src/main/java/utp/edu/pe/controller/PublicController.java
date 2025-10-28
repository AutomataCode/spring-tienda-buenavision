package utp.edu.pe.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import utp.edu.pe.dto.RegistroClienteDTO;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.entity.enums.EstadoGeneral;
import utp.edu.pe.entity.enums.Rolx;
import utp.edu.pe.service.ClienteService;
import utp.edu.pe.service.UsuarioService;

@Controller
@RequestMapping("/public")
public class PublicController {
	
	@Autowired
    private UsuarioService usuarioService; 
	
	@Autowired
	private ClienteService clienteService;

  
    @Autowired
    private PasswordEncoder passwordEncoder;
	
	@GetMapping
	public String landingPage() {
		
		return "public/landing-page";
	}
	
	@GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
       
        model.addAttribute("registroDto", new RegistroClienteDTO());
        return "public/registrar-usuario"; 
    }
	
	@PostMapping("/registro")
    public String procesarRegistro(
            @Valid @ModelAttribute("registroDto") RegistroClienteDTO dto, // ⬅️ Usa el DTO
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

   
        if (bindingResult.hasErrors()) {
        	return "public/registrar-usuario"; 
        }

        //  Validación de Contraseña
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.dto", "Las contraseñas no coinciden");
            return "public/registrar-usuario"; 
        }

        //  Validar duplicados (usa los métodos del servicio)
        if (usuarioService.existsByUsername(dto.getUsername())) {
            bindingResult.rejectValue("username", "error.dto", "El nombre de usuario ya está en uso");
            return "public/registrar-usuario"; 
        }

        if (usuarioService.existsByEmail(dto.getEmail())) {
            bindingResult.rejectValue("email", "error.dto", "El correo electrónico ya está registrado");
            return "public/registrar-usuario"; 
        }
        
        
        if (clienteService.existsByNumeroDocumento(dto.getNumeroDocumento())) {
           bindingResult.rejectValue("numeroDocumento", "error.dto", "El documento ya está registrado");
           return "public/registrar-usuario"; 
        }

       

        //  Llamar al servicio para registrar AMBOS
        usuarioService.registrarCliente(dto);

        // Redirigir al Login
        redirectAttributes.addFlashAttribute("registroExitoso", 
            "¡Cuenta creada exitosamente! Ya puedes iniciar sesión.");
        
        return "redirect:/login";
    }
}