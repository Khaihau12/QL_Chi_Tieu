package com.example.controller;

import com.example.dao.DanhMucDAO;
import com.example.dao.GiaoDichDAO;
import com.example.dao.NganSachDAO;
import com.example.dao.NguoiDungDAO;
import com.example.model.DanhMuc;
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
import java.time.LocalDate;
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
    private NguoiDungDAO nguoiDungDAO = new NguoiDungDAO();
    private DanhMucDAO danhMucDAO = new DanhMucDAO();
    private NganSachDAO nganSachDAO = new NganSachDAO();
    private DecimalFormat df = new DecimalFormat("#,###");
    // Lưu danh sách GiaoDich gốc để sử dụng khi đổi danh mục
    private List<GiaoDich> rawGiaoDichList = new ArrayList<>();

    // Inner class để hiển thị giao dịch
    public static class GiaoDichInfo {
        private int maGiaoDich;
        private String ngay;
        private String loai;
        private String taiKhoan;
        private String soTien;
        private String noiDung;
        private String danhMuc;       // Tên danh mục để hiển thị
        // Internal fields (không phải column)
        private Integer danhMucId;    // ID danh mục chi (nếu Gùi)
        private Integer danhMucThuId; // ID danh mục thu (nếu Nhạn)
        private BigDecimal soTienRaw;
        private java.sql.Timestamp ngayGiaoDichRaw;

        public GiaoDichInfo(int maGiaoDich, String ngay, String loai, String taiKhoan,
                            String soTien, String noiDung, String danhMuc,
                            Integer danhMucId, Integer danhMucThuId,
                            BigDecimal soTienRaw, java.sql.Timestamp ngayGiaoDichRaw) {
            this.maGiaoDich = maGiaoDich;
            this.ngay = ngay;
            this.loai = loai;
            this.taiKhoan = taiKhoan;
            this.soTien = soTien;
            this.noiDung = noiDung;
            this.danhMuc = danhMuc;
            this.danhMucId = danhMucId;
            this.danhMucThuId = danhMucThuId;
            this.soTienRaw = soTienRaw;
            this.ngayGiaoDichRaw = ngayGiaoDichRaw;
        }

        // Backward-compat constructor (legacy code)
        public GiaoDichInfo(String ngay, String loai, String taiKhoan, String soTien, String noiDung) {
            this(0, ngay, loai, taiKhoan, soTien, noiDung, "", null, null, BigDecimal.ZERO, null);
        }

        public int getMaGiaoDich() { return maGiaoDich; }
        public String getNgay() { return ngay; }
        public String getLoai() { return loai; }
        public String getTaiKhoan() { return taiKhoan; }
        public String getSoTien() { return soTien; }
        public String getNoiDung() { return noiDung; }
        public String getDanhMuc() { return danhMuc; }
        public void setDanhMuc(String danhMuc) { this.danhMuc = danhMuc; }
        public Integer getDanhMucId() { return danhMucId; }
        public void setDanhMucId(Integer id) { this.danhMucId = id; }
        public Integer getDanhMucThuId() { return danhMucThuId; }
        public void setDanhMucThuId(Integer id) { this.danhMucThuId = id; }
        public BigDecimal getSoTienRaw() { return soTienRaw; }
        public java.sql.Timestamp getNgayGiaoDichRaw() { return ngayGiaoDichRaw; }
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

        Button btnDoiMatKhau = new Button("🔑 Đổi mật khẩu");
        btnDoiMatKhau.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        btnDoiMatKhau.setPrefHeight(40);
        btnDoiMatKhau.setPrefWidth(160);
        btnDoiMatKhau.setOnAction(e -> handleDoiMatKhau());

        row3.getChildren().addAll(btnGiaoDich, btnRefresh, btnNganSach, btnThongKe, btnDanhMuc, btnDoiMatKhau);

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

        // Nút đổi danh mục
        Button btnDoiDanhMuc = new Button("↙️ Đổi danh mục");
        btnDoiDanhMuc.setStyle("-fx-background-color: #16a085; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        btnDoiDanhMuc.setPrefHeight(35);
        btnDoiDanhMuc.setOnAction(e -> handleDoiDanhMuc());

        HBox titleRow = new HBox(15);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        titleRow.getChildren().addAll(lblTitle, sp, btnDoiDanhMuc);

        // TableView
        tableGiaoDich = createTable();

        center.getChildren().addAll(titleRow, tableGiaoDich);
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
        colNoiDung.setPrefWidth(200);

        TableColumn<GiaoDichInfo, String> colDanhMuc = new TableColumn<>("Danh mục");
        colDanhMuc.setCellValueFactory(new PropertyValueFactory<>("danhMuc"));
        colDanhMuc.setPrefWidth(160);
        colDanhMuc.setCellFactory(col -> new TableCell<GiaoDichInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                boolean blank = item.isBlank();
                setText(blank ? "— Chưa phân loại" : item);
                if (blank) setStyle("-fx-text-fill: #bdc3c7; -fx-font-style: italic;");
                else {
                    // Thu thì xanh lá, Chi thì cam
                    GiaoDichInfo row = getTableView().getItems().get(getIndex());
                    if ("Nhận".equals(row.getLoai()))
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    else
                        setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                }
            }
        });

        table.getColumns().addAll(colNgay, colLoai, colTaiKhoan, colSoTien, colNoiDung, colDanhMuc);
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
            rawGiaoDichList = new ArrayList<>(danhSach);
            List<GiaoDichInfo> displayList = new ArrayList<>();
            
            for (GiaoDich gd : danhSach.stream().limit(15).collect(Collectors.toList())) {
                String ngay = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(gd.getNgayGiaoDich());
                String loai;
                String taiKhoan;
                String danhMucHienThi;

                if (gd.getSoTaiKhoanGui().equals(soTaiKhoan)) {
                    loai = "Gửi";
                    taiKhoan = gd.getSoTaiKhoanNhan();
                    String tenNguoiNhan = giaoDichDAO.layTenNguoiDung(gd.getSoTaiKhoanNhan());
                    if (tenNguoiNhan != null) taiKhoan += " (" + tenNguoiNhan + ")";
                    danhMucHienThi = gd.getTenDanhMucChi() != null ? gd.getTenDanhMucChi() : "";
                } else {
                    loai = "Nhận";
                    taiKhoan = gd.getSoTaiKhoanGui();
                    String tenNguoiGui = giaoDichDAO.layTenNguoiDung(gd.getSoTaiKhoanGui());
                    if (tenNguoiGui != null) taiKhoan += " (" + tenNguoiGui + ")";
                    danhMucHienThi = gd.getTenDanhMucThu() != null ? gd.getTenDanhMucThu() : "";
                }
                
                String soTien = df.format(gd.getSoTien()) + " đ";
                String noiDung = gd.getNoiDung() != null ? gd.getNoiDung() : "";
                
                displayList.add(new GiaoDichInfo(
                    gd.getMaGiaoDich(), ngay, loai, taiKhoan, soTien, noiDung,
                    danhMucHienThi, gd.getDanhMucId(), gd.getDanhMucThuId(),
                    gd.getSoTien(), gd.getNgayGiaoDich()));
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
        stage.setResizable(false);
        stage.setWidth(1200);
        stage.setHeight(830);
        stage.centerOnScreen();
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
            stage.setResizable(false);
            stage.setWidth(1200);
            stage.setHeight(830);
            stage.centerOnScreen();
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
            stage.setResizable(false);
            stage.setWidth(1200);
            stage.setHeight(830);
            stage.centerOnScreen();
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
            stage.setResizable(false);
            stage.setWidth(1200);
            stage.setHeight(830);
            stage.centerOnScreen();
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
        stage.setWidth(520);
        stage.setHeight(590);
        stage.centerOnScreen();
    }

    private void handleRefresh() {
        loadDashboardData();
    }

    /** Đổi danh mục của giao dịch đang chọn, có cảnh báo ngân sách khi đổi loại chi */
    private void handleDoiDanhMuc() {
        GiaoDichInfo selected = tableGiaoDich.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn giao dịch cần đổi danh mục!", ButtonType.OK).showAndWait();
            return;
        }

        boolean isGui = "Gửi".equals(selected.getLoai());
        String soTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
        String loaiDM = isGui ? "chi" : "thu";
        List<DanhMuc> dsDanhMuc = danhMucDAO.layDanhMucTheoLoai(soTaiKhoan, loaiDM);

        Dialog<DanhMuc> dialog = new Dialog<>();
        dialog.setTitle("Đổi danh mục");
        dialog.setHeaderText((isGui ? "🟠 Danh mục Chi" : "🟢 Danh mục Thu") +
                " — Giao dịch ngày " + selected.getNgay());

        ComboBox<DanhMuc> cbDM = new ComboBox<>();
        cbDM.getItems().addAll(dsDanhMuc);
        cbDM.setPrefWidth(300);

        // Chọn danh mục hiện tại
        Integer curId = isGui ? selected.getDanhMucId() : selected.getDanhMucThuId();
        if (curId != null) {
            dsDanhMuc.stream().filter(d -> d.getId() == curId).findFirst()
                    .ifPresent(cbDM::setValue);
        }

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().addAll(new Label("Chọn danh mục mới:"), cbDM);
        dialog.getDialogPane().setContent(content);
        ButtonType btnOK = new ButtonType("✔ Xác nhận", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> btn == btnOK ? cbDM.getValue() : null);

        dialog.showAndWait().ifPresent(newDM -> {
            if (newDM == null) return;

            // === Kiểm tra ngân sách (chỉ áp dụng cho giao dịch gửi = chi tiêu) ===
            if (isGui) {
                try {
                    java.sql.Timestamp ts = selected.getNgayGiaoDichRaw();
                    LocalDate ngayGD = ts != null
                            ? ts.toLocalDateTime().toLocalDate()
                            : LocalDate.now();
                    int thang = ngayGD.getMonthValue();
                    int nam   = ngayGD.getYear();

                    BigDecimal gioiHan = nganSachDAO.layGioiHanNganSach(
                            soTaiKhoan, newDM.getId(), thang, nam);

                    if (gioiHan != null) {
                        double daChi = nganSachDAO.layTongChiTheoDanhMuc(
                                soTaiKhoan, newDM.getId(), thang, nam);
                        BigDecimal tongSau = BigDecimal.valueOf(daChi).add(selected.getSoTienRaw());
                        if (tongSau.compareTo(gioiHan) > 0) {
                            String msg = String.format(
                                    "⚠️ Cảnh báo ngân sách!%n" +
                                    "Danh mục: %s%n" +
                                    "Hạn mức tháng %d/%d: %s đ%n" +
                                    "Đã chi: %s đ%n" +
                                    "Giao dịch này: +%s đ%n" +
                                    "→ Tổng sau khi chuyển: %s đ (vượt %s đ)%n%n" +
                                    "Bạn vẫn muốn chuyển sang danh mục này?",
                                    newDM.getTenDanhMuc(), thang, nam,
                                    df.format(gioiHan),
                                    df.format(daChi),
                                    df.format(selected.getSoTienRaw()),
                                    df.format(tongSau),
                                    df.format(tongSau.subtract(gioiHan)));
                            Alert warn = new Alert(Alert.AlertType.CONFIRMATION, msg,
                                    ButtonType.YES, ButtonType.NO);
                            warn.setTitle("Vượt hạn mức ngân sách");
                            warn.setHeaderText(null);
                            java.util.Optional<ButtonType> ans = warn.showAndWait();
                            if (ans.isEmpty() || ans.get() != ButtonType.YES) return;
                        }
                    }

                    giaoDichDAO.capNhatDanhMucChi(selected.getMaGiaoDich(), newDM.getId());
                } catch (Exception ex) {
                    showError("Lỗi cập nhật: " + ex.getMessage());
                    return;
                }
            } else {
                try {
                    giaoDichDAO.capNhatDanhMucThu(selected.getMaGiaoDich(), newDM.getId());
                } catch (Exception ex) {
                    showError("Lỗi cập nhật: " + ex.getMessage());
                    return;
                }
            }

            Alert ok = new Alert(Alert.AlertType.INFORMATION,
                    "✅ Đã đổi danh mục thành công: " + newDM.getTenDanhMuc(), ButtonType.OK);
            ok.setHeaderText(null);
            ok.showAndWait();
            loadDashboardData();
        });
    }

    private void handleDoiMatKhau() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Đổi mật khẩu");
        dialog.setHeaderText("🔑  Đổi mật khẩu tài khoản: " + LoginController.currentUser.getTenDangNhap());

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        PasswordField txtCu = new PasswordField();
        txtCu.setPromptText("Nhập mật khẩu hiện tại");
        txtCu.setPrefWidth(280);

        PasswordField txtMoi = new PasswordField();
        txtMoi.setPromptText("Tối thiểu 6 ký tự");
        txtMoi.setPrefWidth(280);

        PasswordField txtXacNhan = new PasswordField();
        txtXacNhan.setPromptText("Nhập lại mật khẩu mới");
        txtXacNhan.setPrefWidth(280);

        Label lblKetQua = new Label("");
        lblKetQua.setWrapText(true);

        grid.add(new Label("Mật khẩu hiện tại:"), 0, 0);
        grid.add(txtCu, 1, 0);
        grid.add(new Label("Mật khẩu mới:"), 0, 1);
        grid.add(txtMoi, 1, 1);
        grid.add(new Label("Xác nhận mật khẩu:"), 0, 2);
        grid.add(txtXacNhan, 1, 2);
        grid.add(lblKetQua, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType btnLuu = new ButtonType("💾 Lưu mật khẩu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnLuu, ButtonType.CANCEL);

        // Validate khi nhấn Lưu (dùng event filter để chặn dialog đóng khi lỗi)
        javafx.scene.Node btnLuuNode = dialog.getDialogPane().lookupButton(btnLuu);
        btnLuuNode.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String cu = txtCu.getText();
            String moi = txtMoi.getText();
            String xn = txtXacNhan.getText();

            if (cu.isEmpty() || moi.isEmpty() || xn.isEmpty()) {
                lblKetQua.setText("⚠️ Vui lòng điền đầy đủ thông tin!");
                lblKetQua.setStyle("-fx-text-fill: red;");
                ev.consume(); // chặn đóng
                return;
            }
            if (moi.length() < 6) {
                lblKetQua.setText("⚠️ Mật khẩu mới phải ít nhất 6 ký tự!");
                lblKetQua.setStyle("-fx-text-fill: red;");
                ev.consume();
                return;
            }
            if (!moi.equals(xn)) {
                lblKetQua.setText("⚠️ Mật khẩu xác nhận không khớp!");
                lblKetQua.setStyle("-fx-text-fill: red;");
                ev.consume();
                return;
            }
            try {
                boolean ok = nguoiDungDAO.doiMatKhau(
                        LoginController.currentUser.getMaNguoiDung(), cu, moi);
                if (!ok) {
                    lblKetQua.setText("❌ Mật khẩu hiện tại không đúng!");
                    lblKetQua.setStyle("-fx-text-fill: red;");
                    ev.consume();
                } // nếu ok thì cho dialog đóng bình thường
            } catch (Exception ex) {
                lblKetQua.setText("Lỗi: " + ex.getMessage());
                lblKetQua.setStyle("-fx-text-fill: red;");
                ev.consume();
            }
        });

        dialog.setResultConverter(btn -> null);

        boolean[] changed = {false};
        btnLuuNode.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            // nếu đạt đây thì đã pass validation (event không bị consumed)
            changed[0] = !ev.isConsumed();
        });

        dialog.showAndWait();
        if (changed[0]) {
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Đổi mật khẩu");
            ok.setHeaderText(null);
            ok.setContentText("✅ Đổi mật khẩu thành công!");
            ok.showAndWait();
        }
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
