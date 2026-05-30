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
import java.nio.charset.StandardCharsets;

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
        try {
            PrintService printer = getPrinter(printerName);
            if (printer == null) {
                log.error("Printer not found: {}", printerName);
                return false;
            }

            // Convert string to bytes. 
            // Dot matrix printers typically expect raw ASCII/byte array data.
            byte[] bytes = textPayload.getBytes(StandardCharsets.US_ASCII);

            // AUTOSENSE tells the print service to send the bytes directly without drivers mangling it
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            Doc doc = new SimpleDoc(bytes, flavor, null);

            DocPrintJob printJob = printer.createPrintJob();
            printJob.print(doc, null);
            
            log.info("Successfully sent plain text payload to printer: {}", printer.getName());
            return true;

        } catch (Exception e) {
            log.error("Exception occurred while printing to dot matrix printer: {}", printerName, e);
            return false;
        }
    }
}
