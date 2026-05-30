package com.rahul.kovil.dotmatrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DotMatrixSampleService {

    private static final Logger log = LoggerFactory.getLogger(DotMatrixSampleService.class);
    
    private final DotMatrixPrintService printService;
    private final DotMatrixPrinterConfig printerConfig;

    public DotMatrixSampleService(DotMatrixPrintService printService, DotMatrixPrinterConfig printerConfig) {
        this.printService = printService;
        this.printerConfig = printerConfig;
    }

    /**
     * Demonstrates generating and printing a simple receipt.
     */
    public boolean printSampleReceipt() {
        int receiptWidth = 40; // Default width for standard small receipt, adjust based on paper

        DotMatrixReceiptBuilder builder = new DotMatrixReceiptBuilder(printerConfig);
        
        // Header
        builder.addRow(DotMatrixTextUtil.padLeft("SRI KOVIL TEMPLE", receiptWidth / 2 + 8)); 
        builder.addRow(DotMatrixTextUtil.padLeft("Official Receipt", receiptWidth / 2 + 8));
        builder.addDivider('-', receiptWidth);
        
        // Meta data
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"));
        builder.addLeftRightRow("Date:", dateStr, receiptWidth);
        builder.addLeftRightRow("Receipt No:", "RC-10045", receiptWidth);
        builder.addDivider('-', receiptWidth);

        // Items (Demonstrating truncation and multi-row)
        // Item 1: Long name that should be truncated
        String longName = "Krishnan Subramanian Pillai";
        builder.addLeftRightRow(longName, "150.00", receiptWidth);
        
        // Item 2: Multi-row
        builder.addRow("Archana - Ganesha");
        builder.addLeftRightRow("  (Star: Ashwini)", "50.00", receiptWidth);

        builder.addDivider('-', receiptWidth);
        
        // Total
        builder.addLeftRightRow("TOTAL:", "200.00", receiptWidth);
        builder.addDivider('=', receiptWidth);
        
        // Footer
        builder.addRow(DotMatrixTextUtil.padLeft("Thank You!", receiptWidth / 2 + 5));

        // Build the string payload
        String payload = builder.build();
        
        log.info("Generated Dot Matrix Payload:\n{}", payload);
        
        // Send to printer
        return printService.print(printerConfig.getPrinterName(), payload);
    }
}
