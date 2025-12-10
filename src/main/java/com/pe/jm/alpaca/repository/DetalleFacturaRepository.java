package com.pe.jm.alpaca.repository;

import com.pe.jm.alpaca.model.DetalleFactura;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class DetalleFacturaRepository {

    private final MySQLPool client;

    public DetalleFacturaRepository(MySQLPool client) {
        this.client = client;
    }

    public Future<Long> insert(DetalleFactura detalle) {
        String sql = """
            INSERT INTO AA_DETALLE_FACTURA (
                RUC_VENDEDOR, CODIGO_FACTURA, ID_PRODUCTO, CANTIDAD,
                UNIDAD_MEDIDA, CODIGO, DESCRIPCION, VALOR_UNITARIO
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Tuple params = Tuple.of(
                detalle.getRucVendedor(),
                detalle.getCodigoFactura(),
                detalle.getIdProducto(),
                detalle.getCantidad(),
                detalle.getUnidadMedida(),
                detalle.getCodigo(),
                detalle.getDescripcion(),
                detalle.getValorUnitario()
        );

        return client.preparedQuery(sql)
                .execute(params)
                .map(rowSet -> (long) rowSet.rowCount());
    }

    public Future<List<DetalleFactura>> findByRucVendedorAndCodigoFactura(Long rucVendedor, String codigoFactura) {
        String sql = """
            SELECT * FROM AA_DETALLE_FACTURA
            WHERE RUC_VENDEDOR = ? AND CODIGO_FACTURA = ?
            """;

        return client.preparedQuery(sql)
                .execute(Tuple.of(rucVendedor, codigoFactura))
                .map(rows -> {
                    List<DetalleFactura> detalles = new ArrayList<>();
                    for (Row row : rows) {
                        detalles.add(mapRowToDetalleFactura(row));
                    }
                    return detalles;
                });
    }

    private DetalleFactura mapRowToDetalleFactura(Row row) {
        return DetalleFactura.builder()
                .detalleId(row.getLong("DETALLE_ID"))
                .rucVendedor(row.getLong("RUC_VENDEDOR"))
                .codigoFactura(row.getString("CODIGO_FACTURA"))
                .idProducto(row.getLong("ID_PRODUCTO"))
                .cantidad(row.getBigDecimal("CANTIDAD"))
                .unidadMedida(row.getString("UNIDAD_MEDIDA"))
                .codigo(row.getString("CODIGO"))
                .descripcion(row.getString("DESCRIPCION"))
                .valorUnitario(row.getBigDecimal("VALOR_UNITARIO"))
                .build();
    }
}
