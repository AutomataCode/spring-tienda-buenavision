package utp.edu.pe.entity.enums;

public enum TipoDocumento {
	DNI("DNI"),
    RUC("RUC"),
	CE("CARNET_EXTRANJERIA");
	
	 private final String descripcion;
	    
	 TipoDocumento(String descripcion) {
	        this.descripcion = descripcion;
	    }
	    
	    public String getDescripcion() {
	        return descripcion;
	    }

}
