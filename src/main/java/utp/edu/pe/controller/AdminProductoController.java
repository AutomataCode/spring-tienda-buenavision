package utp.edu.pe.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import utp.edu.pe.entity.Producto;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.entity.enums.EstadoProducto;
import utp.edu.pe.entity.enums.Genero;
import utp.edu.pe.entity.enums.TallaMontura;
import utp.edu.pe.entity.enums.TipoProducto;
import utp.edu.pe.service.*;  

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Controller
@RequestMapping("/admin/productos")
public class AdminProductoController {
	
	@Autowired
	private  ProductoService productoService;
	@Autowired
    private   InventarioExcelService inventarioExcelService; 
    
     
	@Autowired
    private  CategoriaProductoService categoriaProductoService;
	@Autowired
    private  MarcaService marcaService;
	@Autowired
    private FormaMonturaService formaMonturaService;
	@Autowired
    private MaterialMonturaService materialMonturaService;
    
    
    
    
    private static final Logger log = LoggerFactory.getLogger(AdminProductoController.class); // Logback

    /**
     Muestra la lista paginada de productos.
     */
    @GetMapping
    public String listarProductos(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("idProducto").descending());
        Page<Producto> paginaProductos = productoService.findAll(pageable);
        
        model.addAttribute("paginaProductos", paginaProductos);
        model.addAttribute("activeProducts", productoService.countActiveProducts());
        model.addAttribute("outOfStockProducts", productoService.countOutOfStockProducts());
        return "admin/productos/index"; // Nueva vista
    }

    /**
     * Muestra el formulario para crear un nuevo producto.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        cargarDatosSelect(model);
        return "admin/productos/form"; // Nueva vista
    }

    /**
     * Muestra el formulario para editar un producto existente.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes attributes) {
        Optional<Producto> productoOpt = productoService.findById(id);
        
        if (productoOpt.isEmpty()) {
            attributes.addFlashAttribute("errorMessage", "Producto no encontrado.");
            return "redirect:/admin/productos";
        }
        
        model.addAttribute("producto", productoOpt.get());
        cargarDatosSelect(model);
        return "admin/productos/form";
    }

    /**
     * Guarda un producto (nuevo o editado).
     */
    @PostMapping("/guardar")
    public String guardarProducto(@Valid @ModelAttribute("producto") Producto producto,
                                BindingResult result,
                                Model model,
                                @AuthenticationPrincipal Usuario adminUsuario, // Usuario que guarda
                                RedirectAttributes attributes) {
        
        if (result.hasErrors()) {
            cargarDatosSelect(model);
            return "admin/productos/form";
        }

        // Validar SKU único (solo al crear)
        if (producto.getIdProducto() == null && productoService.existsBySku(producto.getSku())) {
            result.rejectValue("sku", "sku.duplicado", "El SKU ya existe. Elija otro.");
            cargarDatosSelect(model);
            return "admin/productos/form";
        }

        try {
            productoService.saveAdmin(producto, adminUsuario);
            attributes.addFlashAttribute("successMessage", "Producto guardado exitosamente.");
        } catch (Exception e) {
            log.error("Error al guardar producto", e);
            attributes.addFlashAttribute("errorMessage", "Error al guardar el producto: " + e.getMessage());
        }
        
        return "redirect:/admin/productos";
    }

    /**
     *  Marca un producto como INACTIVO.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            productoService.softDelete(id);
            attributes.addFlashAttribute("successMessage", "Producto desactivado exitosamente.");
        } catch (Exception e) {
            log.error("Error al desactivar producto {}", id, e);
            attributes.addFlashAttribute("errorMessage", "Error al desactivar el producto.");
        }
        return "redirect:/admin/productos";
    }

    // --- Endpoints de Apache POI ---

    @GetMapping("/exportar-excel")
    public ResponseEntity<InputStreamResource> exportarExcel() throws IOException {
        ByteArrayInputStream in = inventarioExcelService.exportarInventarioExcel();
        
        HttpHeaders headers = new HttpHeaders();
        String fecha = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        headers.add("Content-Disposition", "attachment; filename=inventario_buenavision_" + fecha + ".xlsx");
        
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }

    @PostMapping("/importar-excel")
    public String importarExcel(@RequestParam("file") MultipartFile file,
                              @AuthenticationPrincipal Usuario adminUsuario,
                              RedirectAttributes attributes) {
        if (file.isEmpty()) {
            attributes.addFlashAttribute("errorMessage", "Por favor, seleccione un archivo Excel.");
            return "redirect:/admin/productos";
        }

        try {
            inventarioExcelService.importarStockDesdeExcel(file, adminUsuario);
            attributes.addFlashAttribute("successMessage", "Stock importado y actualizado exitosamente.");
        } catch (Exception e) {
            log.error("Error al importar Excel", e);
            attributes.addFlashAttribute("errorMessage", "Error al procesar la importación: " + e.getMessage());
        }
        
        return "redirect:/admin/productos";
    }

    /**
     * Método auxiliar para cargar los datos de los <select>
     */
    private void cargarDatosSelect(Model model) {
        model.addAttribute("categorias", categoriaProductoService.findAll());
        model.addAttribute("marcas", marcaService.findAll());
        model.addAttribute("formas", formaMonturaService.findAll());
        model.addAttribute("materiales", materialMonturaService.findAll());
        model.addAttribute("estados", EstadoProducto.values());
        model.addAttribute("tallasMontura", TallaMontura.values());
        model.addAttribute("tiposProducto", TipoProducto.values());
        model.addAttribute("generos", Genero.values());
     
    }
	
	

}
