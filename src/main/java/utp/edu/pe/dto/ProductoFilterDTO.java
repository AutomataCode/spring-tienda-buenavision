package utp.edu.pe.dto;



import java.math.BigDecimal;

import utp.edu.pe.entity.enums.TipoProducto;

public class ProductoFilterDTO {
	
	private String nombre;
	
    private String genero;
    private Long marcaId;
    private Long categoriaId;
    private Long formaId;
    private Long materialId;
    private BigDecimal minPrecio;
    private BigDecimal maxPrecio;
    private String searchTerm;
    private TipoProducto tipo;
  
    public ProductoFilterDTO() {}
  
    
 
    




	public ProductoFilterDTO(String nombre, String genero, Long marcaId, Long categoriaId, Long formaId,
			Long materialId, BigDecimal minPrecio, BigDecimal maxPrecio, String searchTerm, TipoProducto tipo) {
		super();
		this.nombre = nombre;
		this.genero = genero;
		this.marcaId = marcaId;
		this.categoriaId = categoriaId;
		this.formaId = formaId;
		this.materialId = materialId;
		this.minPrecio = minPrecio;
		this.maxPrecio = maxPrecio;
		this.searchTerm = searchTerm;
		this.tipo = tipo;
	}








	public String getNombre() {
		return nombre;
	}








	public void setNombre(String nombre) {
		this.nombre = nombre;
	}








	public TipoProducto getTipo() {
		return tipo;
	}




	public void setTipo(TipoProducto tipo) {
		this.tipo = tipo;
	}




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
    
   
    public boolean hasFiltros() {
        return tipo != null || genero != null || marcaId != null || 
               categoriaId != null || formaId != null || materialId != null ||
               minPrecio != null || maxPrecio != null || 
               (searchTerm != null && !searchTerm.trim().isEmpty());
    }
}
