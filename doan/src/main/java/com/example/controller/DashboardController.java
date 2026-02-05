package com.example.controller;

import com.example.dao.GiaoDichDAO;
import com.example.model.GiaoDich;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller cho màn hình Dashboard (JavaFX thuần)
 */
public class DashboardController {

    private Stage stage;
    private Scene scene;
    private Label lblXinChao;
    private Label lblSoTaiKhoan;
    private Label lblSoDu;
    private TableView<GiaoDichInfo> tableGiaoDich;
    private Button btnGiaoDich;
    private Button btnRefresh;
    private Button btnDangXuat;
    private GiaoDichDAO giaoDichDAO = new GiaoDichDAO();
    private DecimalFormat df = new DecimalFormat("#,###");

    // Inner class để hiển thị giao dịch
    public static class GiaoDichInfo {
        private String ngay;
        private String loai;
        private String taiKhoan;
        private String soTien;
        private String noiDung;

        public GiaoDichInfo(String ngay, String loai, String taiKhoan, String soTien, String noiDung) {
            this.ngay = ngay;
            this.loai = loai;
            this.taiKhoan = taiKhoan;
            this.soTien = soTien;
            this.noiDung = noiDung;
        }

        public String getNgay() { return ngay; }
        public String getLoai() { return loai; }
        public String getTaiKhoan() { return taiKhoan; }
        public String getSoTien() { return soTien; }
        public String getNoiDung() { return noiDung; }
    }

    public DashboardController(Stage stage) {
        this.stage = stage;
        createUI();
        loadDashboardData();
    }

    private void createUI() {
        // Root layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        // === TOP: Header ===
        VBox header = createHeader();
        root.setTop(header);

        // === CENTER: Table ===
        VBox center = createCenter();
        root.setCenter(center);

        // Tạo scene
        scene = new Scene(root, 1200, 800);
    }

    private VBox createHeader() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #2c3e50;");

        // Hàng 1: Xin chào + Đăng xuất
        HBox row1 = new HBox();
        row1.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(row1, Priority.ALWAYS);

        lblXinChao = new Label("Xin chào, " + LoginController.currentUser.getHoTen());
        lblXinChao.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblXinChao.setStyle("-fx-text-fill: white;");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        btnDangXuat = new Button("Đăng xuất");
        btnDangXuat.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        btnDangXuat.setPrefHeight(35);
        btnDangXuat.setPrefWidth(120);
        btnDangXuat.setOnAction(e -> handleDangXuat());

        row1.getChildren().addAll(lblXinChao, spacer1, btnDangXuat);

        // Hàng 2: Số tài khoản + Số dư
        HBox row2 = new HBox(30);
        row2.setAlignment(Pos.CENTER_LEFT);

        lblSoTaiKhoan = new Label("STK: " + LoginController.currentUser.getSoTaiKhoan());
        lblSoTaiKhoan.setFont(Font.font("Arial", 16));
        lblSoTaiKhoan.setStyle("-fx-text-fill: #ecf0f1;");

        Label lblSoDuTitle = new Label("Số dư: ");
        lblSoDuTitle.setFont(Font.font("Arial", 16));
        lblSoDuTitle.setStyle("-fx-text-fill: #ecf0f1;");

        lblSoDu = new Label("0 đ");
        lblSoDu.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblSoDu.setStyle("-fx-text-fill: #2ecc71;");

        row2.getChildren().addAll(lblSoTaiKhoan, lblSoDuTitle, lblSoDu);

        // Hàng 3: Các nút chức năng
        HBox row3 = new HBox(15);
        row3.setAlignment(Pos.CENTER_LEFT);

        btnGiaoDich = new Button("💸 Chuyển tiền");
        btnGiaoDich.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnGiaoDich.setPrefHeight(40);
        btnGiaoDich.setPrefWidth(150);
        btnGiaoDich.setOnAction(e -> handleGiaoDich());

        btnRefresh = new Button("🔄 Làm mới");
        btnRefresh.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnRefresh.setPrefHeight(40);
        btnRefresh.setPrefWidth(130);
        btnRefresh.setOnAction(e -> handleRefresh());
        
        Button btnNganSach = new Button("📋 Ngân sách");
        btnNganSach.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnNganSach.setPrefHeight(40);
        btnNganSach.setPrefWidth(150);
        btnNganSach.setOnAction(e -> {
            System.out.println("DEBUG: Nút Ngân sách được click!");
            handleNganSach();
        });
        
        Button btnThongKe = new Button("📊 Thống kê");
        btnThongKe.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnThongKe.setPrefHeight(40);
        btnThongKe.setPrefWidth(150);
        btnThongKe.setOnAction(e -> {
            System.out.println("DEBUG: Nút Thống kê được click!");
            handleThongKe();
        });
        
        Button btnDanhMuc = new Button("📂 Danh mục");
        btnDanhMuc.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnDanhMuc.setPrefHeight(40);
        btnDanhMuc.setPrefWidth(150);
        btnDanhMuc.setOnAction(e -> handleDanhMuc());

        row3.getChildren().addAll(btnGiaoDich, btnRefresh, btnNganSach, btnThongKe, btnDanhMuc);

        header.getChildren().addAll(row1, row2, row3);
        return header;
    }

    private VBox createCenter() {
        VBox center = new VBox(10);
        center.setPadding(new Insets(20));

        // Tiêu đề
        Label lblTitle = new Label("📊 Lịch sử giao dịch gần đây");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblTitle.setStyle("-fx-text-fill: #2c3e50;");

        // TableView
        tableGiaoDich = createTable();

        center.getChildren().addAll(lblTitle, tableGiaoDich);
        VBox.setVgrow(tableGiaoDich, Priority.ALWAYS);

        return center;
    }

    @SuppressWarnings("unchecked")
    private TableView<GiaoDichInfo> createTable() {
        TableView<GiaoDichInfo> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Các cột
        TableColumn<GiaoDichInfo, String> colNgay = new TableColumn<>("Ngày giờ");
        colNgay.setCellValueFactory(new PropertyValueFactory<>("ngay"));
        colNgay.setPrefWidth(150);

        TableColumn<GiaoDichInfo, String> colLoai = new TableColumn<>("Loại");
        colLoai.setCellValueFactory(new PropertyValueFactory<>("loai"));
        colLoai.setPrefWidth(80);
        // Format màu cho loại giao dịch
        colLoai.setCellFactory(column -> new TableCell<GiaoDichInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(item.equals("Gửi") ? 
                        "-fx-text-fill: red; -fx-font-weight: bold;" : 
                        "-fx-text-fill: green; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<GiaoDichInfo, String> colTaiKhoan = new TableColumn<>("Tài khoản");
        colTaiKhoan.setCellValueFactory(new PropertyValueFactory<>("taiKhoan"));
        colTaiKhoan.setPrefWidth(250);

        TableColumn<GiaoDichInfo, String> colSoTien = new TableColumn<>("Số tiền");
        colSoTien.setCellValueFactory(new PropertyValueFactory<>("soTien"));
        colSoTien.setPrefWidth(150);
        // Format căn phải cho số tiền
        colSoTien.setCellFactory(column -> new TableCell<GiaoDichInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<GiaoDichInfo, String> colNoiDung = new TableColumn<>("Nội dung");
        colNoiDung.setCellValueFactory(new PropertyValueFactory<>("noiDung"));
        colNoiDung.setPrefWidth(250);

        table.getColumns().addAll(colNgay, colLoai, colTaiKhoan, colSoTien, colNoiDung);
        return table;
    }

    private void loadDashboardData() {
        try {
            String soTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
            
            // Lấy số dư hiện tại
            BigDecimal soDu = giaoDichDAO.laySoDu(soTaiKhoan);
            lblSoDu.setText(df.format(soDu) + " đ");
            lblSoDu.setStyle(soDu.compareTo(BigDecimal.ZERO) >= 0 ? 
                "-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 20px;" : 
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 20px;");
            
            // Load 15 giao dịch gần nhất
            List<GiaoDich> danhSach = giaoDichDAO.layLichSuGiaoDich(soTaiKhoan);
            List<GiaoDichInfo> displayList = new ArrayList<>();
            
            for (GiaoDich gd : danhSach.stream().limit(15).collect(Collectors.toList())) {
                String ngay = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(gd.getNgayGiaoDich());
                String loai;
                String taiKhoan;
                
                if (gd.getSoTaiKhoanGui().equals(soTaiKhoan)) {
                    // Giao dịch gửi tiền
                    loai = "Gửi";
                    taiKhoan = gd.getSoTaiKhoanNhan();
                    String tenNguoiNhan = giaoDichDAO.layTenNguoiDung(gd.getSoTaiKhoanNhan());
                    if (tenNguoiNhan != null) {
                        taiKhoan += " (" + tenNguoiNhan + ")";
                    }
                } else {
                    // Giao dịch nhận tiền
                    loai = "Nhận";
                    taiKhoan = gd.getSoTaiKhoanGui();
                    String tenNguoiGui = giaoDichDAO.layTenNguoiDung(gd.getSoTaiKhoanGui());
                    if (tenNguoiGui != null) {
                        taiKhoan += " (" + tenNguoiGui + ")";
                    }
                }
                
                String soTien = df.format(gd.getSoTien()) + " đ";
                String noiDung = gd.getNoiDung() != null ? gd.getNoiDung() : "";
                
                displayList.add(new GiaoDichInfo(ngay, loai, taiKhoan, soTien, noiDung));
            }
            
            ObservableList<GiaoDichInfo> data = FXCollections.observableArrayList(displayList);
            tableGiaoDich.setItems(data);
            
        } catch (Exception e) {
            showError("Lỗi tải dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleGiaoDich() {
        TransactionController transactionController = new TransactionController(stage);
        stage.setScene(transactionController.getScene());
        stage.setTitle("Chuyển Tiền");
    }
    
    private void handleNganSach() {
        try {
            System.out.println("DEBUG: Bắt đầu handleNganSach()");
            String soTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
            System.out.println("DEBUG: Số tài khoản = " + soTaiKhoan);
            
            System.out.println("DEBUG: Đang tạo BudgetController...");
            BudgetController budgetController = new BudgetController(stage, soTaiKhoan);
            System.out.println("DEBUG: Đã tạo BudgetController thành công!");
            
            stage.setScene(budgetController.getScene());
            stage.setTitle("Quản Lý Ngân Sách");
            System.out.println("DEBUG: Đã chuyển scene thành công!");
        } catch (Exception e) {
            System.err.println("DEBUG LỖI: " + e.getMessage());
            e.printStackTrace();
            showError("Lỗi khi mở Ngân sách: " + e.getMessage());
        }
    }
    
    private void handleThongKe() {
        try {
            System.out.println("DEBUG: Bắt đầu handleThongKe()");
            String soTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
            System.out.println("DEBUG: Số tài khoản = " + soTaiKhoan);
            
            System.out.println("DEBUG: Đang tạo StatisticsController...");
            StatisticsController statisticsController = new StatisticsController(stage, soTaiKhoan);
            System.out.println("DEBUG: Đã tạo StatisticsController thành công!");
            
            stage.setScene(statisticsController.getScene());
            stage.setTitle("Thống Kê Chi Tiêu");
            System.out.println("DEBUG: Đã chuyển scene thành công!");
        } catch (Exception e) {
            System.err.println("DEBUG LỖI: " + e.getMessage());
            e.printStackTrace();
            showError("Lỗi khi mở Thống kê: " + e.getMessage());
        }
    }
    
    private void handleDanhMuc() {
        try {
            String soTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
            CategoryController categoryController = new CategoryController(stage, soTaiKhoan);
            stage.setScene(categoryController.getScene());
            stage.setTitle("Quản Lý Danh Mục");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi khi mở Danh mục: " + e.getMessage());
        }
    }

    private void handleDangXuat() {
        LoginController.currentUser = null;
        LoginController loginController = new LoginController(stage);
        stage.setScene(loginController.getScene());
        stage.setTitle("Đăng nhập");
        stage.setResizable(false);
    }

    private void handleRefresh() {
        loadDashboardData();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Scene getScene() {
        return scene;
    }
}
