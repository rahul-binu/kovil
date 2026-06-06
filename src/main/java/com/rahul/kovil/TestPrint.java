package com.rahul.kovil;

import java.awt.*;
import java.awt.print.*;

public class TestPrint {

    public static void main(String[] args) throws Exception {

        PrinterJob job = PrinterJob.getPrinterJob();

        job.setPrintable((graphics, pageFormat, pageIndex) -> {

            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2 = (Graphics2D) graphics;

            g2.drawString("HELLO EPSON LX310", 100, 100);

            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            job.print();
        }
    }
}