package utp.edu.pe.controller;

import utp.edu.pe.entity.*;
import utp.edu.pe.entity.enums.EstadoGeneral;
import utp.edu.pe.entity.enums.EstadoProducto;
import utp.edu.pe.entity.enums.Genero;
import utp.edu.pe.entity.enums.TallaMontura;
import utp.edu.pe.entity.enums.TipoProducto;
import utp.edu.pe.service.*;
import utp.edu.pe.dto.ProductoFilterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;



@Controller
@RequestMapping("/catalogo")
public class CatalogoController {
	
	   private final ProductoService productoService;
	    private final CategoriaProductoService categoriaService;
	    private final MarcaService marcaService;
	    private final FormaMonturaService formaService;
	    private final MaterialMonturaService materialService;
	    
	    public CatalogoController(ProductoService productoService, 
	                            CategoriaProductoService categoriaService,
	                            MarcaService marcaService,
	                            FormaMonturaService formaService,
	                            MaterialMonturaService materialService) {
	        this.productoService = productoService;
	        this.categoriaService = categoriaService;
	        this.marcaService = marcaService;
	        this.formaService = formaService;
	        this.materialService = materialService;
	    }
	
	    @GetMapping
	    public String catalogoPrincipal(@RequestParam(defaultValue = "0") int page,
	                                   @RequestParam(defaultValue = "12") int size,
	                                   @RequestParam(required = false) String tipo,
	                                   Model model) {
	        
	        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
	        Page<Producto> productosPage;
	        
	        if (tipo != null && !tipo.isEmpty()) {
	            // Filtrar por tipo
	            TipoProducto tipoEnum = TipoProducto.valueOf(tipo.toUpperCase());
	            productosPage = productoService.findByTipo(tipoEnum, pageable);
	            model.addAttribute("titulo", getTituloPorTipo(tipoEnum));
	        } else {
	            // Todos los productos activos
	            productosPage = productoService.findActiveProducts(pageable);
	            model.addAttribute("titulo", "Nuestro Catálogo");
	        }
	        
	        model.addAttribute("productos", productosPage.getContent());
	        model.addAttribute("currentPage", page);
	        model.addAttribute("totalPages", productosPage.getTotalPages());
	        model.addAttribute("totalItems", productosPage.getTotalElements());
	        model.addAttribute("selectedTipo", tipo);
	        
	        cargarFiltrosDisponibles(model);
	        return "catalogo/index-catalogo";
	        
	        
	    }
	 
	    
	
	    @GetMapping("/buscar")
	    public String buscarProductos(@RequestParam String q,
	                                 @RequestParam(defaultValue = "0") int page,
	                                 @RequestParam(defaultValue = "12") int size,
	                                 Model model) {
	        
	        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
	        List<Producto> productos = productoService.searchProducts(q);
	        
	        // Paginación manual para resultados de búsqueda
	        int start = (int) pageable.getOffset();
	        int end = Math.min((start + pageable.getPageSize()), productos.size());
	        Page<Producto> productosPage = new org.springframework.data.domain.PageImpl<>(
	            productos.subList(start, end), pageable, productos.size()
	        );
	        
	        model.addAttribute("productos", productosPage.getContent());
	        model.addAttribute("currentPage", page);
	        model.addAttribute("totalPages", productosPage.getTotalPages());
	        model.addAttribute("totalItems", productosPage.getTotalElements());
	        model.addAttribute("searchTerm", q);
	        model.addAttribute("titulo", "Resultados de búsqueda: " + q);
	        
	        cargarFiltrosDisponibles(model);
	        return "catalogo/busqueda";
	    }
	    
	 
	    

	    @GetMapping("/filtros")
	    public String filtrarProductos(@ModelAttribute ProductoFilterDTO filtro,
	                                  @RequestParam(defaultValue = "0") int page,
	                                  @RequestParam(defaultValue = "12") int size,
	                                  Model model) {
	        
	        // Convertir IDs a entidades para el filtro
	        Marca marca = null;
	        CategoriaProducto categoria = null;
	        FormaMontura forma = null;
	        MaterialMontura material = null;
	        TipoProducto tipo = null;
	        Genero genero = null;
	        
	        if (filtro.getMarcaId() != null) {
	            marca = marcaService.findById(filtro.getMarcaId()).orElse(null);
	        }
	        if (filtro.getCategoriaId() != null) {
	            categoria = categoriaService.findById(filtro.getCategoriaId()).orElse(null);
	        }
	        if (filtro.getFormaId() != null) {
	            forma = formaService.findById(filtro.getFormaId()).orElse(null);
	        }
	        if (filtro.getMaterialId() != null) {
	            material = materialService.findById(filtro.getMaterialId()).orElse(null);
	        }
	        if (filtro.getTipo() != null && !filtro.getTipo().isEmpty()) {
	            tipo = TipoProducto.valueOf(filtro.getTipo().toUpperCase());
	        }
	        if (filtro.getGenero() != null && !filtro.getGenero().isEmpty()) {
	            genero = Genero.valueOf(filtro.getGenero().toUpperCase());
	        }
	        
	        List<Producto> productos = productoService.findWithFilters(
	            tipo, genero, marca, categoria, forma, material,
	            filtro.getMinPrecio(), filtro.getMaxPrecio()
	        );
	        
	        // Paginación manual
	        Pageable pageable = PageRequest.of(page, size);
	        int start = (int) pageable.getOffset();
	        int end = Math.min((start + pageable.getPageSize()), productos.size());
	        Page<Producto> productosPage = new org.springframework.data.domain.PageImpl<>(
	            productos.subList(start, end), pageable, productos.size()
	        );
	        
	        model.addAttribute("productos", productosPage.getContent());
	        model.addAttribute("currentPage", page);
	        model.addAttribute("totalPages", productosPage.getTotalPages());
	        model.addAttribute("totalItems", productosPage.getTotalElements());
	        model.addAttribute("filtro", filtro);
	        model.addAttribute("titulo", "Productos Filtrados");
	        
	        cargarFiltrosDisponibles(model);
	        return "catalogo/filtros";
	    }

	    @GetMapping("/producto/{id}")
	    public String detalleProducto(@PathVariable Long id, Model model) {
	        Optional<Producto> productoOpt = productoService.findById(id);
	        
	        if (productoOpt.isEmpty() || 
	            productoOpt.get().getEstado() != EstadoProducto.ACTIVO) {
	            return "redirect:/catalogo?error=Producto no disponible";
	        }
	        
	        Producto producto = productoOpt.get();
	        model.addAttribute("producto", producto);
	        
	        // Productos relacionados (misma categoría y tipo)
	        List<Producto> relacionados = productoService.findByTipoAndGenero(
	            producto.getTipo(), producto.getGenero()
	        ).stream()
	         .filter(p -> !p.getIdProducto().equals(id) && 
	                      p.getEstado() == EstadoProducto.ACTIVO)
	         .limit(4)
	         .toList();
	        
	        model.addAttribute("productosRelacionados", relacionados);
	        return "catalogo/item-catalogo";
	    }
	    

	    @GetMapping("/lentes-sol")
	    public String lentesSol(@RequestParam(defaultValue = "0") int page,
	                           @RequestParam(defaultValue = "12") int size,
	                           @RequestParam(required = false) String genero,
	                           Model model) {
	        
	        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
	        Page<Producto> productosPage;
	        
	        if (genero != null && !genero.isEmpty()) {
	            Genero generoEnum = Genero.valueOf(genero.toUpperCase());
	            productosPage = productoService.findByTipoAndGenero(
	                TipoProducto.SOLAR, generoEnum, pageable
	            );
	        } else {
	            productosPage = productoService.findByTipo(TipoProducto.SOLAR, pageable);
	        }
	        
	        model.addAttribute("productos", productosPage.getContent());
	        model.addAttribute("currentPage", page);
	        model.addAttribute("totalPages", productosPage.getTotalPages());
	        model.addAttribute("totalItems", productosPage.getTotalElements());
	        model.addAttribute("categoria", "Lentes de Sol");
	        model.addAttribute("selectedGenero", genero);
	        model.addAttribute("tipoProducto", "SOLAR");
	        
	        cargarFiltrosDisponibles(model);
	        return "catalogo/categoria";
	    }

	    @GetMapping("/lentes-oftalmicos")
	    public String lentesOftalmicos(@RequestParam(defaultValue = "0") int page,
	                                  @RequestParam(defaultValue = "12") int size,
	                                  @RequestParam(required = false) String genero,
	                                  Model model) {
	        
	        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
	        Page<Producto> productosPage;
	        
	        if (genero != null && !genero.isEmpty()) {
	            Genero generoEnum = Genero.valueOf(genero.toUpperCase());
	            productosPage = productoService.findByTipoAndGenero(
	                TipoProducto.OFTALMICO, generoEnum, pageable
	            );
	        } else {
	            productosPage = productoService.findByTipo(TipoProducto.OFTALMICO, pageable);
	        }
	        
	        model.addAttribute("productos", productosPage.getContent());
	        model.addAttribute("currentPage", page);
	        model.addAttribute("totalPages", productosPage.getTotalPages());
	        model.addAttribute("totalItems", productosPage.getTotalElements());
	        model.addAttribute("categoria", "Lentes Oftálmicos");
	        model.addAttribute("selectedGenero", genero);
	        model.addAttribute("tipoProducto", "OFTALMICO");
	        
	        cargarFiltrosDisponibles(model);
	        return "catalogo/categoria";
	    }

	    @GetMapping("/hombre")
	    public String productosHombre(@RequestParam(defaultValue = "0") int page,
	                                 @RequestParam(defaultValue = "12") int size,
	                                 @RequestParam(required = false) String tipo,
	                                 Model model) {
	        
	        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
	        Page<Producto> productosPage;
	        
	        if (tipo != null && !tipo.isEmpty()) {
	            TipoProducto tipoEnum = TipoProducto.valueOf(tipo.toUpperCase());
	            productosPage = productoService.findByTipoAndGenero(tipoEnum, Genero.HOMBRE, pageable);
	        } else {
	            // Todos los productos para hombre
	            List<Producto> productos = productoService.findWithFilters(
	                null, Genero.HOMBRE, null, null, null, null, null, null
	            );
	            // Convertir a página manualmente
	            int start = (int) pageable.getOffset();
	            int end = Math.min((start + pageable.getPageSize()), productos.size());
	            productosPage = new org.springframework.data.domain.PageImpl<>(
	                productos.subList(start, end), pageable, productos.size()
	            );
	        }
	        
	        model.addAttribute("productos", productosPage.getContent());
	        model.addAttribute("currentPage", page);
	        model.addAttribute("totalPages", productosPage.getTotalPages());
	        model.addAttribute("totalItems", productosPage.getTotalElements());
	        model.addAttribute("categoria", "Para Hombre");
	        model.addAttribute("selectedTipo", tipo);
	        
	        cargarFiltrosDisponibles(model);
	        return "catalogo/genero";
	    }
	    
	    @GetMapping("/mujer")
	    public String productosMujer(@RequestParam(defaultValue = "0") int page,
	                                @RequestParam(defaultValue = "12") int size,
	                                @RequestParam(required = false) String tipo,
	                                Model model) {
	        
	        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
	        Page<Producto> productosPage;
	        
	        if (tipo != null && !tipo.isEmpty()) {
	            TipoProducto tipoEnum = TipoProducto.valueOf(tipo.toUpperCase());
	            productosPage = productoService.findByTipoAndGenero(tipoEnum, Genero.MUJER, pageable);
	        } else {
	            // Todos los productos para mujer
	            List<Producto> productos = productoService.findWithFilters(
	                null, Genero.MUJER, null, null, null, null, null, null
	            );
	            // Convertir a página manualmente
	            int start = (int) pageable.getOffset();
	            int end = Math.min((start + pageable.getPageSize()), productos.size());
	            productosPage = new org.springframework.data.domain.PageImpl<>(
	                productos.subList(start, end), pageable, productos.size()
	            );
	        }
	        
	        model.addAttribute("productos", productosPage.getContent());
	        model.addAttribute("currentPage", page);
	        model.addAttribute("totalPages", productosPage.getTotalPages());
	        model.addAttribute("totalItems", productosPage.getTotalElements());
	        model.addAttribute("categoria", "Para Mujer");
	        model.addAttribute("selectedTipo", tipo);
	        
	        cargarFiltrosDisponibles(model);
	        return "catalogo/genero";
	    }
	    

	    @GetMapping("/marca/{marcaId}")
	    public String productosPorMarca(@PathVariable Long marcaId,
	                                   @RequestParam(defaultValue = "0") int page,
	                                   @RequestParam(defaultValue = "12") int size,
	                                   Model model) {
	        
	        Optional<Marca> marcaOpt = marcaService.findById(marcaId);
	        if (marcaOpt.isEmpty() || marcaOpt.get().getEstado() != EstadoGeneral.ACTIVO) {
	            return "redirect:/catalogo?error=Marca no disponible";
	        }
	        
	        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
	        List<Producto> productos = productoService.findByMarca(marcaOpt.get())
	            .stream()
	            .filter(p -> p.getEstado() == EstadoProducto.ACTIVO)
	            .toList();
	        
	        // Convertir a página manualmente
	        int start = (int) pageable.getOffset();
	        int end = Math.min((start + pageable.getPageSize()), productos.size());
	        Page<Producto> productosPage = new org.springframework.data.domain.PageImpl<>(
	            productos.subList(start, end), pageable, productos.size()
	        );
	        
	        model.addAttribute("productos", productosPage.getContent());
	        model.addAttribute("currentPage", page);
	        model.addAttribute("totalPages", productosPage.getTotalPages());
	        model.addAttribute("totalItems", productosPage.getTotalElements());
	        model.addAttribute("categoria", "Marca: " + marcaOpt.get().getNombre());
	        model.addAttribute("marca", marcaOpt.get());
	        
	        cargarFiltrosDisponibles(model);
	        return "catalogo/marca";
	    }
	    

	    @GetMapping("/categoria/{categoriaId}")
	    public String productosPorCategoria(@PathVariable Long categoriaId,
	                                       @RequestParam(defaultValue = "0") int page,
	                                       @RequestParam(defaultValue = "12") int size,
	                                       Model model) {
	        
	        Optional<CategoriaProducto> categoriaOpt = categoriaService.findById(categoriaId);
	        if (categoriaOpt.isEmpty() || categoriaOpt.get().getEstado() != EstadoGeneral.ACTIVO) {
	            return "redirect:/catalogo?error=Categoría no disponible";
	        }
	        
	        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
	        List<Producto> productos = productoService.findByCategoria(categoriaOpt.get())
	            .stream()
	            .filter(p -> p.getEstado() == EstadoProducto.ACTIVO)
	            .toList();
	        
	        // Convertir a página manualmente
	        int start = (int) pageable.getOffset();
	        int end = Math.min((start + pageable.getPageSize()), productos.size());
	        Page<Producto> productosPage = new org.springframework.data.domain.PageImpl<>(
	            productos.subList(start, end), pageable, productos.size()
	        );
	        
	        model.addAttribute("productos", productosPage.getContent());
	        model.addAttribute("currentPage", page);
	        model.addAttribute("totalPages", productosPage.getTotalPages());
	        model.addAttribute("totalItems", productosPage.getTotalElements());
	        model.addAttribute("categoria", "Categoría: " + categoriaOpt.get().getNombre());
	        model.addAttribute("categoriaObj", categoriaOpt.get());
	        
	        cargarFiltrosDisponibles(model);
	        return "catalogo/categoria-especifica";
	    }
	    

	    private void cargarFiltrosDisponibles(Model model) {
	        model.addAttribute("tiposProducto", TipoProducto.values());
	        model.addAttribute("generos", Genero.values());
	        model.addAttribute("tallas", TallaMontura.values());
	        model.addAttribute("marcas", marcaService.findAllActive());
	        model.addAttribute("formas", formaService.findAllActive());
	        model.addAttribute("materiales", materialService.findAllActive());
	        model.addAttribute("categorias", categoriaService.findAllActive());
	        
	        // Rangos de precio predefinidos
	        model.addAttribute("rangosPrecio", List.of(
	            new RangoPrecio("Menos de S/ 100", new BigDecimal("0"), new BigDecimal("100")),
	            new RangoPrecio("S/ 100 - S/ 300", new BigDecimal("100"), new BigDecimal("300")),
	            new RangoPrecio("S/ 300 - S/ 500", new BigDecimal("300"), new BigDecimal("500")),
	            new RangoPrecio("Más de S/ 500", new BigDecimal("500"), null)
	        ));
	    }
	    
	    private String getTituloPorTipo(TipoProducto tipo) {
	        return switch (tipo) {
	            case SOLAR -> "Lentes de Sol";
	            case OFTALMICO -> "Lentes Oftálmicos";
	        };
	    }
	    
	    // CLASE INTERNA PARA RANGOS DE PRECIO
	    private static class RangoPrecio {
	        private final String nombre;
	        private final BigDecimal min;
	        private final BigDecimal max;
	        
	        public RangoPrecio(String nombre, BigDecimal min, BigDecimal max) {
	            this.nombre = nombre;
	            this.min = min;
	            this.max = max;
	        }
	        
	        public String getNombre() { return nombre; }
	        public BigDecimal getMin() { return min; }
	        public BigDecimal getMax() { return max; }
	    }

}
