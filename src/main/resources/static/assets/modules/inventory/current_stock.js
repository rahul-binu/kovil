let tableHeaders = ["#", "Item Group", "Item Name", "Current Stock"];

$(function() {
	$("#tableLoader").removeClass("hidden");
	getCurentStock();
});

$("#searchBtn").click(getCurentStock);
function getCurentStock() {
	const dateTime = $("#dateTime").val();
	if (!dateTime) {
		showMessage("Warning", "Please select date & time", false);
		return;
	}

	fetch(`/api/inv/stock/${dateTime}`, {
		method: "GET",
		headers: {
			"Accept": "application/json",
			"Authorization": "Bearer " + localStorage.getItem("jwtToken")
		}
	})
		.then(res => {
			if (!res.ok) throw new Error("API error");
			return res.json();
		})
		.then(data => {
			mergeAllData(data);
		})
		.catch(error => {
			console.error(error);
			showMessage("Error", "Internal Server Error", false);
		});
}
function mergeAllData(data) {
	const itemMap = mapToFieldsWithKey(
		"id",
		data.item.label,
		data.item.data
	);

	const stockList = mapToFields(
		data.stock.label,
		data.stock.data
	);

	const groupedData = {};

	stockList.forEach(s => {
		const item = itemMap[s.item];
		if (!item) return;

		const group = item.itemGroup;

		if (!groupedData[group]) {
			groupedData[group] = [];
		}

		groupedData[group].push({
			...s,
			itemDetails: item
		});
	});

	$("#stockTableAppendArea").html("");

	Object.keys(groupedData).forEach(groupName => {
		const tableHtml = buildGroupedStockTable(
			groupName,
			groupedData[groupName]
		);
		$("#stockTableAppendArea").append(tableHtml);
	});

	$("#tableLoader").addClass("hidden");
}
function buildGroupedStockTable(groupName, rows) {
	let body = "";

	rows.forEach((row, index) => {
		body += `
			<tr class="border-b hover:bg-gray-50">
				<td class="px-3 py-2">${index + 1}</td>
				<td class="px-3 py-2 font-medium">${row.itemDetails.itemCode}</td>
				<td class="px-3 py-2 font-medium">${row.itemDetails.itemName}</td>
				<td class="px-3 py-2 text-right">
					${Number(row.qty).toFixed(2)} ${row.itemDetails.baseUnit}
				</td>
			</tr>
		`;
	});

	return `
		<div class="mb-8 border rounded-lg shadow-sm">
			<div class="px-4 py-2  font-semibold text-gray-700">
				${groupName}
			</div>

			<table class="app-table">
				<thead>
					<tr class="bg-gray-50 border-b">
						<th class="px-3 py-2 text-left w-12">#</th>
					<th class="px-3 py-2 text-left">Item Code</th>
						<th class="px-3 py-2 text-left">Item Name</th>
						<th class="px-3 py-2 text-right">Current Stock</th>
					</tr>
				</thead>
				<tbody>
					${body || `
						<tr>
							<td colspan="3" class="text-center py-4 text-gray-500">
								No data
							</td>
						</tr>
					`}
				</tbody>
			</table>
		</div>
	`;
}



$('#htmlPrintVendor').click(function() {
	let tablesHtml = "";
	$("#stockTableAppendArea > div").each(function() {
		const groupTitle = $(this).find("div:first").text();
		const table = $(this).find("table").clone();

		tablesHtml += `
			<h4 style="margin-top:20px">${groupTitle}</h4>
			${table.prop("outerHTML")}
		`;
	});

	if (!tablesHtml) {
		showMessage("Warning", "No data to print", false);
		return;
	}

	const header = `
		<h3 style="text-align:center; margin-bottom:10px">
			Stock Inventory
		</h3>
		<p style="text-align:center; font-size:12px">
			As on ${formatDateTime($("#dateTime").val())}
		</p>
	`;

	printTableContent(tablesHtml, header);
});
$("#excelExportVendor").click(function() {

	const dataSetExcel = [];
	let rowIndex = 1;

	$("#stockTableAppendArea > div").each(function() {

		const groupName = $(this).find("div:first").text().trim();

		dataSetExcel.push([
			groupName, "", "", ""
		]);

		$(this).find("tbody tr").each(function() {
			const cols = $(this).find("td");
			if (cols.length < 3) return;

			dataSetExcel.push([
				rowIndex++,
				$(cols[1]).text().trim(),
				groupName,
				$(cols[2]).text().trim()
			]);
		});
		dataSetExcel.push(["", "", "", ""]);
	});

	if (dataSetExcel.length === 0) {
		alert("No data to export!");
		return;
	}

	exportToExcel({
		fileName: "current_stock.xlsx",
		sheetName: "Stock",
		heading: [
			{ text: "Current Stock Report", merge: "A1:D1" },
			{ text: `As on: ${$("#dateTime").val()}`, merge: "A2:D2" }
		],
		headers: ["#", "Item Name", "Item Group", "Current Stock"],
		data: dataSetExcel,
		columnWidths: [5, 35, 25, 20]
	});
});




