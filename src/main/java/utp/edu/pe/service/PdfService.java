package utp.edu.pe.service;
import utp.edu.pe.entity.Venta;
import java.io.ByteArrayInputStream;

public interface PdfService {
	
	ByteArrayInputStream generarBoletaPdf(Venta venta, String ruc, String empresaNombre);

}
