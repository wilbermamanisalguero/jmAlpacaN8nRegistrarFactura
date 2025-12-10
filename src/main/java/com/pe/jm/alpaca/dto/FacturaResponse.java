package com.pe.jm.alpaca.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaResponse {
    private boolean success;
    private String message;
    private String codigoFactura;
    private Long rucVendedor;
}
