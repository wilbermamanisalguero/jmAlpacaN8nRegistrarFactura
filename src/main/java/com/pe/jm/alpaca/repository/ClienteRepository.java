package com.pe.jm.alpaca.repository;

import com.pe.jm.alpaca.model.Cliente;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.springframework.stereotype.Repository;

@Repository
public class ClienteRepository {

    private final MySQLPool client;

    public ClienteRepository(MySQLPool client) {
        this.client = client;
    }

    public Future<Cliente> findByRuc(Long rucCliente) {
        String sql = """
            SELECT * FROM AA_CLIENTE
            WHERE RUC_CLIENTE = ?
            """;

        return client.preparedQuery(sql)
                .execute(Tuple.of(rucCliente))
                .map(rows -> {
                    if (rows.size() == 0) {
                        return null;
                    }
                    Row row = rows.iterator().next();
                    return mapRowToCliente(row);
                });
    }

    public Future<Long> insert(Cliente cliente) {
        String sql = """
            INSERT INTO AA_CLIENTE (RUC_CLIENTE, NOMBRE_CLIENTE)
            VALUES (?, ?)
            """;

        Tuple params = Tuple.of(
                cliente.getRucCliente(),
                cliente.getNombreCliente()
        );

        return client.preparedQuery(sql)
                .execute(params)
                .map(rowSet -> (long) rowSet.rowCount());
    }

    public Future<Boolean> existsByRuc(Long rucCliente) {
        String sql = """
            SELECT COUNT(*) as total FROM AA_CLIENTE
            WHERE RUC_CLIENTE = ?
            """;

        return client.preparedQuery(sql)
                .execute(Tuple.of(rucCliente))
                .map(rows -> {
                    Row row = rows.iterator().next();
                    Long count = row.getLong("total");
                    return count > 0;
                });
    }

    private Cliente mapRowToCliente(Row row) {
        return Cliente.builder()
                .rucCliente(row.getLong("RUC_CLIENTE"))
                .nombreCliente(row.getString("NOMBRE_CLIENTE"))
                .build();
    }
}
