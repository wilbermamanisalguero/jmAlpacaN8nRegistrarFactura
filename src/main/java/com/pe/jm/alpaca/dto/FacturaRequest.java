package com.pe.jm.alpaca.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaRequest {
    @JsonProperty("factura")
    private FacturaData factura;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacturaData {
        @JsonProperty("serieNumero")
        private String serieNumero;

        @JsonProperty("rucEmisor")
        private String rucEmisor;

        @JsonProperty("fechaEmision")
        private String fechaEmision;
 

        @JsonProperty("ruc")
        private String ruc;

        @JsonProperty("moneda")
        private String moneda;

        @JsonProperty("facturaAnticipo")
        private List<FacturaAnticipoItem> facturaAnticipo;

        @JsonProperty("observacion")
        private String observacion;

        @JsonProperty("bienServicioCodigo")
        private String bienServicioCodigo;

        @JsonProperty("medioPagoCodigo")
        private String medioPagoCodigo;

        @JsonProperty("numeroCuentaBancoNacion")
        private String numeroCuentaBancoNacion;

        @JsonProperty("porcentajeDetraccion")
        private Double porcentajeDetraccion;

        @JsonProperty("montoDetraccion")
        private Double montoDetraccion;

        @JsonProperty("estadoFactura")
        private String estadoFactura;

        @JsonProperty("tipoFactura")
        private String tipoFactura;        

        @JsonProperty("detalle")
        private List<DetalleItem> detalle;

        @JsonProperty("totales")
        private TotalesData totales;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacturaAnticipoItem {
        @JsonProperty("numero")
        private String numero;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleItem {
        @JsonProperty("cantidad")
        private Double cantidad;

        @JsonProperty("unidadMedida")
        private String unidadMedida;

        @JsonProperty("descripcion")
        private String descripcion;

        @JsonProperty("valorUnitario")
        private Double valorUnitario;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalesData {
        @JsonProperty("subTotalVentas")
        private Double subTotalVentas;

        @JsonProperty("anticipos")
        private Double anticipos;

        @JsonProperty("descuentos")
        private Double descuentos;

        @JsonProperty("valorVenta")
        private Double valorVenta;

        @JsonProperty("isc")
        private Double isc;

        @JsonProperty("igv")
        private Double igv;

        @JsonProperty("otrosCargos")
        private Double otrosCargos;

        @JsonProperty("otrosTributos")
        private Double otrosTributos;

        @JsonProperty("montoRedondeo")
        private Double montoRedondeo;

        @JsonProperty("importeTotal")
        private Double importeTotal;
    }
}
