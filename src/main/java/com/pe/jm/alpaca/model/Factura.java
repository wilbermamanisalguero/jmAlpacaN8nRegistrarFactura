package com.pe.jm.alpaca.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Factura {
    private Long rucVendedor;
    private String codigoFactura;
    private LocalDate fechaEmision;
    private Long rucCliente;
    private String formaPago;
    private String facturaAnticipo;
    private String tipoMoneda;
    private BigDecimal subTotalVentas;
    private BigDecimal anticipos;
    private BigDecimal valorVenta;
    private BigDecimal montoDetraccion;
    private String observacion;
    private String codBienServicio;
    private String codMedioPago;
    private String nroCtaBancoNacion;
    private BigDecimal porcentajeDetraccion;
    private BigDecimal importeTotal;
    private String tipo;
    private String anulacion;
}
