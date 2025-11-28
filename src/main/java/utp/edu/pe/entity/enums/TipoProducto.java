package utp.edu.pe.entity.enums;

public enum TipoProducto {
    MONTURA("MONTURA"),
    SOLAR("LENTES DE SOL"),
    LENTE_CONTACTO("LENTE DE CONTACTO"),
    ACCESORIO("ACCESORIO");
	
    private final String descripcion;
    
    TipoProducto(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }

}
