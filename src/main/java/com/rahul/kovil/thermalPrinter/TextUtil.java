package com.rahul.kovil.thermalPrinter;

import java.util.ArrayList;
import java.util.List;

public class TextUtil {

    public static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s.length() > n ? s.substring(0, n) : s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s.length() > n ? s.substring(0, n) : s);
    }

    public static List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        while (text.length() > width) {
            lines.add(text.substring(0, width));
            text = text.substring(width);
        }
        lines.add(text);
        return lines;
    }
}
