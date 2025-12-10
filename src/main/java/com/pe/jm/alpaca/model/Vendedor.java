package com.pe.jm.alpaca.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vendedor {
    private Long rucVendedor;
    private String nombreVendedor;
}
