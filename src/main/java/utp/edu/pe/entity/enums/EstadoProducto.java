package utp.edu.pe.entity.enums;

public enum EstadoProducto {
	ACTIVO("Activo"),
    INACTIVO("Inactivo"),
	AGOTADO("Inactivo");
    private final String descripcion;
    
    EstadoProducto(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}
