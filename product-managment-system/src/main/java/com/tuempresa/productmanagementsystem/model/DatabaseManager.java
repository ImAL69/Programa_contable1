package com.tuempresa.productmanagementsystem.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:products.db";

    public DatabaseManager() {
        initializeDatabase();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "barcode TEXT PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "price REAL NOT NULL," +
                    "stock INTEGER NOT NULL)");
            
            System.out.println("Base de datos inicializada correctamente.");
        } catch (SQLException e) {
            System.out.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }

    public void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Conexión a la base de datos establecida correctamente.");
        } catch (SQLException e) {
            System.out.println("Error al conectar con la base de datos: " + e.getMessage());
        }
    }
}