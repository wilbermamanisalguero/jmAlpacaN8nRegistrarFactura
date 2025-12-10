package com.pe.jm.alpaca.config;

import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MySQLConfig {

    @Value("${mysql.host}")
    private String host;

    @Value("${mysql.port}")
    private int port;

    @Value("${mysql.database}")
    private String database;

    @Value("${mysql.username}")
    private String username;

    @Value("${mysql.password}")
    private String password;

    @Value("${mysql.pool.max-size}")
    private int maxPoolSize;

    @Value("${mysql.pool.max-wait-queue-size}")
    private int maxWaitQueueSize;

    @Value("${mysql.connection.timeout}")
    private int connectionTimeout;

    @Value("${mysql.idle.timeout}")
    private int idleTimeout;

    @Bean
    public Vertx vertx() {
        return Vertx.vertx();
    }

    @Bean
    public MySQLPool mySQLPool(Vertx vertx) {
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setHost(host)
                .setPort(port)
                .setDatabase(database)
                .setUser(username)
                .setPassword(password)
                .setConnectTimeout(connectionTimeout)
                .setIdleTimeout(idleTimeout)
                .setCharset("utf8mb4")
                .setCollation("utf8mb4_unicode_ci");

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(maxPoolSize)
                .setMaxWaitQueueSize(maxWaitQueueSize);

        return MySQLPool.pool(vertx, connectOptions, poolOptions);
    }
}
