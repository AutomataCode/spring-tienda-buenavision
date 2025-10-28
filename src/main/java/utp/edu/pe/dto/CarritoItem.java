package utp.edu.pe.dto;
import utp.edu.pe.entity.Producto;
import java.math.BigDecimal;
import java.util.Objects;
public class CarritoItem {
	
	private Producto producto;
    private int cantidad;
    private BigDecimal precioUnitario; // Precio al momento de añadir

    public CarritoItem(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecioVenta(); // Captura el precio actual
    }

    // Getters y Setters
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

  
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarritoItem that = (CarritoItem) o;
        return Objects.equals(producto.getIdProducto(), that.producto.getIdProducto());
    }

    @Override
    public int hashCode() {
        return Objects.hash(producto.getIdProducto());
    }

}
