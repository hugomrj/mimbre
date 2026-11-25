package com.app.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class FormatoNumero {

    private FormatoNumero() {
    }

    public static String miles(BigDecimal valor) {
        if (valor == null) return "";
        DecimalFormatSymbols simbolos = DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-PY"));
        simbolos.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", simbolos)
                .format(valor.setScale(0, RoundingMode.HALF_UP));
    }
}
