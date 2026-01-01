package com.rahul.kovil.thermalPrinter;

import java.io.ByteArrayOutputStream;

public class EscPosWriter {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final int charsPerLine=32;
    

    public byte[] getBytes() {
        return out.toByteArray();
    }

    public EscPosWriter init() {
        write(0x1B, 0x40); // ESC @
        return this;
    }

    public EscPosWriter alignLeft()   { write(0x1B, 0x61, 0x00); return this; }
    public EscPosWriter alignCenter() { write(0x1B, 0x61, 0x01); return this; }
    public EscPosWriter alignRight()  { write(0x1B, 0x61, 0x02); return this; }

    public EscPosWriter boldOn()  { write(0x1B, 0x45, 0x01); return this; }
    public EscPosWriter boldOff() { write(0x1B, 0x45, 0x00); return this; }

    public EscPosWriter normalSize() { write(0x1D, 0x21, 0x00); return this; }
    public EscPosWriter doubleSize() { write(0x1D, 0x21, 0x11); return this; }

    public EscPosWriter text(String s) {

        // Split by newline to protect layout
        String[] lines = s.split("\n", -1);

        try {
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];

                // CUT from right if exceeds printer width
                if (charsPerLine > 0 && line.length() > charsPerLine) {
                    line = line.substring(0, charsPerLine);
                }

                out.write(line.getBytes("UTF-8"));

                // Re-add newline if it existed
                if (i < lines.length - 1) {
                    out.write('\n');
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this;
    }


    public EscPosWriter lf() {
        write(0x0A);
        return this;
    }

    private void write(int... bytes) {
        for (int b : bytes) out.write(b);
    }
}
