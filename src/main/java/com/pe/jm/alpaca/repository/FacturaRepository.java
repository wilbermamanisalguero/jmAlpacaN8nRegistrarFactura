package com.pe.jm.alpaca.repository;

import com.pe.jm.alpaca.model.Factura;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.springframework.stereotype.Repository;

@Repository
public class FacturaRepository {

    private final MySQLPool client;

    public FacturaRepository(MySQLPool client) {
        this.client = client;
    }

    public Future<Long> insert(Factura factura) {
        String sql = """
            INSERT INTO AA_FACTURA (
                RUC_VENDEDOR, CODIGO_FACTURA, FECHA_EMISION, RUC_CLIENTE,
                FORMA_PAGO, FACTURA_ANTICIPO, TIPO_MONEDA, SUB_TOTAL_VENTAS,
                ANTICIPOS, VALOR_VENTA, MONTO_DETRACCION, OBSERVACION,
                COD_BIEN_SERVICIO, COD_MEDIO_PAGO, NRO_CTA_BANCO_NACION,
                PORCENTAJE_DETRACCION, IMPORTE_TOTAL, TIPO, ANULACION
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Tuple params = Tuple.of(
                factura.getRucVendedor(),
                factura.getCodigoFactura(),
                factura.getFechaEmision(),
                factura.getRucCliente(),
                factura.getFormaPago(),
                factura.getFacturaAnticipo(),
                factura.getTipoMoneda(),
                factura.getSubTotalVentas(),
                factura.getAnticipos(),
                factura.getValorVenta(),
                factura.getMontoDetraccion(),
                factura.getObservacion(),
                factura.getCodBienServicio(),
                factura.getCodMedioPago(),
                factura.getNroCtaBancoNacion(),
                factura.getPorcentajeDetraccion(),
                factura.getImporteTotal(),
                factura.getTipo(),
                factura.getAnulacion()
        );

        return client.preparedQuery(sql)
                .execute(params)
                .map(rowSet -> (long) rowSet.rowCount());
    }

    public Future<Factura> findByRucVendedorAndCodigoFactura(Long rucVendedor, String codigoFactura) {
        String sql = """
            SELECT * FROM AA_FACTURA
            WHERE RUC_VENDEDOR = ? AND CODIGO_FACTURA = ?
            """;

        return client.preparedQuery(sql)
                .execute(Tuple.of(rucVendedor, codigoFactura))
                .map(rows -> {
                    if (rows.size() == 0) {
                        return null;
                    }
                    Row row = rows.iterator().next();
                    return mapRowToFactura(row);
                });
    }

    private Factura mapRowToFactura(Row row) {
        return Factura.builder()
                .rucVendedor(row.getLong("RUC_VENDEDOR"))
                .codigoFactura(row.getString("CODIGO_FACTURA"))
                .fechaEmision(row.getLocalDate("FECHA_EMISION"))
                .rucCliente(row.getLong("RUC_CLIENTE"))
                .formaPago(row.getString("FORMA_PAGO"))
                .facturaAnticipo(row.getString("FACTURA_ANTICIPO"))
                .tipoMoneda(row.getString("TIPO_MONEDA"))
                .subTotalVentas(row.getBigDecimal("SUB_TOTAL_VENTAS"))
                .anticipos(row.getBigDecimal("ANTICIPOS"))
                .valorVenta(row.getBigDecimal("VALOR_VENTA"))
                .montoDetraccion(row.getBigDecimal("MONTO_DETRACCION"))
                .observacion(row.getString("OBSERVACION"))
                .codBienServicio(row.getString("COD_BIEN_SERVICIO"))
                .codMedioPago(row.getString("COD_MEDIO_PAGO"))
                .nroCtaBancoNacion(row.getString("NRO_CTA_BANCO_NACION"))
                .porcentajeDetraccion(row.getBigDecimal("PORCENTAJE_DETRACCION"))
                .importeTotal(row.getBigDecimal("IMPORTE_TOTAL"))
                .tipo(row.getString("TIPO"))
                .anulacion(row.getString("ANULACION"))
                .build();
    }
}
