/**
 * Export data to Excel using ExcelJS
 * @param {Object} options
 * @param {string} options.fileName - The Excel file name (with .xlsx)
 * @param {string} options.sheetName - The worksheet name
 * @param {Array} options.heading - Array of objects for heading rows [{text: 'Pooja Master', merge: 'A1:E1'}]
 * @param {Array} options.headers - Array of strings for header row (bold)
 * @param {Array} options.data - Array of arrays for table body [[row1], [row2], ...]
 * @param {Array} options.columnWidths - Optional array of column widths
 */
async function exportToExcel({ fileName = "file.xlsx", sheetName = "Sheet1", heading = [], headers = [], data = [], columnWidths = [] }) {
	const workbook = new ExcelJS.Workbook();
	const worksheet = workbook.addWorksheet(sheetName);

	addHeadings(worksheet, heading);
	addHeaderRow(worksheet, headers);
	addBodyRows(worksheet, data);
	adjustColumnWidths(worksheet, columnWidths);

	await saveWorkbook(workbook, fileName);
}

/* ------------------------- Sub-functions ------------------------- */

/** Add heading rows with merge, font, and alignment */
function addHeadings(worksheet, heading) {
	heading.forEach(h => {
		const row = worksheet.addRow([h.text]);
		if (h.merge) worksheet.mergeCells(h.merge);
		row.font = { size: 14, bold: true };
		row.alignment = { horizontal: "center" };
	});
}

/** Add the header row with styling */
function addHeaderRow(worksheet, headers) {
	if (headers.length === 0) return;

	const headerRow = worksheet.addRow(headers);
	headerRow.font = { bold: true };
	headerRow.alignment = { horizontal: "center" };

	headerRow.eachCell(cell => {
		cell.fill = {
			type: "pattern",
			pattern: "solid",
			fgColor: { argb: "FFDCE6F1" } // light blue
		};
		cell.border = createThinBorder();
	});
}

/** Add body rows with border and alignment */
function addBodyRows(worksheet, data) {
	data.forEach(rowData => {
		const row = worksheet.addRow(rowData);
		row.alignment = { horizontal: "center" };
		row.eachCell((cell, colNumber) => {
			cell.border = createThinBorder();
			// Align numeric column (Amount) right
			if (colNumber === 4) {
				cell.alignment = { horizontal: "right" };
			}
		});
	});
}

/** Create a thin border object for reuse */
function createThinBorder() {
	return {
		top: { style: "thin" },
		left: { style: "thin" },
		bottom: { style: "thin" },
		right: { style: "thin" }
	};
}

/** Adjust column widths */
function adjustColumnWidths(worksheet, columnWidths) {
	if (columnWidths && columnWidths.length > 0) {
		worksheet.columns.forEach((col, i) => {
			col.width = columnWidths[i] || 15;
		});
	} else {
		worksheet.columns.forEach(column => {
			let maxLength = 10;
			column.eachCell({ includeEmpty: true }, cell => {
				const val = cell.value ? cell.value.toString() : "";
				const length = val.length + val.split('\n').length - 1;
				if (length > maxLength) maxLength = length;
			});
			column.width = maxLength * 1.1;
		});
	}
}

/** Save workbook as downloadable file */
async function saveWorkbook(workbook, fileName) {
	const buffer = await workbook.xlsx.writeBuffer();
	const blob = new Blob([buffer], { type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" });
	const link = document.createElement("a");
	link.href = URL.createObjectURL(blob);
	link.download = fileName;
	link.click();
}