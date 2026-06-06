package com.rahul.kovil.pooja.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rahul.kovil.common.response.ApiResponse;
import com.rahul.kovil.dotmatrix.DotMatrixPrintService;
import com.rahul.kovil.dotmatrix.DotMatrixPrinterConfig;
import com.rahul.kovil.dotmatrix.DotMatrixReceiptBuilder;
import com.rahul.kovil.dotmatrix.DotMatrixTextUtil;
import com.rahul.kovil.dotmatrix.RawPrinterHelper;
import javax.print.PrintServiceLookup;
import javax.print.PrintService;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import com.rahul.kovil.pooja.entity.Pooja;
import com.rahul.kovil.pooja.entity.PoojaTransaction;
import com.rahul.kovil.pooja.service.PPrintService;
import com.rahul.kovil.vendor.entity.Vendor;

@RestController
@RequestMapping("/api/print")
public class PrintController {
	
	private final PPrintService printService;
	private final DotMatrixPrintService dotMatrixPrintService;
	private final DotMatrixPrinterConfig printerConfig;
	
	@Value("${client.name:SRI KOVIL TEMPLE}")
	private String clientName;
	
	public PrintController(PPrintService printService, DotMatrixPrintService dotMatrixPrintService, DotMatrixPrinterConfig printerConfig) {
		this.printService = printService;
		this.dotMatrixPrintService = dotMatrixPrintService;
		this.printerConfig = printerConfig;
	}

	@GetMapping("/printers")
	public ResponseEntity<?> listPrinters() {
		PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
		java.util.List<String> names = new java.util.ArrayList<>();
		for (PrintService ps : services) {
			names.add(ps.getName());
		}
		return ResponseEntity.ok(names);
	}

	@GetMapping("/rawtest")
	public ResponseEntity<?> rawTest(@RequestParam("printer") String printer) {
		if (printer == null || printer.isEmpty()) {
			return ResponseEntity.badRequest().body("Provide printer query param, e.g. ?printer=EPSON LX-310");
		}
		String payload = "\u001B@RAW TEST\u000C"; // ESC @ + text + form-feed
		byte[] bytes = payload.getBytes(StandardCharsets.US_ASCII);
		boolean rawOk = false;
		try {
			rawOk = RawPrinterHelper.printBytesToPrinter(printer, bytes);
		} catch (Throwable t) {
			StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(java.util.Map.of("printer", printer, "rawOk", false, "error", sw.toString()));
		}
		return ResponseEntity.ok(java.util.Map.of("printer", printer, "rawOk", rawOk));
	}

	@GetMapping("/printerstatus")
	public ResponseEntity<?> printerStatus(@RequestParam("printer") String printer) {
		if (printer == null || printer.isEmpty()) {
			return ResponseEntity.badRequest().body("Provide printer query param, e.g. ?printer=EPSON LX-310");
		}
		try {
			return ResponseEntity.ok(RawPrinterHelper.getPrinterStatus(printer));
		} catch (Throwable t) {
			StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(java.util.Map.of("printer", printer, "error", sw.toString()));
		}
	}

	@GetMapping("/texttest")
	public ResponseEntity<?> textTest(@RequestParam("printer") String printer) {
		if (printer == null || printer.isEmpty()) {
			return ResponseEntity.badRequest().body("Provide printer query param, e.g. ?printer=EPSON LX-310");
		}
		String payload = "TEXT MODE TEST\r\nLine 1\r\nLine 2\r\n\f";
		boolean ok = dotMatrixPrintService.printWithPrinterJob(printer, payload);
		System.out.print(ok);
		if (!ok) {
			ok = dotMatrixPrintService.print(printer, payload);
		}
		return ResponseEntity.ok(java.util.Map.of("printer", printer, "textOk", ok));
	}

	@GetMapping("/flavors")
	public ResponseEntity<?> supportedFlavors(@RequestParam("printer") String printer) {
		if (printer == null || printer.isEmpty()) {
			return ResponseEntity.badRequest().body("Provide printer query param, e.g. ?printer=EPSON LX-310");
		}
		return ResponseEntity.ok(java.util.Map.of("printer", printer, "flavors", dotMatrixPrintService.getSupportedDocFlavors(printer)));
	}

	@GetMapping("/pooja/{tids}")
	public ResponseEntity<?> poojaRreceiptPrint(@PathVariable String tids, @RequestParam(value = "printer", required = false) String printer){
		// If a printer is provided by the UI or a default printer is configured,
		// forward the request to the dot-matrix printing flow so the UI can trigger printing.
		String targetPrinter = (printer != null && !printer.isEmpty()) ? printer : printerConfig.getPrinterName();
		if (targetPrinter != null && !targetPrinter.isEmpty()) {
			return dotMatrixPrint(tids, targetPrinter);
		}

		if (tids != null && tids.contains(",")) {
			String[] splitTids = tids.split(",");
			java.util.List<Object> results = new java.util.ArrayList<>();
			for (String tid : splitTids) {
				results.add(printService.poojaRreceiptPrint(tid.trim()));
			}
			return ResponseEntity.ok(results);
		}
		return ResponseEntity.ok(printService.poojaRreceiptPrint(tids));
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping("/dotmatrix/pooja/{tids}")
	public ResponseEntity<?> dotMatrixPrint(@PathVariable String tids, @RequestParam(value = "printer", required = false) String printer) {
		String[] splitTids = tids != null ? tids.split(",") : new String[0];
		if (splitTids.length == 0 && tids != null && !tids.isEmpty()) {
			splitTids = new String[] { tids };
		}
		String targetPrinter = (printer != null && !printer.isEmpty()) ? printer : printerConfig.getPrinterName();
		for (String tid : splitTids) {
			Map<String, Object> data = printService.poojaRreceiptPrint(tid.trim());
			Pooja pooja = (Pooja) data.get("pooja");
			List<PoojaTransaction> poojat = (List<PoojaTransaction>) data.get("poojat");
			Vendor vendor = (Vendor) data.get("vendor");
			
			if (pooja != null && poojat != null) {
				int receiptWidth = 40;
				DotMatrixReceiptBuilder builder = new DotMatrixReceiptBuilder(printerConfig);
				
				builder.addRow(DotMatrixTextUtil.padLeft(clientName, receiptWidth / 2 + (clientName.length() / 2)));
				builder.addRow(DotMatrixTextUtil.padLeft("Official Receipt", receiptWidth / 2 + 8));
				builder.addDivider('-', receiptWidth);
				
				String dateStr = pooja.getDate() != null ? pooja.getDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : "";
				builder.addLeftRightRow("Date:", dateStr, receiptWidth);
				
				String vendorName = vendor != null ? vendor.getFullName() : "";
				String nakshathra = vendor != null && vendor.getNakshathra() != null ? vendor.getNakshathra().name() : "";
				if (!nakshathra.isEmpty()) {
					vendorName += " (" + nakshathra + ")";
				}
				
				builder.addLeftRightRow("Name:", vendorName, receiptWidth);
				builder.addDivider('-', receiptWidth);
				
				for (PoojaTransaction pt : poojat) {
					String pName = pt.getPoojaMaster() != null ? pt.getPoojaMaster().getName() : "";
					if (pName.length() > receiptWidth) {
						builder.addRow(pName.substring(0, receiptWidth));
					} else {
						builder.addRow(pName);
					}
					String amountStr = pt.getAmount() != null ? pt.getAmount().toString() : "0.00";
					String rcptNo = pt.getReceiptNo() != null ? pt.getPrefix() + pt.getReceiptNo() : "";
					builder.addLeftRightRow("  Rcpt: " + rcptNo, amountStr, receiptWidth);
				}
				
				builder.addDivider('-', receiptWidth);
				builder.addLeftRightRow("TOTAL:", pooja.getAmount() != null ? pooja.getAmount().toString() : "0.00", receiptWidth);
				builder.addDivider('=', receiptWidth);
				
				builder.addRow(DotMatrixTextUtil.padLeft("Thank You!", receiptWidth / 2 + 5));
				builder.addBlankLines(4);
				boolean printOk = dotMatrixPrintService.printWithPrinterJob(targetPrinter, builder.build());
				if (!printOk) {
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(ApiResponse.builder().message("Failed to send receipt to printer: " + targetPrinter).status(HttpStatus.INTERNAL_SERVER_ERROR).build());
				}
			}
		}
		return ResponseEntity.ok(ApiResponse.builder().message("Sent to dot matrix printer successfully").status(HttpStatus.OK).build());
	}
}
