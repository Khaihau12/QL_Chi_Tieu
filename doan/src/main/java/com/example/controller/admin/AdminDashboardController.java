package com.example.controller.admin;

import com.example.controller.LoginController;
import com.example.model.NguoiDung;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Controller tổng cho trang Admin, chỉ phụ trách điều hướng giữa các panel con.
 */
public class AdminDashboardController {

    private final Stage stage;
    private Scene scene;
    private BorderPane root;
    private StackPane contentArea;

    private final AdminAccountController accountController;
    private final AdminPasswordController passwordController;
    private final AdminExpenseCategoryController expenseCategoryController;
    private final AdminIncomeCategoryController incomeCategoryController;
    private final AdminReportController reportController;

    // Nav buttons
    private Button btnNavTaiKhoan;
    private Button btnNavDoiMatKhau;
    private Button btnNavDanhMuc;
    private Button btnNavDanhMucThu;
    private Button btnNavBaoCao;

    public AdminDashboardController(Stage stage) {
        this.stage = stage;
        this.accountController = new AdminAccountController();
        this.passwordController = new AdminPasswordController();
        this.expenseCategoryController = new AdminExpenseCategoryController();
        this.incomeCategoryController = new AdminIncomeCategoryController();
        this.reportController = new AdminReportController();
        createUI();
    }

    private void createUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #ecf0f1;");
        root.setTop(createHeader());
        root.setLeft(createNavBar());

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        root.setCenter(contentArea);

        showPanel(accountController.getPanel());
        accountController.refresh();
        setActiveNav(btnNavTaiKhoan);

        scene = new Scene(root, 1200, 800);
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #2c3e50;");

        Label lblTitle = new Label("BẢNG ĐIỀU KHIỂN QUẢN TRỊ");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblTitle.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        NguoiDung admin = LoginController.currentUser;
        String tenAdmin = (admin != null && admin.getHoTen() != null) ? admin.getHoTen() : "Admin";
        Label lblAdmin = new Label(tenAdmin);
        lblAdmin.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 13px;");

        Button btnDangXuat = new Button("Đăng xuất");
        btnDangXuat.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnDangXuat.setOnAction(e -> handleDangXuat());

        header.getChildren().addAll(lblTitle, spacer, lblAdmin, new Label("    "), btnDangXuat);
        return header;
    }

    private VBox createNavBar() {
        VBox nav = new VBox(5);
        nav.setPadding(new Insets(20, 10, 20, 10));
        nav.setStyle("-fx-background-color: #34495e;");
        nav.setPrefWidth(215);

        Label lblMenu = new Label("CHỨC NĂNG");
        lblMenu.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px; -fx-font-weight: bold;");
        lblMenu.setPadding(new Insets(0, 0, 10, 5));

        btnNavTaiKhoan = createNavButton("Quản lý tài khoản");
        btnNavDoiMatKhau = createNavButton("Đổi mật khẩu");
        btnNavDanhMuc = createNavButton("Danh mục Chi mặc định");
        btnNavDanhMucThu = createNavButton("Danh mục Thu mặc định");
        btnNavBaoCao = createNavButton("Báo cáo hệ thống");

        btnNavTaiKhoan.setOnAction(e -> {
            showPanel(accountController.getPanel());
            accountController.refresh();
            setActiveNav(btnNavTaiKhoan);
        });
        btnNavDoiMatKhau.setOnAction(e -> {
            showPanel(passwordController.getPanel());
            setActiveNav(btnNavDoiMatKhau);
        });
        btnNavDanhMuc.setOnAction(e -> {
            showPanel(expenseCategoryController.getPanel());
            expenseCategoryController.refresh();
            setActiveNav(btnNavDanhMuc);
        });
        btnNavDanhMucThu.setOnAction(e -> {
            showPanel(incomeCategoryController.getPanel());
            incomeCategoryController.refresh();
            setActiveNav(btnNavDanhMucThu);
        });
        btnNavBaoCao.setOnAction(e -> {
            showPanel(reportController.getPanel());
            reportController.refresh();
            setActiveNav(btnNavBaoCao);
        });

        nav.getChildren().addAll(lblMenu, btnNavTaiKhoan, btnNavDoiMatKhau, btnNavDanhMuc, btnNavDanhMucThu, btnNavBaoCao);
        return nav;
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(195);
        btn.setPrefHeight(42);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(0, 10, 0, 10));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 13px; -fx-cursor: hand;");
        return btn;
    }

    private void setActiveNav(Button active) {
        for (Button btn : new Button[]{btnNavTaiKhoan, btnNavDoiMatKhau, btnNavDanhMuc, btnNavDanhMucThu, btnNavBaoCao}) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 13px; -fx-cursor: hand;");
        }
        active.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    private void showPanel(VBox panel) {
        contentArea.getChildren().setAll(panel);
    }

    private void handleDangXuat() {
        LoginController.currentUser = null;
        LoginController loginController = new LoginController(stage);
        stage.setScene(loginController.getScene());
        stage.setTitle("Quản Lý Chi Tiêu - Đăng Nhập");
        stage.setResizable(false);
        stage.setWidth(520);
        stage.setHeight(590);
        stage.centerOnScreen();
    }

    public Scene getScene() {
        return scene;
    }
}
