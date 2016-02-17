package com.laserfountain.circly;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class NumberFormatter {

    private static DecimalFormatSymbols symbols = null;
    private static DecimalFormat noDecimals = null;
    private static DecimalFormat oneDecimal = null;
    private static DecimalFormat twoDecimals = null;
    private static DecimalFormat threeDecimals = null;

    private static String[] names = {
            "Million",
            "Billion",
            "Trillion",
            "Quadrillion",
            "Quintillion",
            "Sextillion",
            "Septillion",
            "Octillion",
            "Nonillion",
            "Decillion",
            "Undecillion",
            "Duodecillion",
            "Tredecillion",
            "Quattuordecillion",
            "Quinquadecillion",
            "Sedecillion",
            "Septendecillion",
            "Octodecillion",
            "Novendecillion",
            "Vigintillion",
            "Unvigintillion",
            "Duovigintillion",
            "Tresvigintillion",
            "Quattuorvigintillion",
            "Quinquavigintillion",
            "Sesvigintillion",
            "Septemvigintillion",
            "Octovigintillion",
            "Novemvigintillion",
            "Trigintillion",
            "Untrigintillion",
            "Duotrigintillion",
            "Trestrigintillion",
            "Quattuortrigintillion",
            "Quinquatrigintillion",
            "Sestrigintillion",
            "Septentrigintillion",
            "Octotrigintillion",
            "Noventrigintillion",
            "Quadragintillion"
    };

    private static void init() {
        if (symbols != null) {
            return;
        }
        symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        noDecimals = new DecimalFormat("###,###", symbols);
        oneDecimal = new DecimalFormat("###,##0.0", symbols);
        twoDecimals = new DecimalFormat("###,##0.00", symbols);
        threeDecimals = new DecimalFormat("###,##0.000", symbols);
    }

    public static String formatDouble(double n) {
        init();
        if (n < 10000) {
            return twoDecimals.format(n);
        }
        if (n < 100000) {
            return oneDecimal.format(n);
        }
        if (n < 1000000) {
            return noDecimals.format(n);
        }
        n = n / 1000000;
        int base = 0;
        while (base < 40) {
            if (n < 100) {
                return threeDecimals.format(n) + " " + names[base];
            }
            if (n < 1000) {
                return twoDecimals.format(n) + " " + names[base];
            }
            n = n / 1000;
            base++;
        }
        return "NaN";
    }

    public static String formatDoubleCompact(double n) {
        init();
        if (n < 1000000) {
            return noDecimals.format(n);
        }
        n = n / 1000000;
        int base = 0;
        while (base < 40) {
            if (n < 1000) {
                return noDecimals.format(n) + " " + names[base];
            }
            n = n / 1000;
            base++;
        }
        return "NaN";
    }

    public static String formatInt(int number) {
        return Integer.toString(number);
    }

    public static String formatDoubleTime(double v) {
        double seconds = v;
        double minutes = Math.floor(seconds / 60);
        double hours = Math.floor(minutes / 60);
        double days = Math.floor(hours / 24);
        hours -= days * 24;
        minutes -= hours * 60;
        seconds -= minutes * 60;
        String result = "";
        if (days > 0) {
            result = result + " " + formatDoubleCompact(days) + " days";
        }
        if (hours > 0) {
            result = result + " " + formatDoubleCompact(hours) + " hours";
        }
        if (minutes > 0) {
            result = result + " " + formatDoubleCompact(minutes) + " minutes";
        }
        if (seconds > 0) {
            result = result + " " + formatDoubleCompact(seconds) + " seconds";
        }
        return result;
    }
}
