package com.example.controller;

import com.example.dao.NguoiDungDAO;
import com.example.model.NguoiDung;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller cho màn hình đăng nhập (JavaFX thuần)
 */
public class LoginController {

    private Stage stage;
    private Scene scene;
    private TextField txtTenDangNhap;
    private PasswordField txtMatKhau;
    private Label lblThongBao;
    private Button btnDangNhap;
    private Hyperlink linkDangKy;
    private NguoiDungDAO nguoiDungDAO = new NguoiDungDAO();

    // Biến static để lưu thông tin người dùng hiện tại
    public static NguoiDung currentUser;

    public LoginController(Stage stage) {
        this.stage = stage;
        createUI();
    }

    private void createUI() {
        // Root layout
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f0f0f0;");

        // Tiêu đề
        Label lblTieuDe = new Label("ĐĂNG NHẬP");
        lblTieuDe.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblTieuDe.setStyle("-fx-text-fill: #2c3e50;");

        Label lblPhuDe = new Label("Quản Lý Chi Tiêu");
        lblPhuDe.setFont(Font.font("Arial", 16));
        lblPhuDe.setStyle("-fx-text-fill: #7f8c8d;");

        // Form đăng nhập
        Label lblTenDangNhap = new Label("Tên đăng nhập:");
        lblTenDangNhap.setFont(Font.font("Arial", 13));
        txtTenDangNhap = new TextField();
        txtTenDangNhap.setPromptText("Nhập tên đăng nhập");
        txtTenDangNhap.setPrefWidth(350);
        txtTenDangNhap.setStyle("-fx-font-size: 13px;");

        Label lblMatKhau = new Label("Mật khẩu:");
        lblMatKhau.setFont(Font.font("Arial", 13));
        txtMatKhau = new PasswordField();
        txtMatKhau.setPromptText("Nhập mật khẩu");
        txtMatKhau.setPrefWidth(350);
        txtMatKhau.setStyle("-fx-font-size: 13px;");

        // Nút đăng nhập
        btnDangNhap = new Button("Đăng nhập");
        btnDangNhap.setPrefWidth(350);
        btnDangNhap.setPrefHeight(40);
        btnDangNhap.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnDangNhap.setOnAction(e -> handleDangNhap());

        // Thông báo
        lblThongBao = new Label("");
        lblThongBao.setFont(Font.font("Arial", 12));
        lblThongBao.setWrapText(true);
        lblThongBao.setMaxWidth(350);

        // Link đăng ký
        linkDangKy = new Hyperlink("Chưa có tài khoản? Đăng ký ngay");
        linkDangKy.setStyle("-fx-font-size: 12px;");
        linkDangKy.setOnAction(e -> handleDangKy());

        // Thêm vào layout
        root.getChildren().addAll(
            lblTieuDe,
            lblPhuDe,
            new Label(""), // spacer
            lblTenDangNhap,
            txtTenDangNhap,
            lblMatKhau,
            txtMatKhau,
            btnDangNhap,
            lblThongBao,
            linkDangKy
        );

        // Tạo scene
        scene = new Scene(root, 500, 550);
    }

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

    private void handleDangKy() {
        RegisterController registerController = new RegisterController(stage);
        stage.setScene(registerController.getScene());
        stage.setTitle("Đăng ký");
    }

    private void chuyenSangDashboard() {
        DashboardController dashboardController = new DashboardController(stage);
        stage.setScene(dashboardController.getScene());
        stage.setTitle("Quản Lý Chi Tiêu - Trang Chủ");
        stage.setResizable(true);
        stage.setMaximized(true);
    }

    public Scene getScene() {
        return scene;
    }
}
