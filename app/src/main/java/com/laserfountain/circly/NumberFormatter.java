package com.laserfountain.circly;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public class NumberFormatter {

    private static DecimalFormatSymbols symbols;
    private static DecimalFormat df;
    private static DecimalFormat dfsmall;

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

    public static String formatDouble(double n) {
        symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        df = new DecimalFormat("###,###", symbols);
        dfsmall = new DecimalFormat("###,##0.00", symbols);
        if (n < 10000) {
            return dfsmall.format(n);
        }
        if (n < 1000000) {
            return df.format(n);
        }
        n = n / 1000000;
        int base = 0;
        while (base < 40) {
            if (n < 1000) {
                return df.format(n) + " " + names[base];
            }
            n = n / 1000;
            base++;
        }
        return "NaN";
    }

    public static String formatInt(int number) {
        return Integer.toString(number);
    }
}
