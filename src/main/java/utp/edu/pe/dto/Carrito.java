package utp.edu.pe.dto;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Carrito {
    private List<CarritoItem> items = new ArrayList<>();

    public List<CarritoItem> getItems() { return items; }
    public void setItems(List<CarritoItem> items) { this.items = items; }

    // Calcula el número total de unidades
    public int getTotalUnidades() {
        return items.stream().mapToInt(CarritoItem::getCantidad).sum();
    }

    // Calcula el subtotal total
    public BigDecimal getSubtotalTotal() {
        return items.stream()
                    .map(CarritoItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
   
}