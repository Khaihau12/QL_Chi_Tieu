package com.example;

import com.example.controller.LoginController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App - Quản Lý Chi Tiêu
 * Phiên bản JavaFX thuần (không dùng FXML)
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        // Khởi tạo màn hình đăng nhập
        LoginController loginController = new LoginController(stage);
        
        Scene scene = loginController.getScene();
        stage.setScene(scene);
        stage.setTitle("Đăng nhập - Quản Lý Chi Tiêu");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}