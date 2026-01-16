package com.example.controller;

import com.example.dao.NguoiDungDAO;
import com.example.model.NguoiDung;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller cho màn hình đăng ký
 */
public class RegisterController {

    @FXML
    private TextField txtTenDangNhap;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtHoTen;

    @FXML
    private PasswordField txtMatKhau;

    @FXML
    private PasswordField txtXacNhanMatKhau;

    @FXML
    private Label lblThongBao;

    @FXML
    private Button btnDangKy;

    @FXML
    private Hyperlink linkDangNhap;

    private NguoiDungDAO nguoiDungDAO = new NguoiDungDAO();

    @FXML
    private void handleDangKy() {
        String tenDangNhap = txtTenDangNhap.getText().trim();
        String email = txtEmail.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String matKhau = txtMatKhau.getText();
        String xacNhanMatKhau = txtXacNhanMatKhau.getText();

        // Kiểm tra validation
        if (tenDangNhap.isEmpty() || email.isEmpty() || hoTen.isEmpty() || 
            matKhau.isEmpty() || xacNhanMatKhau.isEmpty()) {
            lblThongBao.setText("Vui lòng nhập đầy đủ thông tin!");
            lblThongBao.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!matKhau.equals(xacNhanMatKhau)) {
            lblThongBao.setText("Mật khẩu xác nhận không khớp!");
            lblThongBao.setStyle("-fx-text-fill: red;");
            return;
        }

        if (matKhau.length() < 6) {
            lblThongBao.setText("Mật khẩu phải có ít nhất 6 ký tự!");
            lblThongBao.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            // Tạo đối tượng người dùng mới (mật khẩu chưa mã hóa - đơn giản)
            NguoiDung nguoiDung = new NguoiDung(tenDangNhap, matKhau, hoTen, email);

            boolean thanhCong = nguoiDungDAO.dangKy(nguoiDung);

            if (thanhCong) {
                lblThongBao.setText("Đăng ký thành công! Đang chuyển về đăng nhập...");
                lblThongBao.setStyle("-fx-text-fill: green;");

                // Delay 1.5s rồi chuyển về màn hình đăng nhập
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        javafx.application.Platform.runLater(this::chuyenVeDangNhap);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate entry")) {
                lblThongBao.setText("Tên đăng nhập hoặc email đã tồn tại!");
            } else {
                lblThongBao.setText("Lỗi: " + e.getMessage());
            }
            lblThongBao.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleQuayLai() {
        chuyenVeDangNhap();
    }

    private void chuyenVeDangNhap() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnDangKy.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 550));
            stage.setTitle("Đăng nhập");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
