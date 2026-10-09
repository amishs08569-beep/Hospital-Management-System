package com.hospital.util;

import com.hospital.config.AppConfig;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyUtil {

    private static final NumberFormat FORMATTER = NumberFormat.getCurrencyInstance(Locale.US);

    private CurrencyUtil() {}

    public static String format(BigDecimal amount) {
        if (amount == null) amount = BigDecimal.ZERO;
        String formatted = FORMATTER.format(amount);
        String symbol = AppConfig.getCurrencySymbol();
        if (!"$".equals(symbol)) {
            return formatted.replace("$", symbol + " ");
        }
        return formatted;
    }
}
