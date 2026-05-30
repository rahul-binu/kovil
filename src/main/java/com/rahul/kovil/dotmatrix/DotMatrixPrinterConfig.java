package com.rahul.kovil.dotmatrix;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "printer.dotmatrix")
public class DotMatrixPrinterConfig {

    private String printerName = "EPSON LX-310"; // Default name, can be overridden in application.properties
    private int topOffset = 0;
    private int leftOffset = 0;
    private int feedLines = 5; // Number of extra blank lines after a receipt to move to tear-off position

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public int getTopOffset() {
        return topOffset;
    }

    public void setTopOffset(int topOffset) {
        this.topOffset = topOffset;
    }

    public int getLeftOffset() {
        return leftOffset;
    }

    public void setLeftOffset(int leftOffset) {
        this.leftOffset = leftOffset;
    }

    public int getFeedLines() {
        return feedLines;
    }

    public void setFeedLines(int feedLines) {
        this.feedLines = feedLines;
    }
}
