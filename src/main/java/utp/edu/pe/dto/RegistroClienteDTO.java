package utp.edu.pe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import utp.edu.pe.entity.enums.TipoDocumento;

public class RegistroClienteDTO {
	

    @NotEmpty(message = "El nombre de usuario es obligatorio")
    @Size(min = 4, max = 50, message = "El usuario debe tener entre 4 y 50 caracteres")
    private String username;

    @NotEmpty(message = "El correo electrónico es obligatorio")
    @Email(message = "Debe ser un correo válido")
    private String email;

    @NotEmpty(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotEmpty(message = "Debes confirmar la contraseña")
    private String confirmPassword;

    // --- Datos de Cliente ---
    @NotEmpty(message = "El nombre es obligatorio")
    private String nombre; 

    @NotEmpty(message = "El apellido es obligatorio")
    private String apellido; 

    private TipoDocumento tipoDocumento = TipoDocumento.DNI; 

    @NotEmpty(message = "El número de documento es obligatorio")
    private String numeroDocumento;

    private String telefono;
    private String direccion;
	public RegistroClienteDTO(
			@NotEmpty(message = "El nombre de usuario es obligatorio") @Size(min = 4, max = 50, message = "El usuario debe tener entre 4 y 50 caracteres") String username,
			@NotEmpty(message = "El correo electrónico es obligatorio") @Email(message = "Debe ser un correo válido") String email,
			@NotEmpty(message = "La contraseña es obligatoria") @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String password,
			@NotEmpty(message = "Debes confirmar la contraseña") String confirmPassword,
			@NotEmpty(message = "El nombre es obligatorio") String nombre,
			@NotEmpty(message = "El apellido es obligatorio") String apellido, TipoDocumento tipoDocumento,
			@NotEmpty(message = "El número de documento es obligatorio") String numeroDocumento, String telefono,
			String direccion) {
		super();
		this.username = username;
		this.email = email;
		this.password = password;
		this.confirmPassword = confirmPassword;
		this.nombre = nombre;
		this.apellido = apellido;
		this.tipoDocumento = tipoDocumento;
		this.numeroDocumento = numeroDocumento;
		this.telefono = telefono;
		this.direccion = direccion;
	}
	public RegistroClienteDTO() {
		super();
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getConfirmPassword() {
		return confirmPassword;
	}
	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getApellido() {
		return apellido;
	}
	public void setApellido(String apellido) {
		this.apellido = apellido;
	}
	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}
	public void setTipoDocumento(TipoDocumento tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}
	public String getNumeroDocumento() {
		return numeroDocumento;
	}
	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}
	public String getTelefono() {
		return telefono;
	}
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	public String getDireccion() {
		return direccion;
	}
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

    
}
