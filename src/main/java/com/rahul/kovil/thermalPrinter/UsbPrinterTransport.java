package com.rahul.kovil.thermalPrinter;

import java.util.Arrays;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

public class UsbPrinterTransport implements PrinterTransport {

    private final String printerName;

    public UsbPrinterTransport(String printerName) {
        this.printerName = printerName;
    }

    @Override
    public void print(byte[] data) throws Exception {
        PrintService service = Arrays.stream(
                PrintServiceLookup.lookupPrintServices(null, null))
                .filter(p -> p.getName().equalsIgnoreCase(printerName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Printer not found"));

        DocPrintJob job = service.createPrintJob();
        Doc doc = new SimpleDoc(data, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
        job.print(doc, null);
    }
}
