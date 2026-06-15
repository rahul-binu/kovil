package com.rahul.kovil.dotmatrix;

import java.util.ArrayList;
import java.util.List;

public class DotMatrixReceiptBuilder {

    private final DotMatrixPrinterConfig config;
    private final List<String> lines;

    // Standard ASCII carriage return + line feed for dot matrix printers
    private static final String CRLF = "\r\n";

    public DotMatrixReceiptBuilder(DotMatrixPrinterConfig config) {
        this.config = config;
        this.lines = new ArrayList<>();

        // Add top offset if configured
        if (config.getTopOffset() > 0) {
            addBlankLines(config.getTopOffset());
        }
    }

    /**
     * Adds blank lines to the receipt.
     */
    public DotMatrixReceiptBuilder addBlankLines(int count) {
        for (int i = 0; i < count; i++) {
            lines.add(""); // Empty line, will be prefixed with leftOffset on build
        }
        return this;
    }

    /**
     * Adds a simple row of text. It will be prefixed with the left offset during
     * build.
     */
    public DotMatrixReceiptBuilder addRow(String content) {
        if (content == null) {
            content = "";
        }
        lines.add(content);
        return this;
    }

    /**
     * Adds a row formatted as left-aligned text and right-aligned text.
     */
    public DotMatrixReceiptBuilder addLeftRightRow(String leftText, String rightText, int totalWidth) {
        return addRow(DotMatrixTextUtil.alignLeftRight(leftText, rightText, totalWidth));
    }

    /**
     * Adds a divider row (e.g., "--------------------").
     */
    public DotMatrixReceiptBuilder addDivider(char ch, int width) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width; i++) {
            sb.append(ch);
        }
        return addRow(sb.toString());
    }

    /**
     * Builds the final plain text payload ready to be sent to the printer.
     */
    public String buildOld() {
        StringBuilder sb = new StringBuilder();

        // Pre-compute left offset string
        String leftOffsetStr = "";
        if (config.getLeftOffset() > 0) {
            StringBuilder offsetSb = new StringBuilder();
            for (int i = 0; i < config.getLeftOffset(); i++) {
                offsetSb.append(" ");
            }
            leftOffsetStr = offsetSb.toString();
        }

        // Process all lines
        for (String line : lines) {
            sb.append(leftOffsetStr).append(line).append(CRLF);
        }

        // Add feed lines at the end to move paper to tear-off position
        if (config.getFeedLines() > 0) {
            for (int i = 0; i < config.getFeedLines(); i++) {
                sb.append(CRLF); // Feed lines don't need left offset
            }
        }

        return sb.toString();
    }

    /**
     * Adds text at an absolute line number and column position.
     * Use this for pre-printed forms where fields have fixed positions.
     *
     * @param content Text to place
     * @param line    0-based line number from top of receipt
     * @param col     0-based column from left (after leftOffset)
     */
    public DotMatrixReceiptBuilder addFieldAt(String content, int line, int col) {
        if (content == null)
            content = "";

        // Expand lines list if needed
        while (lines.size() <= line) {
            lines.add(null); // null = unset line
        }

        String existing = lines.get(line);
        if (existing == null)
            existing = "";

        // Pad existing line to reach the target column
        if (existing.length() < col) {
            existing = existing + " ".repeat(col - existing.length());
        }

        // Insert content at column, overwriting characters
        String before = existing.substring(0, col);
        String after = existing.length() > col + content.length()
                ? existing.substring(col + content.length())
                : "";
        lines.set(line, before + content + after);

        return this;
    }

    /**
     * Adds text at a position defined as percentage of receipt dimensions.
     * Useful when positions are configured via the position editor.
     *
     * @param content      Text to place
     * @param xPct         Horizontal position as % of receiptWidth (0–100)
     * @param yPct         Vertical position as % of totalLines (0–100)
     * @param receiptWidth Total width in characters (e.g. 40)
     * @param totalLines   Total lines on the receipt form (e.g. 30)
     */
    public DotMatrixReceiptBuilder addFieldAtPct(String content,
            float xPct, float yPct,
            int receiptWidth, int totalLines) {
        int col = Math.round(xPct / 100f * receiptWidth);
        int line = Math.round(yPct / 100f * totalLines);
        return addFieldAt(content, line, col);
    }

    /**
     * Adds multiple vendor rows starting at a base line,
     * each row spaced by rowSpacing lines.
     *
     * @param nameCol    Column for vendor name
     * @param starCol    Column for nakshatra
     * @param amtCol     Column for amount
     * @param baseLine   Starting line for first vendor
     * @param rowSpacing Lines between each vendor row
     * @param vendors    List of String[]{name, nakshatra, amount}
     */
    public DotMatrixReceiptBuilder addVendorRows(int nameCol, int starCol, int amtCol,
            int baseLine, int rowSpacing,
            List<String[]> vendors) {
        for (int i = 0; i < vendors.size(); i++) {
            String[] v = vendors.get(i);
            int line = baseLine + (i * rowSpacing);
            addFieldAt(v.length > 0 ? v[0] : "", line, nameCol); // name
            addFieldAt(v.length > 1 ? v[1] : "", line, starCol); // nakshatra
            addFieldAt(v.length > 2 ? v[2] : "", line, amtCol); // amount
        }
        return this;
    }

    /**
     * Convert Y in centimeters (from top) to a row number used by addFieldAt.
     * Uses the project's empirical conversion factor: row = round(Y_cm * 2.362).
     */
    public static int cmToRow(double yCm) {
        return (int) Math.round(yCm * 2.362);
    }

    /**
     * Convert X in centimeters (from left) to a column number used by addFieldAt.
     * Uses the project's empirical conversion factor: col = round(X_cm * 3.937).
     */
    public static int cmToCol(double xCm) {
        return (int) Math.round(xCm * 3.937);
    }

    /**
     * Convenience method to place a field using physical cm coordinates.
     * 
     * @param content text
     * @param yCm     vertical offset in cm from top
     * @param xCm     horizontal offset in cm from left
     */
    public DotMatrixReceiptBuilder addFieldAtCm(String content, double yCm, double xCm) {
        int row = cmToRow(yCm);
        int col = cmToCol(xCm);
        return addFieldAt(content, row, col);
    }

    /**
     * Override build() to handle null lines (gaps between positioned fields).
     * Replaces nulls with empty lines.
     */
    public String build() {
        StringBuilder sb = new StringBuilder();

        String leftOffsetStr = config.getLeftOffset() > 0
                ? " ".repeat(config.getLeftOffset())
                : "";

        for (String line : lines) {
            sb.append(leftOffsetStr)
                    .append(line == null ? "" : line)
                    .append(CRLF);
        }

        if (config.getFeedLines() > 0) {
            for (int i = 0; i < config.getFeedLines(); i++) {
                sb.append(CRLF);
            }
        }

        return sb.toString();
    }
}
