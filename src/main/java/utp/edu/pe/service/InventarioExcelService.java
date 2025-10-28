package utp.edu.pe.service;

import org.springframework.web.multipart.MultipartFile;
import utp.edu.pe.entity.Usuario;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public interface InventarioExcelService {
	/**
     * Genera un archivo Excel (XLSX) con el stock actual de todos los productos.
     */
    ByteArrayInputStream exportarInventarioExcel() throws IOException;

 
    void importarStockDesdeExcel(MultipartFile file, Usuario adminUsuario) throws IOException;
}
