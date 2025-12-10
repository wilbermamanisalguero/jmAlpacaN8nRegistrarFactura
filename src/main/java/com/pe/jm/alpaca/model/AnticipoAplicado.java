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
public class AnticipoAplicado {
    private Long ventaRucVendedor;
    private String ventaCodigoFactura;
    private Long anticipoRucVendedor;
    private String anticipoCodigoFactura;
    private BigDecimal montoAplicado;
}
