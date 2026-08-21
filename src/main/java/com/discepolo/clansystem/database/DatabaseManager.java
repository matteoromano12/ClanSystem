package com.discepolo.clansystem.database;

import com.discepolo.clansystem.ClanSystem;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseManager {

    private final ClanSystem plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(ClanSystem plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        var cfg = plugin.getConfig();

        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl("jdbc:mariadb://%s:%d/%s".formatted(
                cfg.getString("database.host"),
                cfg.getInt("database.port"),
                cfg.getString("database.name")));
        hikari.setUsername(cfg.getString("database.user"));
        hikari.setPassword(cfg.getString("database.password"));
        hikari.setMaximumPoolSize(cfg.getInt("database.pool-size", 8));
        hikari.setPoolName("ClanSystem-Pool");
        hikari.setDriverClassName("org.mariadb.jdbc.Driver");

        try {
            dataSource = new HikariDataSource(hikari);
            try (Connection conn = getConnection()) {
                conn.isValid(2);
            }
            runSchema();
            plugin.getLogger().info("Connesso a MariaDB (" + cfg.getString("database.name") + ").");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Connessione a MariaDB fallita:");
            e.printStackTrace();
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private void runSchema() throws Exception {
        String sql;
        try (InputStream in = plugin.getResource("schema.sql")) {
            if (in == null) throw new IllegalStateException("schema.sql non trovato nel jar");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }
        }

        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {

            for (String statement : sql.split(";")) {
                if (statement.isBlank()) continue;
                try {
                    st.execute(statement);
                } catch (SQLException e) {
                    if (!e.getMessage().contains("Duplicate key name")) throw e;
                }
            }
        }
    }
}