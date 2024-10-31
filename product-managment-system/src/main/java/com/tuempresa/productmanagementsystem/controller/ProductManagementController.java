package com.tuempresa.productmanagementsystem.controller;

import com.tuempresa.productmanagementsystem.model.DatabaseManager;
import com.tuempresa.productmanagementsystem.model.Product;
import com.tuempresa.productmanagementsystem.model.ProductManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Map;
import java.util.Optional;

public class ProductManagementController {

    @FXML private TextField barcodeField;
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private TextField searchField;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> barcodeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;

    private ProductManager productManager;
    private ObservableList<Product> productList;

    @FXML
    public void initialize() {
        DatabaseManager dbManager = new DatabaseManager();
        productManager = new ProductManager(dbManager);

        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));

        updateProductTable();
    }

    private void updateProductTable() {
        productList = FXCollections.observableArrayList(productManager.getProducts());
        productTable.setItems(productList);
    }

    @FXML
    private void handleScanBarcode() {
        String barcode = barcodeField.getText();
        Product product = productManager.findProductByBarcode(barcode);
        if (product != null) {
            nameField.setText(product.getName());
            priceField.setText(String.valueOf(product.getPrice()));
            stockField.setText(String.valueOf(product.getStock()));
        } else {
            showAlert("Producto no encontrado", "No se encontró ningún producto con el código de barras " + barcode);
        }
    }

    @FXML
    private void handleAddOrUpdateProduct() {
        String barcode = barcodeField.getText();
        String name = nameField.getText();
        String priceText = priceField.getText();
        String stockText = stockField.getText();

        if (barcode.isEmpty() || name.isEmpty() || priceText.isEmpty() || stockText.isEmpty()) {
            showAlert("Error", "Todos los campos son obligatorios.");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int stock = Integer.parseInt(stockText);
            Product product = new Product(barcode, name, price, stock);
            productManager.addOrUpdateProduct(product);
            updateProductTable();
            clearFields();
            showAlert("Éxito", "Producto agregado/actualizado correctamente.");
        } catch (NumberFormatException e) {
            showAlert("Error", "El precio debe ser un número válido y las existencias un número entero.");
        }
    }

    @FXML
    private void handleEditProduct() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            barcodeField.setText(selectedProduct.getBarcode());
            nameField.setText(selectedProduct.getName());
            priceField.setText(String.valueOf(selectedProduct.getPrice()));
            stockField.setText(String.valueOf(selectedProduct.getStock()));
        } else {
            showAlert("Seleccionar producto", "Por favor, seleccione un producto para editar.");
        }
    }

    @FXML
    private void handleDeleteProduct() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText(null);
            alert.setContentText("¿Está seguro de que desea eliminar este producto?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                productManager.deleteProduct(selectedProduct.getBarcode());
                updateProductTable();
                clearFields();
            }
        } else {
            showAlert("Seleccionar producto", "Por favor, seleccione un producto para eliminar.");
        }
    }

    @FXML
    private void handleSellProduct() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Vender producto");
            dialog.setHeaderText("Existencias actuales: " + selectedProduct.getStock());
            dialog.setContentText("Ingrese la cantidad a vender:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(quantityStr -> {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    if (quantity > 0 && quantity <= selectedProduct.getStock()) {
                        if (productManager.sellProduct(selectedProduct.getBarcode(), quantity)) {
                            updateProductTable();
                            showAlert("Éxito", "Se vendieron " + quantity + " unidades del producto.");
                        } else {
                            showAlert("Error", "No se pudo realizar la venta. Verifique las existencias.");
                        }
                    } else {
                        showAlert("Error", "La cantidad debe ser mayor que 0 y no exceder las existencias actuales.");
                    }
                } catch (NumberFormatException e) {
                    showAlert("Error", "Por favor, ingrese un número válido.");
                }
            });
        } else {
            showAlert("Seleccionar producto", "Por favor, seleccione un producto para vender.");
        }
    }

    @FXML
    private void handleSearchProducts() {
        String searchTerm = searchField.getText().toLowerCase();
        ObservableList<Product> filteredList = FXCollections.observableArrayList();
        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(searchTerm) || product.getBarcode().contains(searchTerm)) {
                filteredList.add(product);
            }
        }
        productTable.setItems(filteredList);
    }

    private void clearFields() {
        barcodeField.clear();
        nameField.clear();
        priceField.clear();
        stockField.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void handleClosing() {
        Map<String, Integer> totalSales = productManager.getTotalSales();
        if (!totalSales.isEmpty()) {
            StringBuilder message = new StringBuilder("Resumen de ventas:\n\n");
            for (Map.Entry<String, Integer> entry : totalSales.entrySet()) {
                Product product = productManager.findProductByBarcode(entry.getKey());
                if (product != null) {
                    message.append(product.getName()).append(": ").append(entry.getValue()).append(" unidades\n");
                }
            }
            showAlert("Resumen de ventas", message.toString());
        } else {
            showAlert("Resumen de ventas", "No se realizaron ventas.");
        }
    }
}
