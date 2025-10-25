package utp.edu.pe.entity.enums;

public enum Rolx {
	
	ADMIN("ADMIN"),
	CLIENTE("CLIENTE");
    
    private final String descripcion;
    
    Rolx(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }

	
}
