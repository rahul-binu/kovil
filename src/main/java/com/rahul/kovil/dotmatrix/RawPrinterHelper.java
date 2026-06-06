package com.rahul.kovil.dotmatrix;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.platform.win32.Kernel32Util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RawPrinterHelper {

    private interface Winspool extends StdCallLibrary {
        Winspool INSTANCE = Native.load("winspool.drv", Winspool.class, W32APIOptions.UNICODE_OPTIONS);

        boolean OpenPrinter(String pPrinterName, PointerByReference phPrinter, Pointer pDefault);
        boolean ClosePrinter(Pointer hPrinter);
        boolean StartDocPrinter(Pointer hPrinter, int level, DOC_INFO_1 pDocInfo);
        boolean EndDocPrinter(Pointer hPrinter);
        boolean StartPagePrinter(Pointer hPrinter);
        boolean EndPagePrinter(Pointer hPrinter);
        boolean WritePrinter(Pointer hPrinter, Pointer pBuffer, int cbBuf, IntByReference pcWritten);
        boolean GetPrinter(Pointer hPrinter, int level, Pointer pPrinter, int cbBuf, IntByReference pcbNeeded);
    }

    public static class DOC_INFO_1 extends Structure {
        public WString pDocName;
        public WString pOutputFile;
        public WString pDatatype;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("pDocName", "pOutputFile", "pDatatype");
        }
    }

    public static class PRINTER_INFO_2 extends Structure {
        public WString pServerName;
        public WString pPrinterName;
        public WString pShareName;
        public WString pPortName;
        public WString pDriverName;
        public WString pComment;
        public WString pLocation;
        public Pointer pDevMode;
        public WString pSepFile;
        public WString pPrintProcessor;
        public WString pDatatype;
        public WString pParameters;
        public Pointer pSecurityDescriptor;
        public int Attributes;
        public int Priority;
        public int DefaultPriority;
        public int StartTime;
        public int UntilTime;
        public int Status;
        public int cJobs;
        public int AveragePPM;

        public PRINTER_INFO_2() {
        }

        public PRINTER_INFO_2(Pointer p) {
            super(p);
            read();
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                    "pServerName",
                    "pPrinterName",
                    "pShareName",
                    "pPortName",
                    "pDriverName",
                    "pComment",
                    "pLocation",
                    "pDevMode",
                    "pSepFile",
                    "pPrintProcessor",
                    "pDatatype",
                    "pParameters",
                    "pSecurityDescriptor",
                    "Attributes",
                    "Priority",
                    "DefaultPriority",
                    "StartTime",
                    "UntilTime",
                    "Status",
                    "cJobs",
                    "AveragePPM"
            );
        }
    }

    public static Map<String, Object> getPrinterStatus(String printerName) {
        PointerByReference phPrinter = new PointerByReference();
        if (!Winspool.INSTANCE.OpenPrinter(printerName, phPrinter, null)) {
            throw new RuntimeException("OpenPrinter failed: " + getLastErrorMessage());
        }
        Pointer hPrinter = phPrinter.getValue();
        try {
            IntByReference needed = new IntByReference();
            Winspool.INSTANCE.GetPrinter(hPrinter, 2, Pointer.NULL, 0, needed);
            int size = needed.getValue();
            if (size <= 0) {
                throw new RuntimeException("GetPrinter failed to return buffer size");
            }
            Memory buffer = new Memory(size);
            if (!Winspool.INSTANCE.GetPrinter(hPrinter, 2, buffer, size, needed)) {
                throw new RuntimeException("GetPrinter failed: " + getLastErrorMessage());
            }
            PRINTER_INFO_2 info = new PRINTER_INFO_2(buffer);
            return Map.of(
                    "printerName", info.pPrinterName == null ? null : info.pPrinterName.toString(),
                    "portName", info.pPortName == null ? null : info.pPortName.toString(),
                    "driverName", info.pDriverName == null ? null : info.pDriverName.toString(),
                    "datatype", info.pDatatype == null ? null : info.pDatatype.toString(),
                    "status", info.Status,
                    "jobs", info.cJobs,
                    "attributes", info.Attributes
            );
        } finally {
            Winspool.INSTANCE.ClosePrinter(hPrinter);
        }
    }

    public static boolean printBytesToPrinter(String printerName, byte[] data) {
        PointerByReference phPrinter = new PointerByReference();
        if (!Winspool.INSTANCE.OpenPrinter(printerName, phPrinter, null)) {
            throw new RuntimeException("OpenPrinter failed: " + getLastErrorMessage());
        }
        Pointer hPrinter = phPrinter.getValue();

        DOC_INFO_1 di = new DOC_INFO_1();
        di.pDocName = new WString("JavaRawPrint");
        di.pOutputFile = null;
        di.pDatatype = new WString("RAW");
        di.write();

        try {
            if (!Winspool.INSTANCE.StartDocPrinter(hPrinter, 1, di)) {
                throw new RuntimeException("StartDocPrinter failed: " + getLastErrorMessage());
            }
            if (!Winspool.INSTANCE.StartPagePrinter(hPrinter)) {
                throw new RuntimeException("StartPagePrinter failed: " + getLastErrorMessage());
            }

            Memory p = new Memory(data.length);
            p.write(0, data, 0, data.length);
            IntByReference written = new IntByReference();
            boolean writtenResult = Winspool.INSTANCE.WritePrinter(hPrinter, p, data.length, written);
            if (!writtenResult) {
                throw new RuntimeException("WritePrinter failed: " + getLastErrorMessage());
            }
            if (written.getValue() != data.length) {
                throw new RuntimeException("WritePrinter wrote " + written.getValue() + " bytes of " + data.length);
            }

            if (!Winspool.INSTANCE.EndPagePrinter(hPrinter)) {
                throw new RuntimeException("EndPagePrinter failed: " + getLastErrorMessage());
            }
            if (!Winspool.INSTANCE.EndDocPrinter(hPrinter)) {
                throw new RuntimeException("EndDocPrinter failed: " + getLastErrorMessage());
            }
            return true;
        } finally {
            Winspool.INSTANCE.ClosePrinter(hPrinter);
        }
    }

    private static String getLastErrorMessage() {
        return Kernel32Util.formatMessage(Native.getLastError());
    }
}
