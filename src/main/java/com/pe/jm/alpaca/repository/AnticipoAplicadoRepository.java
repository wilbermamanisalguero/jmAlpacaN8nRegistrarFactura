package com.pe.jm.alpaca.repository;

import com.pe.jm.alpaca.model.AnticipoAplicado;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AnticipoAplicadoRepository {

    private final MySQLPool client;

    public AnticipoAplicadoRepository(MySQLPool client) {
        this.client = client;
    }

    public Future<Long> insert(AnticipoAplicado anticipo) {
        String sql = """
            INSERT INTO AA_ANTICIPO_APLICADO (
                VENTA_RUC_VENDEDOR, VENTA_CODIGO_FACTURA,
                ANTICIPO_RUC_VENDEDOR, ANTICIPO_CODIGO_FACTURA, MONTO_APLICADO
            ) VALUES (?, ?, ?, ?, ?)
            """;

        Tuple params = Tuple.of(
                anticipo.getVentaRucVendedor(),
                anticipo.getVentaCodigoFactura(),
                anticipo.getAnticipoRucVendedor(),
                anticipo.getAnticipoCodigoFactura(),
                anticipo.getMontoAplicado()
        );

        return client.preparedQuery(sql)
                .execute(params)
                .map(rowSet -> (long) rowSet.rowCount());
    }

    public Future<List<AnticipoAplicado>> findByVenta(Long rucVendedor, String codigoFactura) {
        String sql = """
            SELECT * FROM AA_ANTICIPO_APLICADO
            WHERE VENTA_RUC_VENDEDOR = ? AND VENTA_CODIGO_FACTURA = ?
            """;

        return client.preparedQuery(sql)
                .execute(Tuple.of(rucVendedor, codigoFactura))
                .map(rows -> {
                    List<AnticipoAplicado> anticipos = new ArrayList<>();
                    for (Row row : rows) {
                        anticipos.add(mapRowToAnticipoAplicado(row));
                    }
                    return anticipos;
                });
    }

    private AnticipoAplicado mapRowToAnticipoAplicado(Row row) {
        return AnticipoAplicado.builder()
                .ventaRucVendedor(row.getLong("VENTA_RUC_VENDEDOR"))
                .ventaCodigoFactura(row.getString("VENTA_CODIGO_FACTURA"))
                .anticipoRucVendedor(row.getLong("ANTICIPO_RUC_VENDEDOR"))
                .anticipoCodigoFactura(row.getString("ANTICIPO_CODIGO_FACTURA"))
                .montoAplicado(row.getBigDecimal("MONTO_APLICADO"))
                .build();
    }
}
