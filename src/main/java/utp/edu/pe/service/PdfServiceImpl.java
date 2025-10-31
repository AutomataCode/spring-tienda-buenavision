package utp.edu.pe.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import utp.edu.pe.entity.DetallePedido;
import utp.edu.pe.entity.Venta;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;


@Service
public class PdfServiceImpl  implements PdfService{
	// --- Definición de Fuentes ---
    private static final Font FONT_TITULO_EMPRESA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Font.ITALIC);
    private static final Font FONT_INFO_EMPRESA = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC);
    private static final Font FONT_TITULO_BOLETA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.ITALIC);
    private static final Font FONT_BOLETA_NUMERO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.BOLDITALIC);
    private static final Font FONT_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Font.ITALIC);
    private static final Font FONT_BODY = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.BOLD);
    private static final Font FONT_TABLA_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL);
    private static final Font FONT_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.ITALIC);
	@Override
	public ByteArrayInputStream generarBoletaPdf(Venta venta, String ruc, String empresaNombre) {
ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try (Document document = new Document(PageSize.A4)) {
            PdfWriter.getInstance(document, out);
            document.open();

            // ---  Encabezado (Empresa y N° Boleta) ---
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{3f, 2f});
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            
            // Datos de la empresa
            PdfPCell cellEmpresa = new PdfPCell();
            cellEmpresa.setBorder(Rectangle.NO_BORDER);
            cellEmpresa.addElement(new Paragraph(empresaNombre, FONT_TITULO_EMPRESA));
            cellEmpresa.addElement(new Paragraph("RUC: " + ruc, FONT_INFO_EMPRESA));
            headerTable.addCell(cellEmpresa);

            // N° de Boleta
            PdfPCell cellBoleta = new PdfPCell();
            cellBoleta.setBorder(Rectangle.BOX);
            cellBoleta.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellBoleta.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellBoleta.addElement(new Paragraph("BOLETA DE VENTA", FONT_TITULO_BOLETA));
            cellBoleta.addElement(new Paragraph(venta.getNumeroVenta(), FONT_BOLETA_NUMERO));
            headerTable.addCell(cellBoleta);
            
            document.add(headerTable);
            document.add(new Paragraph("\n")); // Espacio

            //   Información del Cliente y Venta ---
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // Columna Izquierda (Cliente)
            PdfPCell cellCliente = new PdfPCell();
            cellCliente.setBorder(Rectangle.NO_BORDER);
            cellCliente.addElement(new Paragraph("CLIENTE:", FONT_SUBTITULO));
            cellCliente.addElement(new Paragraph("Razón Social: " + venta.getPedido().getCliente().getNombre() + " " + venta.getPedido().getCliente().getApellido(), FONT_BODY));
            cellCliente.addElement(new Paragraph("Documento: " + venta.getPedido().getCliente().getNumeroDocumento(), FONT_BODY));
            cellCliente.addElement(new Paragraph("Dirección: " + venta.getPedido().getDireccionEntrega(), FONT_BODY));
            infoTable.addCell(cellCliente);
            
            // Columna Derecha (Venta)
            PdfPCell cellVenta = new PdfPCell();
            cellVenta.setBorder(Rectangle.NO_BORDER);
            cellVenta.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellVenta.addElement(new Paragraph("Fecha Emisión: " + venta.getFechaVenta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), FONT_BODY));
            cellVenta.addElement(new Paragraph("N° Pedido: " + venta.getPedido().getNumeroPedido(), FONT_BODY));
            cellVenta.addElement(new Paragraph("Vendedor: " + venta.getVendedor().getNombreCompleto(), FONT_BODY));
            infoTable.addCell(cellVenta);

            document.add(infoTable);
            document.add(new Paragraph("\n")); // Espacio

            //   Tabla de Detalles ---
            PdfPTable tablaDetalles = new PdfPTable(4);
            tablaDetalles.setWidthPercentage(100);
            tablaDetalles.setWidths(new float[]{1f, 5f, 2f, 2f});
            
            // Headers de la tabla
            addHeaderCell(tablaDetalles, "Cant.");
            addHeaderCell(tablaDetalles, "Descripción");
            addHeaderCell(tablaDetalles, "P. Unit.");
            addHeaderCell(tablaDetalles, "Importe");

            // Contenido de la tabla
            for (DetallePedido detalle : venta.getPedido().getDetalles()) {
                addBodyCell(tablaDetalles, detalle.getCantidad().toString(), Element.ALIGN_CENTER);
                addBodyCell(tablaDetalles, detalle.getProducto().getNombre(), Element.ALIGN_LEFT);
                addBodyCell(tablaDetalles, "S/ " + String.format("%.2f", detalle.getPrecioUnitario()), Element.ALIGN_RIGHT);
                
                BigDecimal importe = detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad()));
                addBodyCell(tablaDetalles, "S/ " + String.format("%.2f", importe), Element.ALIGN_RIGHT);
            }
            document.add(tablaDetalles);

            //  Totales ---
            PdfPTable tablaTotales = new PdfPTable(2);
            tablaTotales.setWidthPercentage(100);
            tablaTotales.setWidths(new float[]{7f, 3f});
            tablaTotales.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            tablaTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            addTotalCell(tablaTotales, "Subtotal:");
            addTotalCell(tablaTotales, "S/ " + String.format("%.2f", venta.getSubtotal()));
            
            addTotalCell(tablaTotales, "IGV (18%):");
            addTotalCell(tablaTotales, "S/ " + String.format("%.2f", venta.getIgv()));
            
            addTotalCell(tablaTotales, "TOTAL A PAGAR:", FONT_TOTAL, Element.ALIGN_RIGHT);
            addTotalCell(tablaTotales, "S/ " + String.format("%.2f", venta.getTotal()), FONT_TOTAL, Element.ALIGN_RIGHT);

            document.add(tablaTotales);

            //  Pie de página ---
            Paragraph footer = new Paragraph("\n¡Gracias por su compra en " + empresaNombre + "!", FONT_INFO_EMPRESA);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

        } catch (DocumentException e) {
           
            e.printStackTrace();
        }
        
        return new ByteArrayInputStream(out.toByteArray());
    }
	private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_TABLA_HEADER));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(new java.awt.Color(160, 160, 160)); // Color oscuro
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    private void addBodyCell(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BODY));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        table.addCell(cell);
    }
    
    private void addTotalCell(PdfPTable table, String text) {
        addTotalCell(table, text, FONT_BODY, Element.ALIGN_RIGHT);
    }
    
    private void addTotalCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(3);
        table.addCell(cell);
    }

}
