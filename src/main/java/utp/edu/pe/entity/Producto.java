package utp.edu.pe.entity;

import jakarta.persistence.*;
import utp.edu.pe.entity.enums.EstadoProducto;
import utp.edu.pe.entity.enums.Genero;
import utp.edu.pe.entity.enums.TallaMontura;
import utp.edu.pe.entity.enums.TipoProducto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto")
public class Producto {
	

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;
    
    @Column(name = "sku", unique = true, nullable = false, length = 50)
    private String sku;
    
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    // Relación con Categoría
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria", nullable = false)
    private CategoriaProducto categoria;
    
    // Relación con Marca
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_marca", nullable = false)
    private Marca marca;
    
    // Relación con Forma
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_forma")
    private FormaMontura forma;
    
    // Relación con Material
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_material")
    private MaterialMontura material;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoProducto tipo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "genero")
    private Genero genero;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "talla")
    private TallaMontura talla;
    
    @Column(name = "color", length = 50)
    private String color;
    
    @Column(name = "modelo", length = 100)
    private String modelo;
    
    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;
    
    @Column(name = "precio_costo", precision = 10, scale = 2)
    private BigDecimal precioCosto;
    
    @Column(name = "stock_actual")
    private Integer stockActual = 0;
    
    @Column(name = "stock_minimo")
    private Integer stockMinimo = 5;
    
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;
    
    @Column(name = "fecha_ingreso")
    private LocalDateTime fechaIngreso;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoProducto estado = EstadoProducto.ACTIVO;
    
    // Constructores
    public Producto() {
        this.fechaIngreso = LocalDateTime.now();
    }
    
    public Producto(String sku, String nombre, CategoriaProducto categoria, 
                   Marca marca, TipoProducto tipo, BigDecimal precioVenta) {
        this();
        this.sku = sku;
        this.nombre = nombre;
        this.categoria = categoria;
        this.marca = marca;
        this.tipo = tipo;
        this.precioVenta = precioVenta;
    }
    
    // Getters y Setters
    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }
    
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public CategoriaProducto getCategoria() { return categoria; }
    public void setCategoria(CategoriaProducto categoria) { this.categoria = categoria; }
    
    public Marca getMarca() { return marca; }
    public void setMarca(Marca marca) { this.marca = marca; }
    
    public FormaMontura getForma() { return forma; }
    public void setForma(FormaMontura forma) { this.forma = forma; }
    
    public MaterialMontura getMaterial() { return material; }
    public void setMaterial(MaterialMontura material) { this.material = material; }
    
    public TipoProducto getTipo() { return tipo; }
    public void setTipo(TipoProducto tipo) { this.tipo = tipo; }
    
    public Genero getGenero() { return genero; }
    public void setGenero(Genero genero) { this.genero = genero; }
    
    public TallaMontura getTalla() { return talla; }
    public void setTalla(TallaMontura talla) { this.talla = talla; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }
    
    public BigDecimal getPrecioCosto() { return precioCosto; }
    public void setPrecioCosto(BigDecimal precioCosto) { this.precioCosto = precioCosto; }
    
    public Integer getStockActual() { return stockActual; }
    public void setStockActual(Integer stockActual) { this.stockActual = stockActual; }
    
    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }
    
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    
    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    
    public EstadoProducto getEstado() { return estado; }
    public void setEstado(EstadoProducto estado) { this.estado = estado; }
    
    // Métodos de negocio
    public boolean tieneStockSuficiente(Integer cantidad) {
        return this.stockActual >= cantidad;
    }
    
    public boolean tieneStockBajo() {
        return this.stockActual <= this.stockMinimo;
    }
    
    public void reducirStock(Integer cantidad) {
        if (this.stockActual >= cantidad) {
            this.stockActual -= cantidad;
        } else {
            throw new IllegalStateException("Stock insuficiente");
        }
    }
    
    public void aumentarStock(Integer cantidad) {
        this.stockActual += cantidad;
    }
	

}
