package com.rahul.kovil.dotmatrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import java.awt.*;
import java.awt.print.*;
import java.io.ByteArrayOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Service
public class DotMatrixPrintService {

    private static final Logger log = LoggerFactory.getLogger(DotMatrixPrintService.class);

    /**
     * Looks up a printer by its exact OS name (case-insensitive contains match for flexibility).
     */
    public PrintService getPrinter(String printerName) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printer : printServices) {
            if (printer.getName().toLowerCase().contains(printerName.toLowerCase())) {
                return printer;
            }
        }
        return null;
    }

    /**
     * Prints the plain text payload to the specified printer.
     */
    public boolean print(String printerName, String textPayload) {
        PrintService printer = getPrinter(printerName);
        if (printer == null) {
            log.error("Printer not found: {}", printerName);
            return false;
        }

        byte[] bytes = buildRawPayload(textPayload);

        if (isWindows()) {
            try {
                boolean rawOk = RawPrinterHelper.printBytesToPrinter(printer.getName(), bytes);
                if (rawOk) {
                    log.info("Successfully sent raw payload to printer via Winspool: {}", printer.getName());
                    return true;
                } else {
                    log.warn("Raw Winspool send failed, falling back to Java PrintService: {}", printer.getName());
                }
            } catch (Throwable t) {
                log.warn("Raw Winspool attempt threw, falling back to Java PrintService: {}", printer.getName(), t);
            }
        }

        try {
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            Doc doc = new SimpleDoc(bytes, flavor, null);
            DocPrintJob printJob = printer.createPrintJob();
            printJob.print(doc, null);
            log.info("Successfully sent plain text payload to printer: {}", printer.getName());
            return true;
        } catch (Exception e) {
            log.warn("Plain byte print attempt failed, trying text-driver print: {}", printer.getName(), e);
        }

        if (printTextViaDriver(printerName, textPayload)) {
            return true;
        }

        if (printWithPrinterJob(printerName, textPayload)) {
            return true;
        }

        log.error("Failed to print payload to printer: {}", printerName);
        return false;
    }

    public boolean printTextViaDriver(String printerName, String textPayload) {
        try {
            PrintService printer = getPrinter(printerName);
            if (printer == null) {
                log.error("Printer not found: {}", printerName);
                return false;
            }

            DocFlavor flavor;
            Doc doc;
            DocFlavor utf8Flavor = new DocFlavor("text/plain; charset=utf-8", "byte[]");
            DocFlavor asciiFlavor = new DocFlavor("text/plain; charset=us-ascii", "byte[]");
            if (printer.isDocFlavorSupported(utf8Flavor)) {
                flavor = utf8Flavor;
                doc = new SimpleDoc(textPayload.getBytes(StandardCharsets.UTF_8), flavor, null);
            } else if (printer.isDocFlavorSupported(asciiFlavor)) {
                flavor = asciiFlavor;
                doc = new SimpleDoc(textPayload.getBytes(StandardCharsets.US_ASCII), flavor, null);
            } else if (printer.isDocFlavorSupported(DocFlavor.STRING.TEXT_PLAIN)) {
                flavor = DocFlavor.STRING.TEXT_PLAIN;
                doc = new SimpleDoc(textPayload, flavor, null);
            } else {
                flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
                doc = new SimpleDoc(textPayload.getBytes(StandardCharsets.US_ASCII), flavor, null);
            }

            DocPrintJob printJob = printer.createPrintJob();
            printJob.print(doc, null);

            log.info("Successfully sent text payload via Java PrintService to printer: {} using flavor {}", printer.getName(), flavor);
            return true;
        } catch (Exception e) {
            log.error("Exception occurred while printing text via driver: {}", printerName, e);
            return false;
        }
    }

    public boolean printWithPrinterJob(String printerName, String textPayload) {
        try {
            PrintService printer = getPrinter(printerName);
            if (printer == null) {
                log.error("Printer not found: {}", printerName);
                return false;
            }

            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("KovilDotMatrixPrint");
            job.setPrintService(printer);
            // Configure a custom Paper that matches the physical continuous feed paper
            // Paper dimensions: Height = 10.0 cm (feed direction), Width = 15.5 cm
            double cmToPts = 28.3464567; // points per cm
            double paperHeightPts = 10.0 * cmToPts; // ~283.46 pt
            double paperWidthPts = 15.5 * cmToPts; // ~439.43 pt

            PageFormat pf = job.defaultPage();
            Paper paper = pf.getPaper();
            paper.setSize(paperWidthPts, paperHeightPts);
            // Set imageable area to the full paper so Graphics2D origin is at top-left of paper
            paper.setImageableArea(0, 0, paperWidthPts, paperHeightPts);
            pf.setPaper(paper);
            pf.setOrientation(PageFormat.PORTRAIT);

            log.info("Configured custom Paper for printer {}: width={}pt height={}pt imageableY={}", printer.getName(), paper.getWidth(), paper.getHeight(), paper.getImageableY());

            // Use the configured PageFormat when printing so Java's coordinate system matches physical paper
            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2 = (Graphics2D) graphics;
                // imageableX/Y will be zero because we set imageable area to the full paper above
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                g2.setPaint(Color.black);
                g2.setFont(new Font("Monospaced", Font.PLAIN, 10));

                FontMetrics fm = g2.getFontMetrics();
                float lineHeight = fm.getHeight();
                float ascent = fm.getAscent();

                log.info("Printing diagnostics: font={} lineHeight={}pt ascent={}pt imageableY={} pageHeight={}pt", g2.getFont(), lineHeight, ascent, pageFormat.getImageableY(), pageFormat.getHeight());

                String[] lines = textPayload.split("\\r?\\n");
                float y = 0f;
                for (String line : lines) {
                    y += lineHeight;
                    // drawString y param is baseline; adjust so first line sits correctly using ascent
                    g2.drawString(line, 0, y - (lineHeight - ascent));
                }
                return Printable.PAGE_EXISTS;
            }, pf);

            job.print();
            log.info("Successfully sent payload via PrinterJob: {}", printer.getName());
            return true;
        } catch (Exception e) {
            log.error("Exception occurred while printing with PrinterJob: {}", printerName, e);
            return false;
        }
    }

    public String[] getSupportedDocFlavors(String printerName) {
        PrintService printer = getPrinter(printerName);
        if (printer == null) {
            return new String[0];
        }
        return java.util.Arrays.stream(printer.getSupportedDocFlavors())
                .map(flavor -> flavor.getMimeType() + " (" + flavor.getRepresentationClassName() + ")")
                .toArray(String[]::new);
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }

    private boolean printUsingPowerShell(String printerName, String textPayload) throws IOException, InterruptedException {
        File tempFile = File.createTempFile("kovil-print-", ".txt");
        tempFile.deleteOnExit();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.US_ASCII))) {
            writer.write(textPayload);
        }

        String command = String.format("powershell.exe -NoProfile -Command \"Get-Content -LiteralPath '%s' | Out-Printer -Name '%s'\"", tempFile.getAbsolutePath().replace("'", "''"), printerName.replace("'", "''"));
        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        try (var reader = new java.io.BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.warn("PowerShell print command failed with exit code {} and output: {}", exitCode, output.toString());
        }
        return exitCode == 0;
    }

    private boolean printUsingWindowsCommand(String printerName, String textPayload) throws IOException, InterruptedException {
        File tempFile = File.createTempFile("kovil-print-", ".txt");
        tempFile.deleteOnExit();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.US_ASCII))) {
            writer.write(textPayload);
        }

        String command = String.format("print /D:\"%s\" \"%s\"", printerName, tempFile.getAbsolutePath());
        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        try (var reader = new java.io.BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.warn("Windows print command failed with exit code {} and output: {}", exitCode, output.toString());
        }
        return exitCode == 0;
    }

    /**
     * Build raw byte payload for dot-matrix printers.
     * Adds an ESC @ init sequence, normalizes line endings to CRLF,
     * and appends a form-feed so the printer will advance/eject.
     */
    private byte[] buildRawPayload(String text) {
        try {
            // Normalize line endings to CRLF
            String normalized = text.replace("\r\n", "\n").replace("\r", "\n").replace("\n", "\r\n");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // ESC @ (initialize printer)
            baos.write(0x1B);
            baos.write('@');

            baos.write(normalized.getBytes(StandardCharsets.US_ASCII));

            // Form feed to eject / advance paper
            baos.write(0x0C);

            return baos.toByteArray();
        } catch (Exception e) {
            log.warn("Failed to build raw payload, falling back to ASCII bytes", e);
            return text.getBytes(StandardCharsets.US_ASCII);
        }
    }
}
