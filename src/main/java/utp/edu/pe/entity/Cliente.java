package utp.edu.pe.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import utp.edu.pe.entity.enums.EstadoGeneral;

@Entity
@Table(name = "cliente")
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCliente;
    
    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
    
    @Enumerated(EnumType.STRING)
    private TipoDocumento tipoDocumento;
    
    @Column(unique = true, length = 20)
    private String numeroDocumento;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(length = 100)
    private String apellido;
    
    @Column(length = 15)
    private String telefono;
    
    private String direccion;
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
    
    @Enumerated(EnumType.STRING)
    private EstadoGeneral estado;
    
    

	public Cliente() {
		super();
	}



	public Cliente(Integer idCliente, Usuario usuario, TipoDocumento tipoDocumento, String numeroDocumento,
			String nombre, String apellido, String telefono, String direccion, LocalDateTime fechaRegistro,
			EstadoGeneral estado) {
		super();
		this.idCliente = idCliente;
		this.usuario = usuario;
		this.tipoDocumento = tipoDocumento;
		this.numeroDocumento = numeroDocumento;
		this.nombre = nombre;
		this.apellido = apellido;
		this.telefono = telefono;
		this.direccion = direccion;
		this.fechaRegistro = fechaRegistro;
		this.estado = estado;
	}



	public Integer getIdCliente() {
		return idCliente;
	}



	public void setIdCliente(Integer idCliente) {
		this.idCliente = idCliente;
	}



	public Usuario getUsuario() {
		return usuario;
	}



	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
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



	public LocalDateTime getFechaRegistro() {
		return fechaRegistro;
	}



	public void setFechaRegistro(LocalDateTime fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}



	public EstadoGeneral getEstado() {
		return estado;
	}



	public void setEstado(EstadoGeneral estado) {
		this.estado = estado;
	}
    
    
    
    
}

enum TipoDocumento {
    DNI, RUC, CE
}