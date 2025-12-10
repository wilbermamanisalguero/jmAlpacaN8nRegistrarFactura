package com.pe.jm.alpaca.repository;

import com.pe.jm.alpaca.model.Vendedor;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.springframework.stereotype.Repository;

@Repository
public class VendedorRepository {

    private final MySQLPool client;

    public VendedorRepository(MySQLPool client) {
        this.client = client;
    }

    public Future<Vendedor> findByRuc(Long rucVendedor) {
        String sql = """
            SELECT * FROM AA_VENDEDOR
            WHERE RUC_VENDEDOR = ?
            """;

        return client.preparedQuery(sql)
                .execute(Tuple.of(rucVendedor))
                .map(rows -> {
                    if (rows.size() == 0) {
                        return null;
                    }
                    Row row = rows.iterator().next();
                    return mapRowToVendedor(row);
                });
    }

    public Future<Long> insert(Vendedor vendedor) {
        String sql = """
            INSERT INTO AA_VENDEDOR (RUC_VENDEDOR, NOMBRE_VENDEDOR)
            VALUES (?, ?)
            """;

        Tuple params = Tuple.of(
                vendedor.getRucVendedor(),
                vendedor.getNombreVendedor()
        );

        return client.preparedQuery(sql)
                .execute(params)
                .map(rowSet -> (long) rowSet.rowCount());
    }

    public Future<Boolean> existsByRuc(Long rucVendedor) {
        String sql = """
            SELECT COUNT(*) as total FROM AA_VENDEDOR
            WHERE RUC_VENDEDOR = ?
            """;

        return client.preparedQuery(sql)
                .execute(Tuple.of(rucVendedor))
                .map(rows -> {
                    Row row = rows.iterator().next();
                    Long count = row.getLong("total");
                    return count > 0;
                });
    }

    private Vendedor mapRowToVendedor(Row row) {
        return Vendedor.builder()
                .rucVendedor(row.getLong("RUC_VENDEDOR"))
                .nombreVendedor(row.getString("NOMBRE_VENDEDOR"))
                .build();
    }
}
