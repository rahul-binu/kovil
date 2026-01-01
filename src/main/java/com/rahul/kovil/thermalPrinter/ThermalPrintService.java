package com.rahul.kovil.thermalPrinter;

import org.springframework.stereotype.Service;

@Service
public class ThermalPrintService {

    public void printReceipt(PrinterConfig cfg) throws Exception {

        byte[] receipt = ReceiptBuilder.build(cfg);
        
        
        
        

        PrinterTransport transport =
                cfg.getType() == PrinterConfig.ConnectionType.USB
                        ? new UsbPrinterTransport(cfg.getPrinterName())
                        : new BluetoothPrinterTransport(cfg.getPortName());

        transport.print(receipt);
    }
}
