package com.pe.jm.alpaca.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    private Long idProducto;
    private String descripcion;
    private String tipoPresentacion;
    private String tipoFibra;
    private String tipoCalidad;
}
