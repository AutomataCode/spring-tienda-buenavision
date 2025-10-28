package utp.edu.pe.dto;



import java.math.BigDecimal;

public class ProductoFilterDTO {
    private String tipo;
    private String genero;
    private Long marcaId;
    private Long categoriaId;
    private Long formaId;
    private Long materialId;
    private BigDecimal minPrecio;
    private BigDecimal maxPrecio;
    private String searchTerm;
    
    
    public ProductoFilterDTO() {}
    
    public ProductoFilterDTO(String tipo, String genero, Long marcaId, Long categoriaId, 
                            Long formaId, Long materialId, BigDecimal minPrecio, BigDecimal maxPrecio) {
        this.tipo = tipo;
        this.genero = genero;
        this.marcaId = marcaId;
        this.categoriaId = categoriaId;
        this.formaId = formaId;
        this.materialId = materialId;
        this.minPrecio = minPrecio;
        this.maxPrecio = maxPrecio;
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    
    public Long getMarcaId() { return marcaId; }
    public void setMarcaId(Long marcaId) { this.marcaId = marcaId; }
    
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
    
    public Long getFormaId() { return formaId; }
    public void setFormaId(Long formaId) { this.formaId = formaId; }
    
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    
    public BigDecimal getMinPrecio() { return minPrecio; }
    public void setMinPrecio(BigDecimal minPrecio) { this.minPrecio = minPrecio; }
    
    public BigDecimal getMaxPrecio() { return maxPrecio; }
    public void setMaxPrecio(BigDecimal maxPrecio) { this.maxPrecio = maxPrecio; }
    
    public String getSearchTerm() { return searchTerm; }
    public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }
    
    // Métodos utilitarios
    public boolean hasFiltros() {
        return tipo != null || genero != null || marcaId != null || 
               categoriaId != null || formaId != null || materialId != null ||
               minPrecio != null || maxPrecio != null || 
               (searchTerm != null && !searchTerm.trim().isEmpty());
    }
}
