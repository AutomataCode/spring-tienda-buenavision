package utp.edu.pe.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

import utp.edu.pe.service.CustomUserDetailsService; 

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
	
	private final CustomUserDetailsService userDetailsService;
	private final CustomAuthenticationSuccessHandler successHandler;
	
    public SecurityConfig(CustomUserDetailsService userDetailsService,CustomAuthenticationSuccessHandler successHandler) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
    }
    
    @Autowired
    private CustomLoginFailureHandler failureHandler;

 

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }



    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        http
        	.authorizeHttpRequests(authz -> authz
                // AGREGA "/error" A ESTA LISTA
            	.requestMatchers("/public/**","/catalogo/**","/carrito/**", "/css/**", "/js/**", "/img/**","/webfonts/**", "/login", "/registro", "/error").permitAll()
            	.requestMatchers("/pedido/**").hasAuthority("ROLE_CLIENTE")
            	.requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
            )
            
            .headers(headers -> headers
                
            		// 1. Política de Contenido (CSP) - 
                    .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'self'; " +
                        				  "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                                          "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                                          "font-src 'self' https://fonts.gstatic.com; " +
                                          "img-src 'self' data: https:; " +                    
                                          "connect-src 'self' https://cdn.jsdelivr.net; " +     // Para llamadas AJAX
                                          "object-src 'none'; " +       // Bloquea plugins/Flash (Recomendado)
                                          "base-uri 'self'; " +         // SOLUCIÓN ALERTA ZAP
                                          "form-action 'self'; " +      // SOLUCIÓN ALERTA ZAP
                                          "frame-ancestors 'none';")    // Refuerza el bloqueo de iframes
                    )
                    // 2. Anti-Clickjacking (X-Frame-Options)
                    .frameOptions(frame -> frame
                        .deny()
                    )
                    // 3. Protección MIME (CORREGIDO)
                    // Usamos withDefaults() porque 'nosniff' ya es el valor predeterminado
                    .contentTypeOptions(Customizer.withDefaults()) 
                )
            
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .authenticationProvider(authenticationProvider)
            .formLogin(form -> form
                    .loginPage("/login")
                    .successHandler(successHandler)
                    .failureHandler(failureHandler)// 
                    .permitAll()
                )
                .logout(logout -> logout
                    .logoutUrl("/logout")
                    .invalidateHttpSession(true)
                    .logoutSuccessUrl("/login?logout") 
                    .permitAll()
                );

   

        return http.build();
    }
}