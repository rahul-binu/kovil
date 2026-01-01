package com.rahul.kovil.thermalPrinter;

import java.io.ByteArrayOutputStream;

public class EscPosQr {

    public static byte[] qr(String data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(new byte[]{0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00});
            byte[] d = data.getBytes("UTF-8");
            int len = d.length + 3;
            out.write(new byte[]{0x1D, 0x28, 0x6B, (byte) (len % 256), (byte) (len / 256), 0x31, 0x50, 0x30});
            out.write(d);
            out.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }
}
