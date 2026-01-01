package com.rahul.kovil.thermalPrinter;

import org.springframework.stereotype.Component;

@Component
public class PrinterConfig {

	public enum ConnectionType {
		USB, BLUETOOTH
	}

	private int charsPerLine; // 32 or 48 (dynamic)
	private ConnectionType type;

	// USB
	private String printerName;

	// Bluetooth
	private String portName; // COM5 / /dev/rfcomm0

	public int getCharsPerLine() {
		return charsPerLine;
	}

	public void setCharsPerLine(int charsPerLine) {
		this.charsPerLine = charsPerLine;
	}

	public ConnectionType getType() {
		return type;
	}

	public void setType(ConnectionType type) {
		this.type = type;
	}

	public String getPrinterName() {
		return printerName;
	}

	public void setPrinterName(String printerName) {
		this.printerName = printerName;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}
	
	
}
