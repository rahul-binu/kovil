package com.rahul.kovil.thermalPrinter;

import java.io.FileOutputStream;

public class BluetoothPrinterTransport implements PrinterTransport {

    private final String port;

    public BluetoothPrinterTransport(String port) {
        this.port = port;
    }

    @Override
    public void print(byte[] data) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(port)) {
            fos.write(data);
            fos.flush();
        }
    }
}
