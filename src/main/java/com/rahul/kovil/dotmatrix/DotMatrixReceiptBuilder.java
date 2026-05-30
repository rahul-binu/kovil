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
     * Adds a simple row of text. It will be prefixed with the left offset during build.
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
    public String build() {
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
}
