package com.rahul.kovil.pooja.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import com.rahul.kovil.dotmatrix.RawPrinterHelper;
import javax.print.PrintServiceLookup;
import javax.print.PrintService;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

	public PrintController(PPrintService printService, DotMatrixPrintService dotMatrixPrintService,
			DotMatrixPrinterConfig printerConfig) {
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
		return ResponseEntity.ok(
				java.util.Map.of("printer", printer, "flavors", dotMatrixPrintService.getSupportedDocFlavors(printer)));
	}

	@GetMapping("/pooja/{tids}")
	public ResponseEntity<?> poojaRreceiptPrint(@PathVariable String tids,
			@RequestParam(value = "printer", required = false) String printer) {
		// If a printer is provided by the UI or a default printer is configured,
		// forward the request to the dot-matrix printing flow so the UI can trigger
		// printing.
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
	public ResponseEntity<?> dotMatrixPrint(@PathVariable String tids,
			@RequestParam(value = "printer", required = false) String printer) {

		// Split incoming ids (comma separated)
		String[] splitTids = (tids != null && !tids.isEmpty()) ? tids.split(",") : new String[0];

		// Aggregate data across all transaction ids
		List<Pooja> aggregatedPooja = null;
		List<Vendor> aggregatedVendor = null;
		List<PoojaTransaction> aggregatedTransactions = new ArrayList<>();

		BigDecimal totalPaidAmount = BigDecimal.ZERO;

		for (String tid : splitTids) {
			Map<String, Object> data = printService.poojaRreceiptDotPrint(tid.trim());
			List<Pooja> pooja = (List<Pooja>) data.get("poojas");
			List<PoojaTransaction> poojat = (List<PoojaTransaction>) data.get("poojat");
			List<Vendor> vendors = (List<Vendor>) data.get("vendors");

			if (pooja != null && !pooja.isEmpty()) {
				if (aggregatedPooja == null) {
					totalPaidAmount = pooja.get(0).getPaidAmount();
					aggregatedPooja = pooja;
					aggregatedVendor = vendors;
				}
				if (poojat != null) {
					aggregatedTransactions.addAll(poojat);
				}
			}
		}

		// Guard: nothing fetched
		if (aggregatedPooja == null || aggregatedPooja.isEmpty()) {
			return ResponseEntity.badRequest()
					.body(ApiResponse.builder()
							.message("No valid pooja data found for provided ids")
							.status(HttpStatus.BAD_REQUEST)
							.build());
		}
		if (aggregatedVendor == null) {
			aggregatedVendor = new ArrayList<>();
		}
		// ----------------------------------------------------------------
		// Build receipt — positions from pre-printed paper (cm → row/col)
		// Origin: TOP-LEFT. Y increases downward in feed direction.
		// Conversion: row = round(Y_cm × 2.362) [6 LPI]
		// col = round(X_cm × 3.937) [10 CPI]
		// Paper: Length=15.5cm (feed direction), Width=10cm (horizontal)
		// ----------------------------------------------------------------
		DotMatrixReceiptBuilder builder = new DotMatrixReceiptBuilder(printerConfig);

		// --- Receipt No (NO): Y=3.8cm, X=11.0cm → row=9, col=43 ---
		String receiptNo = "";
		if (!aggregatedTransactions.isEmpty()) {
			var tx = aggregatedTransactions.get(0);
			String prefix = tx.getPrefix();
			Long receipt = tx.getReceiptNo();
			receiptNo = (prefix != null ? prefix : "")
					.replace("@N@", receipt != null ? receipt + "" : "");
		}
		builder.addFieldAt(receiptNo, 1, 30); // NO: Y=3.8cm

		// --- Date: Y=4.4cm, X=11.0cm → row=10, col=43 ---
		String dateStr = aggregatedPooja.get(0).getDate() != null
				? aggregatedPooja.get(0).getDate().format(DateTimeFormatter.ofPattern("dd-MM-yy"))
				: "";
		builder.addFieldAt(dateStr, 2, 30); // Date: Y=4.4cm

		// --- Pooja Name (Vazhipad): Y=4.4cm, X=4.5cm → row=10, col=18 ---
		String poojaName = "";
		if (!aggregatedTransactions.isEmpty() && aggregatedTransactions.get(0).getPoojaMaster() != null) {
			poojaName = aggregatedTransactions.get(0).getPoojaMaster().getName() != null
					? aggregatedTransactions.get(0).getPoojaMaster().getName()
					: "";
		}
		builder.addFieldAt("Sastha", 1, 3);
		builder.addFieldAt(poojaName, 2, 3); // Vazhipad: Y=4.4cm

		// --- Devotee details (multiple devotees) ---
		Map<String, BigDecimal> vendorAmtMap = new HashMap<>();
		for (PoojaTransaction t : aggregatedTransactions) {
			if (t.getVendorId() == null)
				continue;
			vendorAmtMap.put(
					t.getVendorId(),
					t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO);
		}

		List<String[]> vendorRows = new ArrayList<>();
		for (Vendor v : aggregatedVendor) {
			String name = v.getFullName() != null ? v.getFullName() : "";
			String star = v.getNakshathra() != null ? v.getNakshathra().name() : "";
			String amt = vendorAmtMap.getOrDefault(
					v.getTransId(),
					BigDecimal.ZERO).toPlainString();
			vendorRows.add(new String[] { name, star, amt });
		}

		// Name: col=6, Star: col=31, Amount: col=49, BaseRow=13, Spacing=2
		builder.addVendorRows(0, 23, 45, 4, 1, vendorRows);

		// --- Total Amount: Y=8.0cm, X=12.5cm → row=19, col=49 ---
		// BigDecimal total = aggregatedTransactions.stream()
		// .map(PoojaTransaction::getAmount)
		// .filter(Objects::nonNull)
		// .reduce(BigDecimal.ZERO, BigDecimal::add);
		builder.addFieldAt(totalPaidAmount.toPlainString(), 10, 38);

		String receipt = builder.build();
		System.out.println(receipt);

		Path path = Paths.get(tids + ".txt");

		try {
			Files.write(path, receipt.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String targetPrinter = (printer != null && !printer.isEmpty())
				? printer
				: printerConfig.getPrinterName();

		boolean printOk = dotMatrixPrintService.printWithPrinterJob(targetPrinter, receipt);
		if (!printOk) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.builder()
							.message("Failed to send receipt to printer: " + targetPrinter)
							.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.build());
		}

		return ResponseEntity.ok(ApiResponse.builder()
				.message("Sent to dot matrix printer successfully")
				.status(HttpStatus.OK)
				.build());
	}
}
