package utp.edu.pe.entity.enums;

public enum TipoProducto {
    OFTALMICO("Lentes Oftálmicos"),
    SOLAR("Lentes Sol"),
	LENTESCONTACTO("Lentes de contacto"),
	ACCESORIOS("Accesorios");
    private final String descripcion;
    
    TipoProducto(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }

}
