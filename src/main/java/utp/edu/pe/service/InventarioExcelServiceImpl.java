package utp.edu.pe.service;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import utp.edu.pe.entity.Producto;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.repository.ProductoRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;


@Service
public class InventarioExcelServiceImpl  implements InventarioExcelService{
	
	
	private final ProductoRepository productoRepository;
    private final ProductoService productoService;
    
    private static final Logger log = LoggerFactory.getLogger(InventarioExcelServiceImpl.class);
    
	public InventarioExcelServiceImpl(ProductoRepository productoRepository, ProductoService productoService) {
		this.productoRepository = productoRepository;
		this.productoService= productoService;
	}

    

	@Override
	public ByteArrayInputStream exportarInventarioExcel() throws IOException {
		String[] COLUMNAS = {"ID", "SKU", "Nombre", "Stock Actual", "Precio", "Estado"}; 
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Sheet sheet = workbook.createSheet("Inventario");
            // Estilo de Cabecera
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            // Fila de Cabecera
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < COLUMNAS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(COLUMNAS[col]);
                cell.setCellStyle(headerCellStyle);
            }
            // Llenar Datos
            List<Producto> productos = productoRepository.findAll(); // Trae todos
            int rowIdx = 1;
            for (Producto producto : productos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(producto.getIdProducto());
                row.createCell(1).setCellValue(producto.getSku());
                row.createCell(2).setCellValue(producto.getNombre());
                row.createCell(3).setCellValue(producto.getStockActual());
                row.createCell(4).setCellValue(producto.getPrecioCosto().doubleValue());
                row.createCell(5).setCellValue(producto.getEstado().name());
            }           
            for(int i=0; i<COLUMNAS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
	}

	@Override
	public void importarStockDesdeExcel(MultipartFile file, Usuario adminUsuario) throws IOException {
		log.info("Iniciando importación de stock por {}", adminUsuario.getEmail());
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                if (rowNumber == 0) { // Omitir cabecera
                    rowNumber++;
                    continue;
                }

                Cell skuCell = currentRow.getCell(1); // Col B (SKU)
                Cell qtyCell = currentRow.getCell(3); // Col D (Nuevo Stock)

                if (skuCell == null || qtyCell == null) {
                    log.warn("Omitiendo fila {}: SKU o Cantidad nulos", rowNumber + 1);
                    continue;
                }
                
                String sku = skuCell.getStringCellValue();
                int nuevoStock = (int) qtyCell.getNumericCellValue();
                
                try {
                    Optional<Producto> productoOpt = productoRepository.findBySku(sku);
                    if (productoOpt.isEmpty()) {
                        log.warn("Omitiendo fila {}: SKU '{}' no encontrado", rowNumber + 1, sku);
                        continue;
                    }
                    
                    Producto producto = productoOpt.get();
                    if (producto.getStockActual() != nuevoStock) {
                        // Usamos saveAdmin para que la lógica de Inventario se ejecute
                        producto.setStockActual(nuevoStock);
                        productoService.saveAdmin(producto, adminUsuario);
                        log.info("Stock actualizado por Excel para SKU {}: {} -> {}", sku, producto.getStockActual(), nuevoStock);
                    }
                    
                } catch (Exception e) {
                    log.error("Error procesando fila {}: SKU {}. Error: {}", rowNumber + 1, sku, e.getMessage());
                }
                rowNumber++;
            }
        }
        log.info("Importación de stock finalizada.");
    }
}
