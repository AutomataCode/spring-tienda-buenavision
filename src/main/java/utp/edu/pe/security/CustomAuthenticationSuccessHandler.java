package utp.edu.pe.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.service.CustomUserDetailsService;


@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		// 1. ACTUALIZAR ÚLTIMO LOGIN
		// (Esta es la lógica que movimos del LoginSuccessListener)
		Usuario usuario = (Usuario) authentication.getPrincipal();
		userDetailsService.actualizarUltimoLogin(usuario.getIdUsuario());

		// 2. LÓGICA DE REDIRECCIÓN POR ROL
		String targetUrl = determineTargetUrl(authentication);

		if (response.isCommitted()) {
			return;
		}

		// Redirige al usuario a la URL determinada
		response.sendRedirect(request.getContextPath() + targetUrl);
	}

	protected String determineTargetUrl(Authentication authentication) {

		// URL por defecto si no es Admin ni Vendedor (ej. CLIENTE)
		String targetUrl = "/public";

		// Itera sobre los "roles" (authorities) del usuario
		for (GrantedAuthority authority : authentication.getAuthorities()) {

			// Spring Security agrega el prefijo "ROLE_"
			if (authority.getAuthority().equals("ROLE_ADMIN")) {
				targetUrl = "/admin/dashboard"; // ⬅️ Tu página de inicio para Admin
				break; // Termina el bucle
			} else if (authority.getAuthority().equals("ROLE_VENDEDOR")) {
				targetUrl = "/vendedor/home"; // ⬅️ Tu página de inicio para Vendedor
				break; // Termina el bucle
			}
		}

		return targetUrl;
	}

}
