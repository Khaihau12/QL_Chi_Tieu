package com.example.exception;

import java.sql.Timestamp;

/**
 * Exception ném ra khi tài khoản bị khóa, mang theo lý do và thời gian mở khóa.
 */
public class TaiKhoanBiKhoaException extends RuntimeException {
    private final String lyDoKhoa;
    private final Timestamp thoiGianMoKhoa; // null = vĩnh viễn

    public TaiKhoanBiKhoaException(String lyDoKhoa, Timestamp thoiGianMoKhoa) {
        super("Tài khoản đã bị khóa");
        this.lyDoKhoa = lyDoKhoa;
        this.thoiGianMoKhoa = thoiGianMoKhoa;
    }

    public String getLyDoKhoa() {
        return lyDoKhoa;
    }

    public Timestamp getThoiGianMoKhoa() {
        return thoiGianMoKhoa;
    }
}
