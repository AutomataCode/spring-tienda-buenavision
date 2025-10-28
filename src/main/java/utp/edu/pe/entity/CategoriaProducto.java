package utp.edu.pe.entity;

import jakarta.persistence.*;
import utp.edu.pe.entity.enums.EstadoGeneral;
import utp.edu.pe.entity.enums.TipoProducto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categoria_producto")
public class CategoriaProducto {
	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "id_categoria")
	    private Long idCategoria;
	    
	    @Column(name = "nombre", unique = true, nullable = false, length = 100)
	    private String nombre;
	    
	    @Enumerated(EnumType.STRING)
	    @Column(name = "tipo")
	    private TipoProducto tipo;
	    
	    @Column(name = "descripcion", columnDefinition = "TEXT")
	    private String descripcion;
	    
	    @Enumerated(EnumType.STRING)
	    @Column(name = "estado")
	    private EstadoGeneral estado = EstadoGeneral.ACTIVO;
	    
	    @Column(name = "fecha_creacion")
	    private LocalDateTime fechaCreacion;
	    
	    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
	    private List<Producto> productos = new ArrayList<>();
	    
 
	    public CategoriaProducto() {
	        this.fechaCreacion = LocalDateTime.now();
	    }
	    
 
	    public Long getIdCategoria() { return idCategoria; }
	    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }
	    
	    public String getNombre() { return nombre; }
	    public void setNombre(String nombre) { this.nombre = nombre; }
	    
	    public TipoProducto getTipo() { return tipo; }
	    public void setTipo(TipoProducto tipo) { this.tipo = tipo; }
	    
	    public String getDescripcion() { return descripcion; }
	    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
	    
	    public EstadoGeneral getEstado() { return estado; }
	    public void setEstado(EstadoGeneral estado) { this.estado = estado; }
	    
	    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
	    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
	    
	    public List<Producto> getProductos() { return productos; }
	    public void setProductos(List<Producto> productos) { this.productos = productos; }
	    

}
