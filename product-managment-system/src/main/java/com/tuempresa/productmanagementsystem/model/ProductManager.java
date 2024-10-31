package com.tuempresa.productmanagementsystem.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductManager {
    private DatabaseManager dbManager;
    private Map<String, Integer> sales;

    public ProductManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.sales = new HashMap<>();
    }

    public List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT barcode, name, price, stock FROM products";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product product = new Product(
                        rs.getString("barcode"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                );
                products.add(product);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener productos: " + e.getMessage());
        }

        return products;
    }

    public void addOrUpdateProduct(Product product) {
        String sql = "INSERT OR REPLACE INTO products (barcode, name, price, stock) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getBarcode());
            pstmt.setString(2, product.getName());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getStock());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al agregar/actualizar producto: " + e.getMessage());
        }
    }

    public void deleteProduct(String barcode) {
        String sql = "DELETE FROM products WHERE barcode = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, barcode);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al eliminar producto: " + e.getMessage());
        }
    }

    public Product findProductByBarcode(String barcode) {
        String sql = "SELECT barcode, name, price, stock FROM products WHERE barcode = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, barcode);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Product(
                        rs.getString("barcode"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar producto: " + e.getMessage());
        }

        return null;
    }

    public boolean sellProduct(String barcode, int quantity) {
        Product product = findProductByBarcode(barcode);
        if (product != null && product.getStock() >= quantity) {
            product.setStock(product.getStock() - quantity);
            addOrUpdateProduct(product);
            sales.merge(barcode, quantity, Integer::sum);
            return true;
        }
        return false;
    }

    public Map<String, Integer> getTotalSales() {
        return sales;
    }
}