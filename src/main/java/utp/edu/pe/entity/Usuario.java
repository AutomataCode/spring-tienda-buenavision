package utp.edu.pe.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import utp.edu.pe.entity.enums.EstadoGeneral;
import utp.edu.pe.entity.enums.Rolx;


@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {
	   @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Integer idUsuario;

	    @Column(unique = true, nullable = false, length = 50)
	    private String username;

	    @Column(nullable = false, length = 255)
	    private String password;

	    @Enumerated(EnumType.STRING)
	    @Column(nullable = false)
	    private Rolx rol;

	    @Column(nullable = false, length = 200)
	    private String nombreCompleto;

	    @Column(unique = true, length = 100)
	    private String email;

	    @Enumerated(EnumType.STRING)
	    private EstadoGeneral estado;

	    @Column(name = "fecha_creacion")
	    private LocalDateTime fechaCreacion;

	    @Column(name = "ultimo_login")
	    private LocalDateTime ultimoLogin;

	    @Column(name = "reset_token")
	    private String resetToken;

	    @Column(name = "token_expira")
	    private LocalDateTime tokenExpira;

	    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
	    private Cliente cliente;

	    // --- Constructores ---
	    public Usuario() {
	        super();
	    }

	 


	    public Usuario(Integer idUsuario, String username, String password, Rolx rol, String nombreCompleto,
				String email, EstadoGeneral estado, LocalDateTime fechaCreacion, LocalDateTime ultimoLogin,
				String resetToken, LocalDateTime tokenExpira, Cliente cliente) {
			super();
			this.idUsuario = idUsuario;
			this.username = username;
			this.password = password;
			this.rol = rol;
			this.nombreCompleto = nombreCompleto;
			this.email = email;
			this.estado = estado;
			this.fechaCreacion = fechaCreacion;
			this.ultimoLogin = ultimoLogin;
			this.resetToken = resetToken;
			this.tokenExpira = tokenExpira;
			this.cliente = cliente;
		}




		public EstadoGeneral getEstado() {
			return estado;
		}




		public void setEstado(EstadoGeneral estado) {
			this.estado = estado;
		}




		@Override
	    public Collection<? extends GrantedAuthority> getAuthorities() {
	        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
	    }

	 
	    @Override
	    public String getPassword() {
	        return this.password;
	    }

	
	    @Override
	    public String getUsername() {
	        return this.username;
	    }

	    @Override
	    public boolean isAccountNonExpired() {
	        return true; //puedes manejar lógica de expiración
	    }

	    @Override
	    public boolean isAccountNonLocked() {
	        return true; // puedes manejar lógica de bloqueo
	    }

	    @Override
	    public boolean isCredentialsNonExpired() {
	        return true; // manejar lógica de expiración de credenciales
	    }

	    @Override
	    public boolean isEnabled() {
	       
	        return this.estado == EstadoGeneral.ACTIVO;
	    }

	

	    public Integer getIdUsuario() {
	        return idUsuario;
	    }

	    public void setIdUsuario(Integer idUsuario) {
	        this.idUsuario = idUsuario;
	    }

	    public Rolx getRol() {
	        return rol;
	    }

	    public void setRol(Rolx rol) {
	        this.rol = rol;
	    }

	    public String getNombreCompleto() {
	        return nombreCompleto;
	    }

	    public void setNombreCompleto(String nombreCompleto) {
	        this.nombreCompleto = nombreCompleto;
	    }

	    public String getEmail() {
	        return email;
	    }

	    public void setEmail(String email) {
	        this.email = email;
	    }

	  

	    public LocalDateTime getFechaCreacion() {
	        return fechaCreacion;
	    }

	    public void setFechaCreacion(LocalDateTime fechaCreacion) {
	        this.fechaCreacion = fechaCreacion;
	    }

	    public LocalDateTime getUltimoLogin() {
	        return ultimoLogin;
	    }

	    public void setUltimoLogin(LocalDateTime ultimoLogin) {
	        this.ultimoLogin = ultimoLogin;
	    }

	    public String getResetToken() {
	        return resetToken;
	    }

	    public void setResetToken(String resetToken) {
	        this.resetToken = resetToken;
	    }

	    public LocalDateTime getTokenExpira() {
	        return tokenExpira;
	    }

	    public void setTokenExpira(LocalDateTime tokenExpira) {
	        this.tokenExpira = tokenExpira;
	    }

	    public Cliente getCliente() {
	        return cliente;
	    }

	    public void setCliente(Cliente cliente) {
	        this.cliente = cliente;
	    }

	    public void setUsername(String username) {
	        this.username = username;
	    }

	    public void setPassword(String password) {
	        this.password = password;
	    }
	
	
	
	
}





