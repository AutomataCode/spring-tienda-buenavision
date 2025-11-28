package utp.edu.pe.entity.enums;

public enum Genero {
		HOMBRE("Hombre"),
	    MUJER("Mujer"),
	    UNISEX("Unisex"),
		KIDS("Kids");	
	    private final String descripcion;
	    
	    Genero(String descripcion) {
	        this.descripcion = descripcion;
	    }
	    
	    public String getDescripcion() {
	        return descripcion;
	    }

}
