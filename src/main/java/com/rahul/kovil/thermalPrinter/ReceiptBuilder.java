package com.rahul.kovil.thermalPrinter;

public class ReceiptBuilder {

    public static byte[] build(PrinterConfig cfg) {

        int W = cfg.getCharsPerLine();
        int itemW = W - 10;

        EscPosWriter p = new EscPosWriter().init();

        // Header
        p.alignCenter().boldOn().doubleSize()
         .text("TEMPLE NAME").lf()
         .normalSize().boldOff()
         .text("Address Line").lf().lf();

        // Items
        p.alignLeft().boldOn()
         .text(TextUtil.padRight("ITEM", itemW) + " QTY  AMT").lf()
         .boldOff();

        p.text("-".repeat(W)).lf();

        addItem(p, "Special Pooja with Long Name", 1, 500, itemW);
        addItem(p, "Archana", 2, 100, itemW);

        p.text("-".repeat(W)).lf();

        // Total
        p.alignRight().boldOn()
         .text("TOTAL : 700.00").lf().boldOff().lf();

        // QR
        p.alignCenter();
        p.text(new String(EscPosQr.qr("TXN123456"))).lf();

        // Manual tear (no cut)
        p.lf().lf().lf();

        return p.getBytes();
    }

    private static void addItem(EscPosWriter p, String name, int qty, int amt, int itemW) {
        for (String line : TextUtil.wrap(name, itemW)) {
            p.text(TextUtil.padRight(line, itemW)
                    + TextUtil.padLeft(String.valueOf(qty), 4)
                    + TextUtil.padLeft(String.valueOf(amt), 6)).lf();
        }
    }
}
