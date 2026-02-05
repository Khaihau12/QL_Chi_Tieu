package com.example.controller;

import com.example.dao.DanhMucDAO;
import com.example.model.DanhMuc;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;
import java.util.Random;

/**
 * Controller cho màn hình Quản lý Danh mục
 */
public class CategoryController {
    private Stage stage;
    private Scene scene;
    private String soTaiKhoan;
    
    private TableView<DanhMuc> table;
    private DanhMucDAO danhMucDAO;
    
    public CategoryController(Stage stage, String soTaiKhoan) {
        this.stage = stage;
        this.soTaiKhoan = soTaiKhoan;
        this.danhMucDAO = new DanhMucDAO();
        
        createUI();
    }
    
    private void createUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        
        // Header
        Label title = new Label("📂 QUẢN LÝ DANH MỤC");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Hướng dẫn
        Label guide = new Label("💡 Danh mục mặc định: Không thể sửa/xóa | Danh mục riêng ⭐: Có thể sửa/xóa");
        guide.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        
        // Bảng danh mục
        table = createTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        
        // Nút chức năng
        HBox buttonBox = createButtonBox();
        
        root.getChildren().addAll(title, guide, table, buttonBox);
        
        scene = new Scene(root, 900, 600);
        loadData();
    }
    
    private TableView<DanhMuc> createTable() {
        TableView<DanhMuc> table = new TableView<>();
        
        TableColumn<DanhMuc, String> colTen = new TableColumn<>("Tên Danh Mục");
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenDanhMuc"));
        colTen.setPrefWidth(250);
        
        TableColumn<DanhMuc, String> colMoTa = new TableColumn<>("Mô Tả");
        colMoTa.setCellValueFactory(new PropertyValueFactory<>("moTa"));
        colMoTa.setPrefWidth(300);
        
        TableColumn<DanhMuc, String> colMau = new TableColumn<>("Màu Sắc");
        colMau.setCellValueFactory(new PropertyValueFactory<>("mauSac"));
        colMau.setPrefWidth(100);
        colMau.setCellFactory(col -> new TableCell<DanhMuc, String>() {
            @Override
            protected void updateItem(String mauSac, boolean empty) {
                super.updateItem(mauSac, empty);
                if (empty || mauSac == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(mauSac);
                    setStyle("-fx-background-color: " + mauSac + "; -fx-text-fill: white; -fx-font-weight: bold;");
                }
            }
        });
        
        TableColumn<DanhMuc, String> colLoai = new TableColumn<>("Loại");
        colLoai.setPrefWidth(150);
        colLoai.setCellFactory(col -> new TableCell<DanhMuc, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    DanhMuc dm = getTableView().getItems().get(getIndex());
                    if (dm.isDanhMucMacDinh()) {
                        setText("🌐 Mặc định");
                        setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                    } else {
                        setText("⭐ Riêng tư");
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        table.getColumns().addAll(colTen, colMoTa, colMau, colLoai);
        return table;
    }
    
    private HBox createButtonBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        
        Button btnThem = new Button("➕ Thêm Danh Mục");
        btnThem.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnThem.setOnAction(e -> handleThem());
        
        Button btnSua = new Button("✏️ Sửa");
        btnSua.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        btnSua.setOnAction(e -> handleSua());
        
        Button btnXoa = new Button("🗑️ Xóa");
        btnXoa.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnXoa.setOnAction(e -> handleXoa());
        
        Button btnQuayLai = new Button("⬅️ Quay Lại");
        btnQuayLai.setOnAction(e -> {
            DashboardController dashboard = new DashboardController(stage);
            stage.setScene(dashboard.getScene());
        });
        
        box.getChildren().addAll(btnThem, btnSua, btnXoa, btnQuayLai);
        return box;
    }
    
    private void loadData() {
        List<DanhMuc> danhSach = danhMucDAO.layTatCaDanhMuc(soTaiKhoan);
        table.getItems().setAll(danhSach);
    }
    
    private void handleThem() {
        Dialog<DanhMuc> dialog = new Dialog<>();
        dialog.setTitle("Thêm Danh Mục Riêng");
        dialog.setHeaderText("Tạo danh mục cho riêng bạn");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField txtTen = new TextField();
        txtTen.setPromptText("Tên danh mục (VD: Café sáng, Tiền trọ...)");
        
        TextArea txtMoTa = new TextArea();
        txtMoTa.setPromptText("Mô tả chi tiết...");
        txtMoTa.setPrefRowCount(3);
        
        // Color Picker
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(Color.web(getRandomColor()));
        
        // Preview màu
        Label lblPreview = new Label("Xem trước");
        lblPreview.setPrefWidth(200);
        lblPreview.setPrefHeight(40);
        lblPreview.setAlignment(Pos.CENTER);
        lblPreview.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        colorPicker.setOnAction(e -> {
            String hex = toHexString(colorPicker.getValue());
            lblPreview.setStyle("-fx-background-color: " + hex + "; -fx-font-weight: bold; -fx-text-fill: white;");
        });
        lblPreview.setStyle("-fx-background-color: " + toHexString(colorPicker.getValue()) + 
                           "; -fx-font-weight: bold; -fx-text-fill: white;");
        
        grid.add(new Label("Tên danh mục:"), 0, 0);
        grid.add(txtTen, 1, 0);
        grid.add(new Label("Mô tả:"), 0, 1);
        grid.add(txtMoTa, 1, 1);
        grid.add(new Label("Màu sắc:"), 0, 2);
        grid.add(colorPicker, 1, 2);
        grid.add(new Label("Xem trước:"), 0, 3);
        grid.add(lblPreview, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType btnOK = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOK, ButtonType.CANCEL);
        
        dialog.setResultConverter(btn -> {
            if (btn == btnOK) {
                String ten = txtTen.getText().trim();
                String moTa = txtMoTa.getText().trim();
                String mauSac = toHexString(colorPicker.getValue());
                
                if (ten.isEmpty()) {
                    showAlert("Lỗi", "Vui lòng nhập tên danh mục!");
                    return null;
                }
                
                return new DanhMuc(ten, moTa, mauSac, soTaiKhoan);
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(dm -> {
            if (danhMucDAO.themDanhMuc(dm)) {
                showAlert("Thành công", "Đã thêm danh mục: " + dm.getTenDanhMuc());
                loadData();
            } else {
                showAlert("Lỗi", "Thêm danh mục thất bại!");
            }
        });
    }
    
    private void handleSua() {
        DanhMuc selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Lỗi", "Vui lòng chọn danh mục cần sửa!");
            return;
        }
        
        if (selected.isDanhMucMacDinh()) {
            showAlert("Lỗi", "Không thể sửa danh mục mặc định!");
            return;
        }
        
        Dialog<DanhMuc> dialog = new Dialog<>();
        dialog.setTitle("Sửa Danh Mục");
        dialog.setHeaderText("Chỉnh sửa: " + selected.getTenDanhMuc());
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField txtTen = new TextField(selected.getTenDanhMuc());
        TextArea txtMoTa = new TextArea(selected.getMoTa());
        txtMoTa.setPrefRowCount(3);
        
        ColorPicker colorPicker = new ColorPicker(Color.web(selected.getMauSac()));
        
        Label lblPreview = new Label("Xem trước");
        lblPreview.setPrefWidth(200);
        lblPreview.setPrefHeight(40);
        lblPreview.setAlignment(Pos.CENTER);
        lblPreview.setStyle("-fx-background-color: " + selected.getMauSac() + 
                           "; -fx-font-weight: bold; -fx-text-fill: white;");
        colorPicker.setOnAction(e -> {
            String hex = toHexString(colorPicker.getValue());
            lblPreview.setStyle("-fx-background-color: " + hex + "; -fx-font-weight: bold; -fx-text-fill: white;");
        });
        
        grid.add(new Label("Tên danh mục:"), 0, 0);
        grid.add(txtTen, 1, 0);
        grid.add(new Label("Mô tả:"), 0, 1);
        grid.add(txtMoTa, 1, 1);
        grid.add(new Label("Màu sắc:"), 0, 2);
        grid.add(colorPicker, 1, 2);
        grid.add(new Label("Xem trước:"), 0, 3);
        grid.add(lblPreview, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType btnOK = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOK, ButtonType.CANCEL);
        
        dialog.setResultConverter(btn -> {
            if (btn == btnOK) {
                String ten = txtTen.getText().trim();
                String moTa = txtMoTa.getText().trim();
                String mauSac = toHexString(colorPicker.getValue());
                
                if (ten.isEmpty()) {
                    showAlert("Lỗi", "Vui lòng nhập tên danh mục!");
                    return null;
                }
                
                selected.setTenDanhMuc(ten);
                selected.setMoTa(moTa);
                selected.setMauSac(mauSac);
                return selected;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(dm -> {
            if (danhMucDAO.suaDanhMuc(dm)) {
                showAlert("Thành công", "Đã cập nhật danh mục!");
                loadData();
            } else {
                showAlert("Lỗi", "Cập nhật thất bại!");
            }
        });
    }
    
    private void handleXoa() {
        DanhMuc selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Lỗi", "Vui lòng chọn danh mục cần xóa!");
            return;
        }
        
        if (selected.isDanhMucMacDinh()) {
            showAlert("Lỗi", "Không thể xóa danh mục mặc định!");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa danh mục: " + selected.getTenDanhMuc());
        confirm.setContentText("Bạn có chắc chắn muốn xóa?\n\n⚠️ LƯU Ý: Các giao dịch đã dùng danh mục này sẽ mất liên kết!");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (danhMucDAO.xoaDanhMuc(selected.getId())) {
                    showAlert("Thành công", "Đã xóa danh mục!");
                    loadData();
                } else {
                    showAlert("Lỗi", "Xóa thất bại!");
                }
            }
        });
    }
    
    // Helper methods
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }
    
    private String getRandomColor() {
        String[] colors = {
            "#e74c3c", "#3498db", "#9b59b6", "#f39c12", 
            "#27ae60", "#e67e22", "#95a5a6", "#34495e",
            "#1abc9c", "#2ecc71", "#f1c40f", "#e91e63"
        };
        return colors[new Random().nextInt(colors.length)];
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
