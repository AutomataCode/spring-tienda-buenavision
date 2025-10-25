package utp.edu.pe.entity.enums;

public enum EstadoGeneral {
	 	ACTIVO("Activo"),
	    INACTIVO("Inactivo");
	    
	    private final String descripcion;
	    
	    EstadoGeneral(String descripcion) {
	        this.descripcion = descripcion;
	    }
	    
	    public String getDescripcion() {
	        return descripcion;
	    }
}
