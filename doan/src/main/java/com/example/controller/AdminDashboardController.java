package com.example.controller;

import com.example.dao.DanhMucDAO;
import com.example.dao.GiaoDichDAO;
import com.example.dao.NguoiDungDAO;
import com.example.model.DanhMuc;
import com.example.model.NguoiDung;
import com.example.util.MoneyInputUtil;
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
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller cho màn hình quản trị Admin
 */
public class AdminDashboardController {

    private Stage stage;
    private Scene scene;
    private BorderPane root;
    private StackPane contentArea;

    private NguoiDungDAO nguoiDungDAO = new NguoiDungDAO();
    private DanhMucDAO danhMucDAO = new DanhMucDAO();
    private GiaoDichDAO giaoDichDAO = new GiaoDichDAO();
    private DecimalFormat df = new DecimalFormat("#,###");

    // Panels
    private VBox panelTaiKhoan;
    private VBox panelDoiMatKhau;
    private VBox panelDanhMuc;
    private VBox panelDanhMucThu;
    private VBox panelBaoCao;

    // Tables
    private TableView<NguoiDung> tableTaiKhoan;
    private TableView<DanhMuc> tableDanhMuc;
    private TableView<DanhMuc> tableDanhMucThu;
    private TableView<UserReportRow> tableUserReport;
    private TextField txtTimStkTaiKhoan;

    // Report stat labels
    private Label lblTongNguoiDung;
    private Label lblTongGiaoDich;
    private Label lblTongTienLuuChuyen;
    private Label lblBiKhoa;

    // Nav buttons
    private Button btnNavTaiKhoan;
    private Button btnNavDoiMatKhau;
    private Button btnNavDanhMuc;
    private Button btnNavDanhMucThu;
    private Button btnNavBaoCao;

    // DTO for user report table rows
    public static class UserReportRow {
        private String tenDangNhap;
        private String hoTen;
        private String soTaiKhoan;
        private String soDu;
        private String soGDDaGui;
        private String soGDDaNhan;
        private String trangThai;

        public UserReportRow(String tenDangNhap, String hoTen, String soTaiKhoan,
                             String soDu, String soGDDaGui, String soGDDaNhan, String trangThai) {
            this.tenDangNhap = tenDangNhap;
            this.hoTen = hoTen;
            this.soTaiKhoan = soTaiKhoan;
            this.soDu = soDu;
            this.soGDDaGui = soGDDaGui;
            this.soGDDaNhan = soGDDaNhan;
            this.trangThai = trangThai;
        }

        public String getTenDangNhap() { return tenDangNhap; }
        public String getHoTen() { return hoTen; }
        public String getSoTaiKhoan() { return soTaiKhoan; }
        public String getSoDu() { return soDu; }
        public String getSoGDDaGui() { return soGDDaGui; }
        public String getSoGDDaNhan() { return soGDDaNhan; }
        public String getTrangThai() { return trangThai; }
    }

    public AdminDashboardController(Stage stage) {
        this.stage = stage;
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

        buildPanelTaiKhoan();
        buildPanelDoiMatKhau();
        buildPanelDanhMuc();
        buildPanelDanhMucThu();
        buildPanelBaoCao();

        showPanel(panelTaiKhoan);
        setActiveNav(btnNavTaiKhoan);

        scene = new Scene(root, 1200, 800);
    }

    // ==================== HEADER ====================

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

    // ==================== NAVIGATION BAR ====================

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

        btnNavTaiKhoan.setOnAction(e -> { showPanel(panelTaiKhoan); setActiveNav(btnNavTaiKhoan); });
        btnNavDoiMatKhau.setOnAction(e -> { showPanel(panelDoiMatKhau); setActiveNav(btnNavDoiMatKhau); });
        btnNavDanhMuc.setOnAction(e -> { showPanel(panelDanhMuc); setActiveNav(btnNavDanhMuc); refreshDanhMucPanel(); });
        btnNavDanhMucThu.setOnAction(e -> { showPanel(panelDanhMucThu); setActiveNav(btnNavDanhMucThu); refreshDanhMucThuPanel(); });
        btnNavBaoCao.setOnAction(e -> { showPanel(panelBaoCao); setActiveNav(btnNavBaoCao); refreshBaoCaoPanel(); });

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

    // ==================== PANEL: QUẢN LÝ TÀI KHOẢN ====================

    private void buildPanelTaiKhoan() {
        panelTaiKhoan = new VBox(15);
        panelTaiKhoan.setPadding(new Insets(5));

        Label title = new Label("Quản lý tài khoản người dùng");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Label lblTim = new Label("Tìm theo STK:");
        lblTim.setStyle("-fx-font-weight: bold;");

        txtTimStkTaiKhoan = new TextField();
        txtTimStkTaiKhoan.setPromptText("Nhập số tài khoản user...");
        txtTimStkTaiKhoan.setPrefWidth(260);
        MoneyInputUtil.attachDigitsOnly(txtTimStkTaiKhoan);

        Button btnXoaLoc = new Button("Xóa lọc");
        btnXoaLoc.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold;");
        btnXoaLoc.setOnAction(e -> {
            txtTimStkTaiKhoan.clear();
            loadDanhSachTaiKhoan();
        });

        txtTimStkTaiKhoan.textProperty().addListener((obs, oldV, newV) -> loadDanhSachTaiKhoan(newV));
        searchBox.getChildren().addAll(lblTim, txtTimStkTaiKhoan, btnXoaLoc);

        tableTaiKhoan = new TableView<>();
        VBox.setVgrow(tableTaiKhoan, Priority.ALWAYS);

        TableColumn<NguoiDung, String> colSTK = new TableColumn<>("Số TK");
        colSTK.setCellValueFactory(new PropertyValueFactory<>("soTaiKhoan"));
        colSTK.setPrefWidth(110);

        TableColumn<NguoiDung, String> colTenDN = new TableColumn<>("Tên đăng nhập");
        colTenDN.setCellValueFactory(new PropertyValueFactory<>("tenDangNhap"));
        colTenDN.setPrefWidth(150);

        TableColumn<NguoiDung, String> colHoTen = new TableColumn<>("Họ tên");
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colHoTen.setPrefWidth(180);

        TableColumn<NguoiDung, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(200);

        TableColumn<NguoiDung, String> colVaiTro = new TableColumn<>("Vai trò");
        colVaiTro.setCellValueFactory(new PropertyValueFactory<>("vaiTro"));
        colVaiTro.setPrefWidth(100);
        colVaiTro.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                if ("quan_ly".equals(item)) {
                    setText("Quản lý");
                } else if ("nguoi_dung".equals(item)) {
                    setText("Người dùng");
                } else {
                    setText(item);
                }
            }
        });

        TableColumn<NguoiDung, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(130);
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    setStyle("");
                } else {
                    NguoiDung nd = getTableView().getItems().get(getIndex());
                    if ("hoat_dong".equals(nd.getTrangThai())) {
                        setText("Hoạt động");
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setText("Bị khóa");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tableTaiKhoan.getColumns().addAll(colSTK, colTenDN, colHoTen, colEmail, colVaiTro, colTrangThai);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button btnRefresh = new Button("Làm mới");
        btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnRefresh.setOnAction(e -> loadDanhSachTaiKhoan());

        Button btnKhoa = new Button("Khóa tài khoản");
        btnKhoa.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        btnKhoa.setOnAction(e -> handleKhoaTaiKhoan());

        Button btnMoKhoa = new Button("Mở khóa");
        btnMoKhoa.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnMoKhoa.setOnAction(e -> handleMoKhoaTaiKhoan());

        Button btnNapTien = new Button("Nạp tiền cho user");
        btnNapTien.setStyle("-fx-background-color: #16a085; -fx-text-fill: white; -fx-font-weight: bold;");
        btnNapTien.setOnAction(e -> handleNapTienChoUser());

        Button btnXemThongTin = new Button("Xem thông tin user");
        btnXemThongTin.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold;");
        btnXemThongTin.setOnAction(e -> handleXemThongTinTaiKhoan());

        buttonBox.getChildren().addAll(btnRefresh, btnKhoa, btnMoKhoa, btnNapTien, btnXemThongTin);

        loadDanhSachTaiKhoan();
        panelTaiKhoan.getChildren().addAll(title, searchBox, tableTaiKhoan, buttonBox);
    }

    private void loadDanhSachTaiKhoan() {
        loadDanhSachTaiKhoan(txtTimStkTaiKhoan != null ? txtTimStkTaiKhoan.getText() : null);
    }

    private void loadDanhSachTaiKhoan(String keywordStk) {
        try {
            List<NguoiDung> list = nguoiDungDAO.layTatCa();
            String keyword = keywordStk != null ? keywordStk.trim() : "";
            if (!keyword.isEmpty()) {
                List<NguoiDung> filtered = new ArrayList<>();
                for (NguoiDung nd : list) {
                    String stk = nd.getSoTaiKhoan() != null ? nd.getSoTaiKhoan() : "";
                    if (stk.contains(keyword)) {
                        filtered.add(nd);
                    }
                }
                tableTaiKhoan.getItems().setAll(filtered);
            } else {
                tableTaiKhoan.getItems().setAll(list);
            }
        } catch (Exception e) {
            showAlert("Lỗi", "Không thể tải danh sách: " + e.getMessage());
        }
    }

    // Inner record to carry lock dialog result
    private static class KhoaInfo {
        String lyDo;
        Timestamp thoiGianMoKhoa;
        KhoaInfo(String lyDo, Timestamp thoiGianMoKhoa) {
            this.lyDo = lyDo;
            this.thoiGianMoKhoa = thoiGianMoKhoa;
        }
    }

    private void handleKhoaTaiKhoan() {
        NguoiDung selected = tableTaiKhoan.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Lỗi", "Vui lòng chọn tài khoản cần khóa!"); return; }
        if ("quan_ly".equals(selected.getVaiTro())) { showAlert("Lỗi", "Không thể khóa tài khoản Admin!"); return; }
        if ("bi_khoa".equals(selected.getTrangThai())) { showAlert("Thông báo", "Tài khoản này đã bị khóa!"); return; }

        // Dialog nhập lý do + thời gian khóa
        Dialog<KhoaInfo> dialog = new Dialog<>();
        dialog.setTitle("Khóa tài khoản");
        dialog.setHeaderText("Khóa tài khoản: " + selected.getTenDangNhap());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextArea txtLyDo = new TextArea();
        txtLyDo.setPromptText("Nhập lý do khóa để thông báo tới người dùng...");
        txtLyDo.setPrefRowCount(3);
        txtLyDo.setPrefWidth(380);
        txtLyDo.setWrapText(true);

        // Thời gian khóa - không bắt buộc
        ComboBox<String> cbThoiGian = new ComboBox<>();
        cbThoiGian.getItems().addAll(
            "Vĩnh viễn (không tự mở)",
            "1 giờ",
            "6 giờ",
            "1 ngày",
            "3 ngày",
            "7 ngày",
            "30 ngày"
        );
        cbThoiGian.setValue("Vĩnh viễn (không tự mở)");
        cbThoiGian.setPrefWidth(250);

        grid.add(new Label("Lý do khóa:"), 0, 0);
        grid.add(txtLyDo, 0, 1);
        grid.add(new Label("Thời gian khóa (tùy chọn):"), 0, 2);
        grid.add(cbThoiGian, 0, 3);

        dialog.getDialogPane().setContent(grid);
        ButtonType btnKhoaOK = new ButtonType("Xác nhận khóa", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnKhoaOK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == btnKhoaOK) {
                String lyDo = txtLyDo.getText().trim();
                if (lyDo.isEmpty()) lyDo = "(Không có lý do cụ thể)";

                // Tính thời gian mở khóa
                Timestamp thoiGianMoKhoa = null;
                String sel = cbThoiGian.getValue();
                LocalDateTime now = LocalDateTime.now();
                switch (sel) {
                    case "1 giờ"   -> thoiGianMoKhoa = Timestamp.valueOf(now.plusHours(1));
                    case "6 giờ"   -> thoiGianMoKhoa = Timestamp.valueOf(now.plusHours(6));
                    case "1 ngày"   -> thoiGianMoKhoa = Timestamp.valueOf(now.plusDays(1));
                    case "3 ngày"   -> thoiGianMoKhoa = Timestamp.valueOf(now.plusDays(3));
                    case "7 ngày"   -> thoiGianMoKhoa = Timestamp.valueOf(now.plusDays(7));
                    case "30 ngày"  -> thoiGianMoKhoa = Timestamp.valueOf(now.plusDays(30));
                    default          -> thoiGianMoKhoa = null; // vĩnh viễn
                }
                return new KhoaInfo(lyDo, thoiGianMoKhoa);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(info -> {
            try {
                nguoiDungDAO.capNhatTrangThai(selected.getMaNguoiDung(), "bi_khoa", info.lyDo, info.thoiGianMoKhoa);
                String msg = "Đã khóa tài khoản: " + selected.getTenDangNhap() + "\nLý do: " + info.lyDo;
                if (info.thoiGianMoKhoa != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                    msg += "\nTự mở khóa lúc: " + sdf.format(info.thoiGianMoKhoa);
                } else {
                    msg += "\nKhóa vĩnh viễn.";
                }
                showAlert("Thành công", msg);
                loadDanhSachTaiKhoan();
            } catch (Exception ex) {
                showAlert("Lỗi", ex.getMessage());
            }
        });
    }

    private void handleMoKhoaTaiKhoan() {
        NguoiDung selected = tableTaiKhoan.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Lỗi", "Vui lòng chọn tài khoản cần mở khóa!"); return; }
        if ("hoat_dong".equals(selected.getTrangThai())) { showAlert("Thông báo", "Tài khoản này đang hoạt động bình thường!"); return; }

        try {
            nguoiDungDAO.capNhatTrangThai(selected.getMaNguoiDung(), "hoat_dong");
            showAlert("Thành công", "Đã mở khóa tài khoản: " + selected.getTenDangNhap());
            loadDanhSachTaiKhoan();
        } catch (Exception ex) {
            showAlert("Lỗi", ex.getMessage());
        }
    }

    private void handleNapTienChoUser() {
        Dialog<BigDecimal> dialog = new Dialog<>();
        dialog.setTitle("Nạp tiền cho user");
        dialog.setHeaderText("Nhập STK để tự động hiển thị người nhận");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtSoTaiKhoan = new TextField();
        txtSoTaiKhoan.setPromptText("Nhập STK user cần nạp");
        txtSoTaiKhoan.setPrefWidth(220);
        MoneyInputUtil.attachDigitsOnly(txtSoTaiKhoan);

        Label lblXacNhan = new Label("Nhập STK để tra cứu người dùng...");
        lblXacNhan.setStyle("-fx-text-fill: #7f8c8d;");

        Label lblSoDu = new Label("Số dư hiện tại: -");
        lblSoDu.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        final NguoiDung[] userDaXacNhan = new NguoiDung[1];

        TextField txtSoTien = new TextField();
        txtSoTien.setPromptText("Nhập số tiền cần nạp (VD: 500000)");
        txtSoTien.setPrefWidth(320);
        MoneyInputUtil.attachMoneyFormatter(txtSoTien);

        grid.add(new Label("Số tài khoản:"), 0, 0);
        grid.add(txtSoTaiKhoan, 1, 0);
        grid.add(lblXacNhan, 1, 1);
        grid.add(lblSoDu, 1, 2);
        grid.add(new Label("Số tiền nạp:"), 0, 3);
        grid.add(txtSoTien, 1, 3);

        dialog.getDialogPane().setContent(grid);
        ButtonType btnNapType = new ButtonType("Nạp tiền", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnNapType, ButtonType.CANCEL);

        Button btnNap = (Button) dialog.getDialogPane().lookupButton(btnNapType);
        btnNap.setDisable(true);

        txtSoTaiKhoan.textProperty().addListener((obs, oldVal, newVal) -> {
            String stk = newVal != null ? newVal.trim() : "";
            if (stk.isEmpty()) {
                userDaXacNhan[0] = null;
                lblXacNhan.setText("Nhập STK để tra cứu người dùng...");
                lblXacNhan.setStyle("-fx-text-fill: #7f8c8d;");
                lblSoDu.setText("Số dư hiện tại: -");
                btnNap.setDisable(true);
                return;
            }

            try {
                NguoiDung nd = nguoiDungDAO.layNguoiDungThuongTheoSoTaiKhoan(stk);
                if (nd == null) {
                    userDaXacNhan[0] = null;
                    lblXacNhan.setText("Không tìm thấy user với STK này.");
                    lblXacNhan.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    lblSoDu.setText("Số dư hiện tại: -");
                    btnNap.setDisable(true);
                    return;
                }

                userDaXacNhan[0] = nd;
                lblXacNhan.setText(nd.getTenDangNhap() + " - " + nd.getHoTen());
                lblXacNhan.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                lblSoDu.setText("Số dư hiện tại: " + df.format(nd.getSoDu()) + " đ");
                btnNap.setDisable(false);
            } catch (Exception ex) {
                userDaXacNhan[0] = null;
                lblXacNhan.setText("Lỗi tra cứu STK.");
                lblXacNhan.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                lblSoDu.setText("Số dư hiện tại: -");
                btnNap.setDisable(true);
                showAlert("Lỗi", "Không thể tra cứu STK: " + ex.getMessage());
            }
        });

        NguoiDung selected = tableTaiKhoan.getSelectionModel().getSelectedItem();
        if (selected != null && "nguoi_dung".equals(selected.getVaiTro()) && selected.getSoTaiKhoan() != null) {
            txtSoTaiKhoan.setText(selected.getSoTaiKhoan());
        }

        dialog.setResultConverter(btn -> {
            if (btn == btnNapType) {
                try {
                    if (userDaXacNhan[0] == null) {
                        showAlert("Lỗi", "Vui lòng nhập STK hợp lệ trước khi nạp tiền!");
                        return null;
                    }
                    String raw = txtSoTien.getText() != null ? txtSoTien.getText().trim() : "";
                    if (raw.isEmpty()) {
                        showAlert("Lỗi", "Vui lòng nhập số tiền cần nạp!");
                        return null;
                    }

                    BigDecimal soTienNap = MoneyInputUtil.parseMoney(raw);
                    if (soTienNap == null) throw new NumberFormatException();
                    if (soTienNap.compareTo(BigDecimal.ZERO) <= 0) {
                        showAlert("Lỗi", "Số tiền nạp phải lớn hơn 0!");
                        return null;
                    }

                    if (soTienNap.compareTo(new BigDecimal("999999999999")) > 0) {
                        showAlert("Lỗi", "Số tiền nạp quá lớn!");
                        return null;
                    }
                    return soTienNap;
                } catch (NumberFormatException ex) {
                    showAlert("Lỗi", "Số tiền không hợp lệ!");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(soTienNap -> {
            try {
                NguoiDung target = userDaXacNhan[0];
                if (target == null) {
                    showAlert("Lỗi", "Không xác định được user nhận tiền!");
                    return;
                }
                String soTaiKhoanAdmin = LoginController.currentUser != null ? LoginController.currentUser.getSoTaiKhoan() : null;
                if (soTaiKhoanAdmin == null || soTaiKhoanAdmin.isBlank()) {
                    showAlert("Lỗi", "Không xác định được tài khoản Admin để ghi lịch sử giao dịch!");
                    return;
                }

                boolean ok = nguoiDungDAO.napTienVaoTaiKhoanVaTaoGiaoDich(
                    soTaiKhoanAdmin,
                    target.getMaNguoiDung(),
                    soTienNap,
                    "Ngân hàng chuyển tiền"
                );
                if (ok) {
                    showAlert("Thành công", "Đã nạp " + df.format(soTienNap) + " đ cho user " + target.getTenDangNhap() +
                        "!\nLịch sử giao dịch đã ghi nội dung: Ngân hàng chuyển tiền.");
                    loadDanhSachTaiKhoan();
                } else {
                    showAlert("Lỗi", "Nạp tiền thất bại. Vui lòng thử lại!");
                }
            } catch (Exception ex) {
                showAlert("Lỗi", "Không thể nạp tiền: " + ex.getMessage());
            }
        });
    }

    private void handleXemThongTinTaiKhoan() {
        NguoiDung selected = tableTaiKhoan.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Lỗi", "Vui lòng chọn user cần xem thông tin!");
            return;
        }

        try {
            NguoiDung nd = nguoiDungDAO.layTheoId(selected.getMaNguoiDung());
            if (nd == null) {
                showAlert("Lỗi", "Không tìm thấy thông tin người dùng trong cơ sở dữ liệu!");
                return;
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Thông tin người dùng");
            dialog.setHeaderText("Chi tiết tài khoản STK: " + nd.getSoTaiKhoan());
            dialog.getDialogPane().setPrefWidth(680);

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

            VBox content = new VBox(14);
            content.setPadding(new Insets(16));
            content.setStyle("-fx-background-color: white;");

            Label secCoBan = createInfoSectionTitle("Thông tin cơ bản");
            GridPane gridCoBan = new GridPane();
            gridCoBan.setHgap(14);
            gridCoBan.setVgap(10);
            int rCoBan = 0;
            addInfoRow(gridCoBan, rCoBan++, "Số tài khoản", safeText(nd.getSoTaiKhoan()));
            addInfoRow(gridCoBan, rCoBan++, "Tên đăng nhập", safeText(nd.getTenDangNhap()));
            addInfoRow(gridCoBan, rCoBan++, "Họ tên", safeText(nd.getHoTen()));
            addInfoRow(gridCoBan, rCoBan++, "Email", safeText(nd.getEmail()));
            addInfoRow(gridCoBan, rCoBan++, "Vai trò", formatVaiTro(nd.getVaiTro()));
            addInfoRow(gridCoBan, rCoBan++, "Trạng thái", formatTrangThai(nd));
            if (!"quan_ly".equals(nd.getVaiTro())) {
                addInfoRow(gridCoBan, rCoBan, "Số dư tài khoản", df.format(nd.getSoDu() != null ? nd.getSoDu() : BigDecimal.ZERO) + " đ");
            }

            content.getChildren().addAll(secCoBan, gridCoBan);

            if ("bi_khoa".equals(nd.getTrangThai())) {
                Label secKhoa = createInfoSectionTitle("Thông tin khóa");
                GridPane gridKhoa = new GridPane();
                gridKhoa.setHgap(14);
                gridKhoa.setVgap(10);
                addInfoRow(gridKhoa, 0, "Lý do khóa", safeText(nd.getLyDoKhoa()));
                addInfoRow(gridKhoa, 1, "Hết khóa lúc", nd.getThoiGianMoKhoa() != null ? sdf.format(nd.getThoiGianMoKhoa()) : "Khóa vĩnh viễn");
                content.getChildren().addAll(secKhoa, gridKhoa);
            }

            Label secMoc = createInfoSectionTitle("Mốc thời gian");
            GridPane gridMoc = new GridPane();
            gridMoc.setHgap(14);
            gridMoc.setVgap(10);
            addInfoRow(gridMoc, 0, "Lần đăng nhập cuối", nd.getLanDangNhapCuoi() != null ? sdf.format(nd.getLanDangNhapCuoi()) : "-");
            addInfoRow(gridMoc, 1, "Ngày tạo", nd.getNgayTao() != null ? sdf.format(nd.getNgayTao()) : "-");
            content.getChildren().addAll(secMoc, gridMoc);

            ScrollPane scroll = new ScrollPane(content);
            scroll.setFitToWidth(true);
            scroll.setPrefViewportHeight(430);

            dialog.getDialogPane().setContent(scroll);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        } catch (Exception ex) {
            showAlert("Lỗi", "Không thể tải thông tin user: " + ex.getMessage());
        }
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private Label createInfoSectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        return label;
    }

    private void addInfoRow(GridPane grid, int row, String key, String value) {
        Label lblKey = new Label(key + ":");
        lblKey.setStyle("-fx-text-fill: #5d6d7e; -fx-font-weight: bold;");

        Label lblVal = new Label(value);
        lblVal.setStyle("-fx-text-fill: #2c3e50;");
        lblVal.setWrapText(true);
        lblVal.setMaxWidth(460);

        grid.add(lblKey, 0, row);
        grid.add(lblVal, 1, row);
    }

    private String formatVaiTro(String vaiTro) {
        if ("quan_ly".equals(vaiTro)) return "Quản lý";
        if ("nguoi_dung".equals(vaiTro)) return "Người dùng";
        return safeText(vaiTro);
    }

    private String formatTrangThai(NguoiDung nd) {
        if (nd == null) return "-";
        if ("hoat_dong".equals(nd.getTrangThai())) return "Hoạt động";
        if ("bi_khoa".equals(nd.getTrangThai())) return "Bị khóa";
        return safeText(nd.getTrangThai());
    }

    // ==================== PANEL: ĐỔI MẬT KHẨU ====================

    private void buildPanelDoiMatKhau() {
        VBox outer = new VBox(20);
        outer.setAlignment(Pos.TOP_CENTER);
        outer.setPadding(new Insets(30));

        Label title = new Label("Đổi mật khẩu Admin");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));
        grid.setMaxWidth(580);
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        PasswordField txtCu = new PasswordField();
        txtCu.setPromptText("Nhập mật khẩu hiện tại");
        txtCu.setPrefWidth(300);

        PasswordField txtMoi = new PasswordField();
        txtMoi.setPromptText("Tối thiểu 6 ký tự");
        txtMoi.setPrefWidth(300);

        PasswordField txtXacNhan = new PasswordField();
        txtXacNhan.setPromptText("Nhập lại mật khẩu mới");
        txtXacNhan.setPrefWidth(300);

        Label lblKetQua = new Label("");
        lblKetQua.setWrapText(true);

        Button btnDoi = new Button("Đổi mật khẩu");
        btnDoi.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnDoi.setPrefWidth(200);

        btnDoi.setOnAction(e -> {
            String cu = txtCu.getText();
            String moi = txtMoi.getText();
            String xacNhan = txtXacNhan.getText();

            if (cu.isEmpty() || moi.isEmpty() || xacNhan.isEmpty()) {
                lblKetQua.setText("Vui lòng điền đầy đủ thông tin!");
                lblKetQua.setStyle("-fx-text-fill: red;");
                return;
            }
            if (moi.length() < 6) {
                lblKetQua.setText("Mật khẩu mới phải có ít nhất 6 ký tự!");
                lblKetQua.setStyle("-fx-text-fill: red;");
                return;
            }
            if (!moi.equals(xacNhan)) {
                lblKetQua.setText("Mật khẩu xác nhận không khớp!");
                lblKetQua.setStyle("-fx-text-fill: red;");
                return;
            }

            try {
                boolean ok = nguoiDungDAO.doiMatKhau(LoginController.currentUser.getMaNguoiDung(), cu, moi);
                if (ok) {
                    lblKetQua.setText("Đổi mật khẩu thành công!");
                    lblKetQua.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    txtCu.clear();
                    txtMoi.clear();
                    txtXacNhan.clear();
                } else {
                    lblKetQua.setText("Mật khẩu hiện tại không đúng!");
                    lblKetQua.setStyle("-fx-text-fill: red;");
                }
            } catch (Exception ex) {
                lblKetQua.setText("Lỗi: " + ex.getMessage());
                lblKetQua.setStyle("-fx-text-fill: red;");
            }
        });

        grid.add(new Label("Mật khẩu hiện tại:"), 0, 0);
        grid.add(txtCu, 1, 0);
        grid.add(new Label("Mật khẩu mới:"), 0, 1);
        grid.add(txtMoi, 1, 1);
        grid.add(new Label("Xác nhận mật khẩu:"), 0, 2);
        grid.add(txtXacNhan, 1, 2);
        grid.add(btnDoi, 1, 3);
        grid.add(lblKetQua, 1, 4);

        Separator separator = new Separator();
        separator.setMaxWidth(620);

        Label titleReset = new Label("Đặt lại mật khẩu cho User (quên mật khẩu)");
        titleReset.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        GridPane gridReset = new GridPane();
        gridReset.setHgap(15);
        gridReset.setVgap(15);
        gridReset.setPadding(new Insets(25));
        gridReset.setMaxWidth(620);
        gridReset.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        TextField txtStkUser = new TextField();
        txtStkUser.setPromptText("Nhập STK user cần đặt lại mật khẩu");
        txtStkUser.setPrefWidth(320);
        MoneyInputUtil.attachDigitsOnly(txtStkUser);

        Label lblUserInfo = new Label("Nhập STK để kiểm tra user...");
        lblUserInfo.setStyle("-fx-text-fill: #7f8c8d;");

        PasswordField txtMatKhauMoiUser = new PasswordField();
        txtMatKhauMoiUser.setPromptText("Mật khẩu mới cho user (tối thiểu 6 ký tự)");
        txtMatKhauMoiUser.setPrefWidth(320);

        PasswordField txtXacNhanUser = new PasswordField();
        txtXacNhanUser.setPromptText("Nhập lại mật khẩu mới");
        txtXacNhanUser.setPrefWidth(320);

        Label lblKetQuaReset = new Label("");
        lblKetQuaReset.setWrapText(true);

        final NguoiDung[] selectedUser = new NguoiDung[1];
        txtStkUser.textProperty().addListener((obs, oldVal, newVal) -> {
            String stk = newVal != null ? newVal.trim() : "";
            if (stk.isEmpty()) {
                selectedUser[0] = null;
                lblUserInfo.setText("Nhập STK để kiểm tra user...");
                lblUserInfo.setStyle("-fx-text-fill: #7f8c8d;");
                return;
            }
            try {
                NguoiDung nd = nguoiDungDAO.layNguoiDungThuongTheoSoTaiKhoan(stk);
                if (nd == null) {
                    selectedUser[0] = null;
                    lblUserInfo.setText("Không tìm thấy user với STK này.");
                    lblUserInfo.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                } else {
                    selectedUser[0] = nd;
                    lblUserInfo.setText(nd.getTenDangNhap() + " - " + nd.getHoTen());
                    lblUserInfo.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            } catch (Exception ex) {
                selectedUser[0] = null;
                lblUserInfo.setText("Lỗi kiểm tra STK.");
                lblUserInfo.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
        });

        Button btnReset = new Button("Đặt lại mật khẩu user");
        btnReset.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnReset.setPrefWidth(250);

        btnReset.setOnAction(e -> {
            NguoiDung nd = selectedUser[0];
            String mkMoi = txtMatKhauMoiUser.getText();
            String mkXn = txtXacNhanUser.getText();

            if (nd == null) {
                lblKetQuaReset.setText("Vui lòng nhập STK user hợp lệ!");
                lblKetQuaReset.setStyle("-fx-text-fill: red;");
                return;
            }
            if (mkMoi == null || mkMoi.isBlank() || mkXn == null || mkXn.isBlank()) {
                lblKetQuaReset.setText("Vui lòng nhập đầy đủ mật khẩu mới và xác nhận!");
                lblKetQuaReset.setStyle("-fx-text-fill: red;");
                return;
            }
            if (mkMoi.length() < 6) {
                lblKetQuaReset.setText("Mật khẩu mới phải có ít nhất 6 ký tự!");
                lblKetQuaReset.setStyle("-fx-text-fill: red;");
                return;
            }
            if (!mkMoi.equals(mkXn)) {
                lblKetQuaReset.setText("Mật khẩu xác nhận không khớp!");
                lblKetQuaReset.setStyle("-fx-text-fill: red;");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận đặt lại mật khẩu");
            confirm.setHeaderText("Đặt lại mật khẩu cho user: " + nd.getTenDangNhap());
            confirm.setContentText("STK: " + nd.getSoTaiKhoan() + "\nHọ tên: " + nd.getHoTen() + "\n\nBạn có chắc chắn muốn tiếp tục?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            try {
                boolean ok = nguoiDungDAO.datLaiMatKhauChoUser(nd.getMaNguoiDung(), mkMoi);
                if (ok) {
                    lblKetQuaReset.setText("Đặt lại mật khẩu thành công cho user " + nd.getTenDangNhap() + "!");
                    lblKetQuaReset.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    txtMatKhauMoiUser.clear();
                    txtXacNhanUser.clear();
                } else {
                    lblKetQuaReset.setText("Đặt lại mật khẩu thất bại!");
                    lblKetQuaReset.setStyle("-fx-text-fill: red;");
                }
            } catch (Exception ex) {
                lblKetQuaReset.setText("Lỗi: " + ex.getMessage());
                lblKetQuaReset.setStyle("-fx-text-fill: red;");
            }
        });

        gridReset.add(new Label("STK User:"), 0, 0);
        gridReset.add(txtStkUser, 1, 0);
        gridReset.add(lblUserInfo, 1, 1);
        gridReset.add(new Label("Mật khẩu mới:"), 0, 2);
        gridReset.add(txtMatKhauMoiUser, 1, 2);
        gridReset.add(new Label("Xác nhận mật khẩu:"), 0, 3);
        gridReset.add(txtXacNhanUser, 1, 3);
        gridReset.add(btnReset, 1, 4);
        gridReset.add(lblKetQuaReset, 1, 5);

        outer.getChildren().addAll(title, grid, separator, titleReset, gridReset);
        panelDoiMatKhau = outer;
    }

    // ==================== PANEL: DANH MỤC MẶC ĐỊNH ====================

    private void buildPanelDanhMuc() {
        panelDanhMuc = new VBox(15);
        panelDanhMuc.setPadding(new Insets(5));

        Label title = new Label("Quản lý danh mục chi tiêu mặc định");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Label note = new Label("Danh mục Chi mặc định hiển thị cho TẤT CẢ người dùng khi gửi tiền. Chỉ Admin mới có thể thêm/sửa/xóa.");
        note.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        tableDanhMuc = new TableView<>();
        VBox.setVgrow(tableDanhMuc, Priority.ALWAYS);

        TableColumn<DanhMuc, String> colTen = new TableColumn<>("Tên danh mục");
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenDanhMuc"));
        colTen.setPrefWidth(220);
        colTen.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    setStyle("");
                    return;
                }
                DanhMuc dm = getTableView().getItems().get(getIndex());
                if (dm.isDanhMucCon()) {
                    setText("   ↳ " + (item != null ? item : "") + " (con)");
                    setStyle("-fx-text-fill: #2c3e50;");
                } else {
                    setText("▣ " + (item != null ? item : "") + " (cha)");
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #1f6fb2;");
                }
            }
        });

        TableColumn<DanhMuc, String> colMoTa = new TableColumn<>("Mô tả");
        colMoTa.setCellValueFactory(new PropertyValueFactory<>("moTa"));
        colMoTa.setPrefWidth(360);

        TableColumn<DanhMuc, String> colCha = new TableColumn<>("Danh mục cha");
        colCha.setCellValueFactory(new PropertyValueFactory<>("tenDanhMucCha"));
        colCha.setPrefWidth(200);
        colCha.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    setStyle("");
                    return;
                }
                DanhMuc dm = getTableView().getItems().get(getIndex());
                if (dm.isDanhMucCon()) {
                    setText(dm.getTenDanhMucCha() != null ? dm.getTenDanhMucCha() : "");
                    setStyle("-fx-text-fill: #5b6b7a;");
                } else {
                    setText("Danh mục cha");
                    setStyle("-fx-text-fill: #1f6fb2; -fx-font-weight: bold;");
                }
            }
        });

        tableDanhMuc.getColumns().addAll(colTen, colMoTa, colCha);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button btnThem = new Button("Thêm");
        btnThem.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnThem.setOnAction(e -> handleThemDanhMuc());

        Button btnSua = new Button("Sửa");
        btnSua.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSua.setOnAction(e -> handleSuaDanhMuc());

        Button btnXoa = new Button("Xóa");
        btnXoa.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnXoa.setOnAction(e -> handleXoaDanhMuc());

        buttonBox.getChildren().addAll(btnThem, btnSua, btnXoa);

        panelDanhMuc.getChildren().addAll(title, note, tableDanhMuc, buttonBox);
        refreshDanhMucPanel();
    }

    private void refreshDanhMucPanel() {
        if (tableDanhMuc != null) {
            tableDanhMuc.getItems().setAll(danhMucDAO.layDanhMucTheoLoai(null, "chi"));
        }
    }

    private void handleThemDanhMuc() {
        Dialog<DanhMuc> dialog = new Dialog<>();
        dialog.setTitle("Thêm danh mục mặc định");
        dialog.setHeaderText("Tạo danh mục hiển thị cho tất cả người dùng");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtTen = new TextField();
        txtTen.setPromptText("Tên danh mục");
        txtTen.setPrefWidth(300);

        TextArea txtMoTa = new TextArea();
        txtMoTa.setPromptText("Mô tả chi tiết...");
        txtMoTa.setPrefRowCount(3);
        txtMoTa.setPrefWidth(300);

        ComboBox<DanhMuc> cbCha = new ComboBox<>();
        cbCha.setPrefWidth(300);
        cbCha.setPromptText("(Không có - danh mục gốc)");
        cbCha.getItems().setAll(layDanhMucChaMacDinh("chi", null));

        grid.add(new Label("Tên danh mục:"), 0, 0);
        grid.add(txtTen, 1, 0);
        grid.add(new Label("Mô tả:"), 0, 1);
        grid.add(txtMoTa, 1, 1);
        grid.add(new Label("Danh mục cha:"), 0, 2);
        grid.add(cbCha, 1, 2);

        dialog.getDialogPane().setContent(grid);
        ButtonType btnOK = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == btnOK) {
                String ten = txtTen.getText().trim();
                if (ten.isEmpty()) { showAlert("Lỗi", "Vui lòng nhập tên danh mục!"); return null; }
                Integer parentId = cbCha.getValue() != null ? cbCha.getValue().getId() : null;
                return new DanhMuc(ten, txtMoTa.getText().trim(), "chi", null, parentId);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dm -> {
            boolean biTrung;
            if (dm.getParentId() != null) {
                biTrung = danhMucDAO.tonTaiTenDanhMucCon(dm.getTenDanhMuc(), dm.getLoai(), null, null);
            } else {
                biTrung = danhMucDAO.tonTaiTenDanhMuc(dm.getTenDanhMuc(), dm.getLoai(), null);
            }

            if (biTrung) {
                showAlert("Lỗi", "Tên danh mục Chi mặc định đã tồn tại!\nVui lòng nhập tên khác.");
                return;
            }

            if (danhMucDAO.themDanhMuc(dm)) {
                showAlert("Thành công", "Đã thêm danh mục: " + dm.getTenDanhMuc());
                refreshDanhMucPanel();
            } else {
                showAlert("Lỗi", "Thêm danh mục thất bại!");
            }
        });
    }

    private void handleSuaDanhMuc() {
        DanhMuc selected = tableDanhMuc.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Lỗi", "Vui lòng chọn danh mục cần sửa!"); return; }
        final String tenCu = selected.getTenDanhMuc() != null ? selected.getTenDanhMuc().trim() : "";
        final Integer parentCu = selected.getParentId();

        Dialog<DanhMuc> dialog = new Dialog<>();
        dialog.setTitle("Sửa danh mục");
        dialog.setHeaderText("Chỉnh sửa: " + selected.getTenDanhMuc());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtTen = new TextField(selected.getTenDanhMuc());
        txtTen.setPrefWidth(300);

        TextArea txtMoTa = new TextArea(selected.getMoTa() != null ? selected.getMoTa() : "");
        txtMoTa.setPrefRowCount(3);
        txtMoTa.setPrefWidth(300);

        ComboBox<DanhMuc> cbCha = new ComboBox<>();
        cbCha.setPrefWidth(300);
        cbCha.setPromptText("(Không có - danh mục gốc)");
        List<DanhMuc> dsCha = layDanhMucChaMacDinh("chi", selected.getId());
        cbCha.getItems().setAll(dsCha);
        if (selected.getParentId() != null) {
            for (DanhMuc dm : dsCha) {
                if (dm.getId() == selected.getParentId()) {
                    cbCha.setValue(dm);
                    break;
                }
            }
        }

        grid.add(new Label("Tên danh mục:"), 0, 0);
        grid.add(txtTen, 1, 0);
        grid.add(new Label("Mô tả:"), 0, 1);
        grid.add(txtMoTa, 1, 1);
        grid.add(new Label("Danh mục cha:"), 0, 2);
        grid.add(cbCha, 1, 2);

        dialog.getDialogPane().setContent(grid);
        ButtonType btnOK = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == btnOK) {
                String ten = txtTen.getText().trim();
                if (ten.isEmpty()) { showAlert("Lỗi", "Vui lòng nhập tên danh mục!"); return null; }
                return new DanhMuc(
                    selected.getId(),
                    ten,
                    txtMoTa.getText().trim(),
                    selected.getLoai(),
                    selected.getSoTaiKhoan(),
                    cbCha.getValue() != null ? cbCha.getValue().getId() : null,
                    null
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dm -> {
            String tenMoi = dm.getTenDanhMuc() != null ? dm.getTenDanhMuc().trim() : "";
            boolean daDoiTen = !tenMoi.equalsIgnoreCase(tenCu);
            boolean daDoiCha = (parentCu == null && dm.getParentId() != null)
                    || (parentCu != null && !parentCu.equals(dm.getParentId()));

            if (dm.getParentId() != null) {
                if ((daDoiTen || daDoiCha)
                        && danhMucDAO.tonTaiTenDanhMucCon(tenMoi, dm.getLoai(), null, dm.getId())) {
                    showAlert("Lỗi", "Tên danh mục con Chi mặc định đã tồn tại (kể cả khác danh mục cha)!\nVui lòng nhập tên khác.");
                    return;
                }
            } else {
                if (daDoiTen && danhMucDAO.tonTaiTenDanhMuc(tenMoi, dm.getLoai(), null)) {
                    showAlert("Lỗi", "Tên danh mục Chi mặc định đã tồn tại!\nVui lòng nhập tên khác.");
                    return;
                }
            }

            if (danhMucDAO.suaDanhMuc(dm)) {
                showAlert("Thành công", "Đã cập nhật danh mục!");
                refreshDanhMucPanel();
            } else {
                showAlert("Lỗi", "Cập nhật thất bại!");
            }
        });
    }

    private void handleXoaDanhMuc() {
        DanhMuc selected = tableDanhMuc.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Lỗi", "Vui lòng chọn danh mục cần xóa!"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa danh mục: " + selected.getTenDanhMuc());
        confirm.setContentText("Các giao dịch đã dùng danh mục này sẽ mất liên kết!\nBạn có chắc chắn muốn xóa?");

        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                if (danhMucDAO.xoaDanhMuc(selected.getId())) {
                    showAlert("Thành công", "Đã xóa danh mục!");
                    refreshDanhMucPanel();
                } else {
                    showAlert("Lỗi", "Xóa thất bại!");
                }
            }
        });
    }

    // ==================== PANEL: DANH MỤC THU MẶC ĐỊNH ====================

    private void buildPanelDanhMucThu() {
        panelDanhMucThu = new VBox(15);
        panelDanhMucThu.setPadding(new Insets(5));

        Label title = new Label("Quản lý danh mục thu nhập mặc định");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Label note = new Label("Danh mục Thu mặc định hiển thị cho TẤT CẢ người dùng khi nhận tiền. Chỉ Admin mới có thể thêm/sửa/xóa.");
        note.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        tableDanhMucThu = new TableView<>();
        VBox.setVgrow(tableDanhMucThu, Priority.ALWAYS);

        TableColumn<DanhMuc, String> colTen = new TableColumn<>("Tên danh mục");
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenDanhMuc"));
        colTen.setPrefWidth(220);
        colTen.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    setStyle("");
                    return;
                }
                DanhMuc dm = getTableView().getItems().get(getIndex());
                if (dm.isDanhMucCon()) {
                    setText("   ↳ " + (item != null ? item : "") + " (con)");
                    setStyle("-fx-text-fill: #2c3e50;");
                } else {
                    setText("▣ " + (item != null ? item : "") + " (cha)");
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #1f6fb2;");
                }
            }
        });

        TableColumn<DanhMuc, String> colMoTa = new TableColumn<>("Mô tả");
        colMoTa.setCellValueFactory(new PropertyValueFactory<>("moTa"));
        colMoTa.setPrefWidth(360);

        TableColumn<DanhMuc, String> colCha = new TableColumn<>("Danh mục cha");
        colCha.setCellValueFactory(new PropertyValueFactory<>("tenDanhMucCha"));
        colCha.setPrefWidth(200);
        colCha.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    setStyle("");
                    return;
                }
                DanhMuc dm = getTableView().getItems().get(getIndex());
                if (dm.isDanhMucCon()) {
                    setText(dm.getTenDanhMucCha() != null ? dm.getTenDanhMucCha() : "");
                    setStyle("-fx-text-fill: #5b6b7a;");
                } else {
                    setText("Danh mục cha");
                    setStyle("-fx-text-fill: #1f6fb2; -fx-font-weight: bold;");
                }
            }
        });

        tableDanhMucThu.getColumns().addAll(colTen, colMoTa, colCha);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button btnThem = new Button("Thêm");
        btnThem.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnThem.setOnAction(e -> handleThemDanhMucThu());

        Button btnSua = new Button("Sửa");
        btnSua.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSua.setOnAction(e -> handleSuaDanhMucThu());

        Button btnXoa = new Button("Xóa");
        btnXoa.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnXoa.setOnAction(e -> handleXoaDanhMucThu());

        buttonBox.getChildren().addAll(btnThem, btnSua, btnXoa);
        panelDanhMucThu.getChildren().addAll(title, note, tableDanhMucThu, buttonBox);
        refreshDanhMucThuPanel();
    }

    private void refreshDanhMucThuPanel() {
        if (tableDanhMucThu != null) {
            tableDanhMucThu.getItems().setAll(danhMucDAO.layDanhMucTheoLoai(null, "thu"));
        }
    }

    private void handleThemDanhMucThu() {
        Dialog<DanhMuc> dialog = new Dialog<>();
        dialog.setTitle("Thêm danh mục Thu mặc định");
        dialog.setHeaderText("Tạo danh mục Thu nhập hiển thị cho tất cả người dùng");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField txtTen = new TextField();
        txtTen.setPromptText("Tên danh mục"); txtTen.setPrefWidth(300);
        TextArea txtMoTa = new TextArea();
        txtMoTa.setPromptText("Mô tả chi tiết..."); txtMoTa.setPrefRowCount(3); txtMoTa.setPrefWidth(300);

        ComboBox<DanhMuc> cbCha = new ComboBox<>();
        cbCha.setPrefWidth(300);
        cbCha.setPromptText("(Không có - danh mục gốc)");
        cbCha.getItems().setAll(layDanhMucChaMacDinh("thu", null));

        grid.add(new Label("Tên danh mục:"), 0, 0); grid.add(txtTen, 1, 0);
        grid.add(new Label("Mô tả:"), 0, 1); grid.add(txtMoTa, 1, 1);
        grid.add(new Label("Danh mục cha:"), 0, 2); grid.add(cbCha, 1, 2);
        dialog.getDialogPane().setContent(grid);

        ButtonType btnOK = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == btnOK) {
                String ten = txtTen.getText().trim();
                if (ten.isEmpty()) { showAlert("Lỗi", "Vui lòng nhập tên danh mục!"); return null; }
                Integer parentId = cbCha.getValue() != null ? cbCha.getValue().getId() : null;
                return new DanhMuc(ten, txtMoTa.getText().trim(), "thu", null, parentId);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dm -> {
            boolean biTrung;
            if (dm.getParentId() != null) {
                biTrung = danhMucDAO.tonTaiTenDanhMucCon(dm.getTenDanhMuc(), dm.getLoai(), null, null);
            } else {
                biTrung = danhMucDAO.tonTaiTenDanhMuc(dm.getTenDanhMuc(), dm.getLoai(), null);
            }

            if (biTrung) {
                showAlert("Lỗi", "Tên danh mục Thu mặc định đã tồn tại!\nVui lòng nhập tên khác.");
                return;
            }

            if (danhMucDAO.themDanhMuc(dm)) {
                showAlert("Thành công", "Đã thêm: " + dm.getTenDanhMuc());
                refreshDanhMucThuPanel();
            } else {
                showAlert("Lỗi", "Thêm thất bại!");
            }
        });
    }

    private void handleSuaDanhMucThu() {
        DanhMuc selected = tableDanhMucThu.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Lỗi", "Vui lòng chọn danh mục cần sửa!"); return; }
        final String tenCu = selected.getTenDanhMuc() != null ? selected.getTenDanhMuc().trim() : "";
        final Integer parentCu = selected.getParentId();

        Dialog<DanhMuc> dialog = new Dialog<>();
        dialog.setTitle("Sửa danh mục Thu");
        dialog.setHeaderText("Chỉnh sửa: " + selected.getTenDanhMuc());

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField txtTen = new TextField(selected.getTenDanhMuc()); txtTen.setPrefWidth(300);
        TextArea txtMoTa = new TextArea(selected.getMoTa() != null ? selected.getMoTa() : "");
        txtMoTa.setPrefRowCount(3); txtMoTa.setPrefWidth(300);

        ComboBox<DanhMuc> cbCha = new ComboBox<>();
        cbCha.setPrefWidth(300);
        cbCha.setPromptText("(Không có - danh mục gốc)");
        List<DanhMuc> dsCha = layDanhMucChaMacDinh("thu", selected.getId());
        cbCha.getItems().setAll(dsCha);
        if (selected.getParentId() != null) {
            for (DanhMuc dm : dsCha) {
                if (dm.getId() == selected.getParentId()) {
                    cbCha.setValue(dm);
                    break;
                }
            }
        }

        grid.add(new Label("Tên danh mục:"), 0, 0); grid.add(txtTen, 1, 0);
        grid.add(new Label("Mô tả:"), 0, 1); grid.add(txtMoTa, 1, 1);
        grid.add(new Label("Danh mục cha:"), 0, 2); grid.add(cbCha, 1, 2);
        dialog.getDialogPane().setContent(grid);

        ButtonType btnOK = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == btnOK) {
                String ten = txtTen.getText().trim();
                if (ten.isEmpty()) { showAlert("Lỗi", "Vui lòng nhập tên!"); return null; }
                return new DanhMuc(
                    selected.getId(),
                    ten,
                    txtMoTa.getText().trim(),
                    selected.getLoai(),
                    selected.getSoTaiKhoan(),
                    cbCha.getValue() != null ? cbCha.getValue().getId() : null,
                    null
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dm -> {
            String tenMoi = dm.getTenDanhMuc() != null ? dm.getTenDanhMuc().trim() : "";
            boolean daDoiTen = !tenMoi.equalsIgnoreCase(tenCu);
            boolean daDoiCha = (parentCu == null && dm.getParentId() != null)
                    || (parentCu != null && !parentCu.equals(dm.getParentId()));

            if (dm.getParentId() != null) {
                if ((daDoiTen || daDoiCha)
                        && danhMucDAO.tonTaiTenDanhMucCon(tenMoi, dm.getLoai(), null, dm.getId())) {
                    showAlert("Lỗi", "Tên danh mục con Thu mặc định đã tồn tại (kể cả khác danh mục cha)!\nVui lòng nhập tên khác.");
                    return;
                }
            } else {
                if (daDoiTen && danhMucDAO.tonTaiTenDanhMuc(tenMoi, dm.getLoai(), null)) {
                    showAlert("Lỗi", "Tên danh mục Thu mặc định đã tồn tại!\nVui lòng nhập tên khác.");
                    return;
                }
            }

            if (danhMucDAO.suaDanhMuc(dm)) {
                showAlert("Thành công", "Đã cập nhật!");
                refreshDanhMucThuPanel();
            } else {
                showAlert("Lỗi", "Cập nhật thất bại!");
            }
        });
    }

    private void handleXoaDanhMucThu() {
        DanhMuc selected = tableDanhMucThu.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Lỗi", "Vui lòng chọn danh mục cần xóa!"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa"); confirm.setHeaderText("Xóa: " + selected.getTenDanhMuc());
        confirm.setContentText("⚠️ Các giao dịch đã dùng danh mục này sẽ mất liên kết!\nBạn có chắc chắn muốn xóa?");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                if (danhMucDAO.xoaDanhMuc(selected.getId())) {
                    showAlert("Thành công", "Đã xóa!");
                    refreshDanhMucThuPanel();
                } else {
                    showAlert("Lỗi", "Xóa thất bại!");
                }
            }
        });
    }

    // Chỉ cho chọn danh mục cha là danh mục gốc mặc định (1 cấp cha/con)
    private List<DanhMuc> layDanhMucChaMacDinh(String loai, Integer excludeId) {
        List<DanhMuc> all = danhMucDAO.layDanhMucTheoLoai(null, loai);
        List<DanhMuc> result = new java.util.ArrayList<>();
        for (DanhMuc dm : all) {
            if (excludeId != null && dm.getId() == excludeId) continue;
            if (dm.getParentId() == null) result.add(dm);
        }
        return result;
    }

    // ==================== PANEL: BÁO CÁO HỆ THỐNG ====================

    private void buildPanelBaoCao() {
        panelBaoCao = new VBox(20);
        panelBaoCao.setPadding(new Insets(5));

        Label title = new Label("Báo cáo hệ thống");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        // Stat cards row
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        lblTongNguoiDung = new Label("...");
        lblTongGiaoDich = new Label("...");
        lblTongTienLuuChuyen = new Label("...");
        lblBiKhoa = new Label("...");

        statsRow.getChildren().addAll(
            createStatCard("Tổng người dùng", lblTongNguoiDung, "#3498db"),
            createStatCard("Tổng GD chuyển khoản", lblTongGiaoDich, "#27ae60"),
            createStatCard("Tổng tiền chuyển khoản", lblTongTienLuuChuyen, "#9b59b6"),
            createStatCard("Tài khoản bị khóa", lblBiKhoa, "#e74c3c")
        );

        Label lblUserTitle = new Label("Chi tiết theo từng người dùng");
        lblUserTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        tableUserReport = new TableView<>();
        VBox.setVgrow(tableUserReport, Priority.ALWAYS);

        TableColumn<UserReportRow, String> c1 = new TableColumn<>("Tên đăng nhập");
        c1.setCellValueFactory(new PropertyValueFactory<>("tenDangNhap"));
        c1.setPrefWidth(130);

        TableColumn<UserReportRow, String> c2 = new TableColumn<>("Họ tên");
        c2.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        c2.setPrefWidth(160);

        TableColumn<UserReportRow, String> c3 = new TableColumn<>("Số TK");
        c3.setCellValueFactory(new PropertyValueFactory<>("soTaiKhoan"));
        c3.setPrefWidth(110);

        TableColumn<UserReportRow, String> c4 = new TableColumn<>("Số dư hiện tại");
        c4.setCellValueFactory(new PropertyValueFactory<>("soDu"));
        c4.setPrefWidth(140);

        TableColumn<UserReportRow, String> c5 = new TableColumn<>("GD CK đã gửi");
        c5.setCellValueFactory(new PropertyValueFactory<>("soGDDaGui"));
        c5.setPrefWidth(100);

        TableColumn<UserReportRow, String> c6 = new TableColumn<>("GD CK đã nhận");
        c6.setCellValueFactory(new PropertyValueFactory<>("soGDDaNhan"));
        c6.setPrefWidth(100);

        TableColumn<UserReportRow, String> c7 = new TableColumn<>("Trạng thái");
        c7.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        c7.setPrefWidth(120);

        tableUserReport.getColumns().addAll(c1, c2, c3, c4, c5, c6, c7);

        Button btnRefresh = new Button("Làm mới báo cáo");
        btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnRefresh.setOnAction(e -> refreshBaoCaoPanel());

        panelBaoCao.getChildren().addAll(title, statsRow, lblUserTitle, tableUserReport, btnRefresh);
    }

    private VBox createStatCard(String label, Label valueLabel, String color) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(15));
        card.setPrefWidth(215);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: " + color + "; -fx-border-width: 0 0 4 0;");

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");

        card.getChildren().addAll(lbl, valueLabel);
        return card;
    }

    private void refreshBaoCaoPanel() {
        try {
            List<NguoiDung> all = nguoiDungDAO.layTatCa();
            long tongUser = all.stream().filter(nd -> "nguoi_dung".equals(nd.getVaiTro())).count();
            long biKhoa = all.stream().filter(nd -> "bi_khoa".equals(nd.getTrangThai())).count();

            long tongGD = giaoDichDAO.demTatCaGiaoDich();
            BigDecimal tongTien = giaoDichDAO.tongSoTienGiaoDich();

            lblTongNguoiDung.setText(String.valueOf(tongUser));
            lblTongGiaoDich.setText(String.valueOf(tongGD));
            lblTongTienLuuChuyen.setText(df.format(tongTien) + " đ");
            lblBiKhoa.setText(String.valueOf(biKhoa));

            tableUserReport.getItems().clear();
            for (NguoiDung nd : all) {
                if ("quan_ly".equals(nd.getVaiTro())) continue;
                long gui = giaoDichDAO.demGDGui(nd.getSoTaiKhoan());
                long nhan = giaoDichDAO.demGDNhan(nd.getSoTaiKhoan());
                String soDuFmt = nd.getSoDu() != null ? df.format(nd.getSoDu()) + " đ" : "0 đ";
                String tt = "hoat_dong".equals(nd.getTrangThai()) ? "✅ Hoạt động" : "🔒 Bị khóa";
                tableUserReport.getItems().add(new UserReportRow(
                        nd.getTenDangNhap(), nd.getHoTen(), nd.getSoTaiKhoan(),
                        soDuFmt, String.valueOf(gui), String.valueOf(nhan), tt));
            }
        } catch (Exception e) {
            showAlert("Lỗi", "Không thể tải báo cáo: " + e.getMessage());
        }
    }

    // ==================== HELPERS ====================

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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public Scene getScene() {
        return scene;
    }
}
