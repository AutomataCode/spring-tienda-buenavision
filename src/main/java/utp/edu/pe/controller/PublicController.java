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
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.entity.enums.EstadoGeneral;
import utp.edu.pe.entity.enums.Rolx;
import utp.edu.pe.service.UsuarioService;

@Controller
@RequestMapping("/public")
public class PublicController {
	
	@Autowired
    private UsuarioService usuarioService; 

    // Inyecta el codificador de contraseñas
    @Autowired
    private PasswordEncoder passwordEncoder;
	
	@GetMapping
	public String landingPage() {
		
		return "public/landing-page";
	}
	
	@GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
       
        model.addAttribute("usuario", new Usuario());
        return "public/registrar-usuario"; 
    }
	
	@PostMapping("/registro")
    public String procesarRegistro(
            @Valid @ModelAttribute("usuario") Usuario usuario, // Obtiene el usuario y lo valida
            BindingResult bindingResult, // Contiene los resultados de la validación
            @RequestParam("confirmPassword") String confirmPassword, // Obtiene el campo extra
            Model model,
            RedirectAttributes redirectAttributes) {

        // 1. Validaciones de @Valid
        if (bindingResult.hasErrors()) {
            // Si hay errores (ej. campos vacíos, email inválido),
            // vuelve a mostrar el formulario con los mensajes de error.
            return "registro";
        }

        // 2. Validación de Contraseña
        if (!usuario.getPassword().equals(confirmPassword)) {
            // Agrega un error específico al campo 'password'
            bindingResult.rejectValue("password", "error.usuario", "Las contraseñas no coinciden");
            return "registro";
        }

        // 3. Validar duplicados de Username y Email
        if (usuarioService.findByUsername(usuario.getUsername()).isPresent()) {
            bindingResult.rejectValue("username", "error.usuario", "El nombre de usuario ya está en uso");
            return "registro";
        }

        if (usuarioService.findByEmail(usuario.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "error.usuario", "El correo electrónico ya está registrado");
            return "registro";
        }

        // --- Si todas las validaciones pasan ---

        // 4. Cifrar la contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // 5. Establecer valores por defecto
        usuario.setRol(Rolx.CLIENTE); // Rol por defecto para nuevos registros
        usuario.setEstado(EstadoGeneral.ACTIVO); // Estado por defecto
        usuario.setFechaCreacion(LocalDateTime.now());

        // 6. Guardar el usuario
        usuarioService.registrar(usuario); // Asumimos un método 'registrar' o 'save'

        // 7. Redirigir al Login con un mensaje de éxito
        // Usamos RedirectAttributes para pasar mensajes entre redirecciones
        redirectAttributes.addFlashAttribute("registroExitoso", 
            "¡Cuenta creada exitosamente! Ya puedes iniciar sesión.");
        
        return "redirect:/public"; // Redirige al controlador de login
    }
	
	

}
