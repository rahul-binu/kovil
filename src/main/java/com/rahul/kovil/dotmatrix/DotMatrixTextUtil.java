package com.rahul.kovil.dotmatrix;

public class DotMatrixTextUtil {

    /**
     * Truncates the text to the specified length and appends "..." if it exceeds the length.
     * E.g., truncate("Krishnan Subramanian Pillai", 18) -> "Krishnan Subraman..."
     */
    public static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        if (maxLength <= 3) {
            return text.substring(0, maxLength); // Too short for "..."
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Pads text with spaces on the right to ensure it fits the exact length.
     */
    public static String padRight(String text, int length) {
        if (text == null) {
            text = "";
        }
        if (text.length() > length) {
            return truncate(text, length);
        }
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < length) {
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Pads text with spaces on the left to ensure it fits the exact length.
     */
    public static String padLeft(String text, int length) {
        if (text == null) {
            text = "";
        }
        if (text.length() > length) {
            return truncate(text, length);
        }
        StringBuilder sb = new StringBuilder();
        int spaces = length - text.length();
        for (int i = 0; i < spaces; i++) {
            sb.append(" ");
        }
        sb.append(text);
        return sb.toString();
    }

    /**
     * Aligns leftText to the left and rightText to the right, separated by spaces.
     * If the combined length exceeds totalWidth, it intelligently truncates.
     */
    public static String alignLeftRight(String leftText, String rightText, int totalWidth) {
        if (leftText == null) leftText = "";
        if (rightText == null) rightText = "";

        if (leftText.length() + rightText.length() > totalWidth) {
            // Priority given to right text (usually amount/price)
            if (rightText.length() > totalWidth) {
                rightText = rightText.substring(0, totalWidth);
                leftText = "";
            } else {
                leftText = truncate(leftText, totalWidth - rightText.length() - 1); // -1 for at least one space
            }
        }

        StringBuilder sb = new StringBuilder(leftText);
        int spaces = totalWidth - leftText.length() - rightText.length();
        for (int i = 0; i < spaces; i++) {
            sb.append(" ");
        }
        sb.append(rightText);
        return sb.toString();
    }
}
