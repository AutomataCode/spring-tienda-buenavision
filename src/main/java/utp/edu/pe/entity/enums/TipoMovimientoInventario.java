package utp.edu.pe.entity.enums;

public enum TipoMovimientoInventario {
	ENTRADA, // Ingreso de stock nuevo
	    SALIDA,  // Venta completada
	    AJUSTE,  // Corrección manual de stock
	    RESERVA  // Podría usarse al crear un pedido, antes de la venta (opcional)
	

}
