package com.dentalcare.api.models.enums;

/**
 * Propósito: Categoriza la naturaleza de las transacciones de inventario clínico.
 * Permite tipificar entradas por compras, salidas por consumo en consulta, bajas o ajustes.
 * 
 * Ubicación y Rol: Capa de Persistencia / Enumerador de Dominio de Inventarios.
 * 
 * Trazabilidad (Referencias):
 * - Utilizado por: Entidad MovimientoInventario.
 */
public enum TipoMovimiento {
    /** Ingreso o compra de nuevos insumos a bodega */
    INGRESO, 
    
    /** Salida por consumo clínico o dispensación a paciente */
    SALIDA, 
    
    /** Ajuste de balance por auditoría física o merma */
    AJUSTE, 
    
    /** Devolución a proveedor o rechazo de lote defectuoso */
    DEVOLUCION, 
    
    /** Reposición por garantía o reposición interna */
    REPOSICION, 
    
    /** Entrada por transferencia entre sucursales o consultorios */
    TRANSFERENCIA_ENTRADA, 
    
    /** Salida por transferencia hacia otra sede o consultorio */
    TRANSFERENCIA_SALIDA, 
    
    /** Movimiento especial no tipificado */
    OTRO    
}

