package com.example.util;

import javafx.scene.control.TextField;

import java.math.BigDecimal;

/**
 * Utility cho ô nhập tiền: tự chèn dấu chấm ngăn cách hàng nghìn.
 */
public final class MoneyInputUtil {

    private MoneyInputUtil() {}

    public static void attachMoneyFormatter(TextField field) {
        final boolean[] updating = {false};
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (updating[0]) return;
            updating[0] = true;
            try {
                String digits = newVal == null ? "" : newVal.replaceAll("[^\\d]", "");
                String formatted = formatDigitsWithDots(digits);
                if (!formatted.equals(newVal)) {
                    field.setText(formatted);
                }
                field.positionCaret(field.getText().length());
            } finally {
                updating[0] = false;
            }
        });
    }

    public static BigDecimal parseMoney(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^\\d]", "");
        if (digits.isEmpty()) return null;
        return new BigDecimal(digits);
    }

    public static void attachDigitsOnly(TextField field) {
        final boolean[] updating = {false};
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (updating[0]) return;
            updating[0] = true;
            try {
                String digits = newVal == null ? "" : newVal.replaceAll("[^\\d]", "");
                if (!digits.equals(newVal)) {
                    field.setText(digits);
                }
                field.positionCaret(field.getText().length());
            } finally {
                updating[0] = false;
            }
        });
    }

    private static String formatDigitsWithDots(String digits) {
        if (digits == null || digits.isEmpty()) return "";

        StringBuilder rev = new StringBuilder(digits).reverse();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < rev.length(); i++) {
            if (i > 0 && i % 3 == 0) out.append('.');
            out.append(rev.charAt(i));
        }
        return out.reverse().toString();
    }
}
