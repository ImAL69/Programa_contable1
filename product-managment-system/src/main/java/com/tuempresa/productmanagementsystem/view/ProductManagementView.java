package com.tuempresa.productmanagementsystem.view;

import com.tuempresa.productmanagementsystem.controller.ProductManagementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ProductManagementView {

    private Stage stage;
    private ProductManagementController controller;

    public ProductManagementView(Stage stage) {
        this.stage = stage;
    }

    public void initialize() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProductManagementView.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Sistema de Gestión de Productos");
        stage.setOnCloseRequest(event -> controller.handleClosing());
        stage.show();
    }

    public ProductManagementController getController() {
        return controller;
    }
}
