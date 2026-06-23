package com.example.controller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CategoryControllerValidationTest {

    @Test
    public void TC01_validateInputDanhMuc_tenDanhMucRong() {
        String actual = CategoryController.validateInputDanhMuc("", false);

        assertEquals("Vui lòng nhập tên danh mục!", actual);
    }

    @Test
    public void TC02_validateInputDanhMuc_tenDanhMucBiTrung() {
        String actual = CategoryController.validateInputDanhMuc("Ăn uống", true);

        assertEquals("Tên danh mục đã tồn tại! Vui lòng nhập tên khác.", actual);
    }

    @Test
    public void TC03_validateInputDanhMuc_hopLe() {
        String actual = CategoryController.validateInputDanhMuc("Ăn uống", false);

        assertNull(actual);
    }
}
