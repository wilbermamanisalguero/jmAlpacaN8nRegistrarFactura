package com.pe.jm.alpaca.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleFactura {
    private Long detalleId;
    private Long rucVendedor;
    private String codigoFactura;
    private Long idProducto;
    private BigDecimal cantidad;
    private String unidadMedida;
    private String codigo;
    private String descripcion;
    private BigDecimal valorUnitario;
}
