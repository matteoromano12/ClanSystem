package com.discepolo.clansystem.database;

import com.discepolo.clansystem.ClanSystem;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

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
}