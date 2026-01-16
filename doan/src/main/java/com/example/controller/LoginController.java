package com.example.controller;

import com.example.dao.NguoiDungDAO;
import com.example.model.NguoiDung;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Controller cho màn hình đăng nhập
 */
public class LoginController {

    @FXML
    private TextField txtTenDangNhap;

    @FXML
    private PasswordField txtMatKhau;

    @FXML
    private Label lblThongBao;

    @FXML
    private Button btnDangNhap;

    @FXML
    private Hyperlink linkDangKy;

    private NguoiDungDAO nguoiDungDAO = new NguoiDungDAO();

    // Biến static để lưu thông tin người dùng hiện tại
    public static NguoiDung currentUser;

    @FXML
    private void handleDangNhap() {
        String tenDangNhap = txtTenDangNhap.getText().trim();
        String matKhau = txtMatKhau.getText();

        if (tenDangNhap.isEmpty() || matKhau.isEmpty()) {
            lblThongBao.setText("Vui lòng nhập đầy đủ thông tin!");
            lblThongBao.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            // Kiểm tra đăng nhập
            NguoiDung nguoiDung = nguoiDungDAO.dangNhap(tenDangNhap, matKhau);

            if (nguoiDung != null) {
                currentUser = nguoiDung;
                lblThongBao.setText("Đăng nhập thành công!");
                lblThongBao.setStyle("-fx-text-fill: green;");

                // Delay 0.8 giây rồi chuyển màn hình
                PauseTransition pause = new PauseTransition(Duration.seconds(0.8));
                pause.setOnFinished(event -> chuyenSangDashboard());
                pause.play();
            } else {
                lblThongBao.setText("Tên đăng nhập hoặc mật khẩu không đúng!");
                lblThongBao.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            lblThongBao.setText("Lỗi: " + e.getMessage());
            lblThongBao.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDangKy() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnDangNhap.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 600));
            stage.setTitle("Đăng ký");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chuyenSangDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnDangNhap.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Quản Lý Chi Tiêu - Trang Chủ");
            stage.setResizable(true);
            stage.setMaximized(true);
        } catch (IOException e) {
            lblThongBao.setText("Lỗi chuyển màn hình: " + e.getMessage());
            lblThongBao.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
}
