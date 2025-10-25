package utp.edu.pe.entity.enums;

public enum TallaMontura {
    PEQUEÑA("Pequeña"),
    MEDIANA("Mediana"),
    GRANDE("Grande");
    
    private final String descripcion;
    
    TallaMontura(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }

}
