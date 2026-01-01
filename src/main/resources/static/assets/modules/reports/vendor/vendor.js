let tableHeader = ["#", "Devotee Name", "Phone Number", "Family Name", "Address", "Nakshatram", "Dreated Date"];
let finalresult = [];
$(function() {
	fetchVendorDetails();
	$("#vendorTypeLabel").text($("#vendorType").val().charAt(0).toUpperCase() + $("#vendorType").val().substring(1));
});

$("#searchVendors").click(function() {
	$("#tableLoader").removeClass("hidden");
	fetchVendorDetails();
})

function fetchVendorDetails() {
	let fm = $("#fromDate").val();
	let to = $("#toDate").val();
	fetch("/api/report/s-vendor/devotee", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			"Authorization": "Bearer " + localStorage.getItem("jwtToken")
		},
		body: JSON.stringify({
			from: fm,
			to: to
		})
	})

		.then(res => res.json())
		.then(data => mergeData(data))
		.catch(err => $("#vendorTable tbody").html(emptyTableBody(8)));
}
function mergeData(data) {
	let vendor = mapToFields(data.vendor.label, data.vendor.data);
	getTableText(vendor);
}

function getTableText(data) {

	$("#tableLoader").addClass("hidden");

	$("#vendorTable thead").html(getTableHeader(tableHeader));

	if (!Array.isArray(data) || data.length === 0) {
		$("#vendorTable tbody").html(emptyTableBody(8));
		return;
	}
	finalresult = data;
	$("#pagination").pagination({
		dataSource: data,
		pageSize: 11,
		callback: function(pageData, pagination) {
			let startIndex = (pagination.pageNumber - 1) * pagination.pageSize;
			$("#vendorTable tbody").html(buildTableBodyText(pageData, startIndex));
		}
	});
}


function buildTableBodyText(data, startIndex = 0) {
	let txt = '';
	data.forEach((e, i) => {
		txt += `
			<tr>
				<td>${startIndex + i + 1}</td>
				<td>${e.vn}</td>
				<td></td>
				<td>${e.mob}</td>
				<td>${e.fnm}</td>
				<td>${e.add}</td>
				<td>${e.nak}</td>
				<td>${formatDateTime(e.cdt)}</td>
			</tr>
		`;
	});

	return txt;
}

$('#htmlPrintVendor').click(function() {
	let tbltxt = `
        <table>
            <thead>${$('#vendorTable thead').html()}</thead>
            <tbody>${buildTableBodyText(finalresult, 1)}</tbody>
        </table>
    `;
	let header = `
        <h3 style="text-align:center">${$("#vendorTypeLabel").text()} Report</h3>
        <h4 style="text-align:center">
            From: ${formatDate($('#fromDate').val())}
            to ${formatDate($('#toDate').val())}
        </h4>
    `;
	printTableContent(tbltxt, header);
});



$("#excelExportVendor").click(function() {
	if (!finalresult || finalresult.length === 0) {
		alert("No data to export!");
		return;
	}

	let dataSetExcel = finalresult.map((e, index) => [
		index + 1,
		e.vn,
		e.mob,
		e.fnm,
		e.add,
		e.nak,
		formatDateTime(e.cdt)
	]);


	const colWidths = [5, 20, 30, 20, 20, 30, 25, 25, 30, 35, 60];

	exportToExcel({
		fileName: "vendor.xlsx",
		sheetName: $("#vendorTypeLabel").text(),
		heading: [
			{ text: $("#vendorTypeLabel").text() + " Report" + $("#fromDate").val() + " - " + $("#toDate").val(), merge: "A1:G1" }
		],
		headers: [
			"#",
			"Devotee Name",
			"Phone Number",
			"Family Name",
			"Address",
			"Nakshatram",
			"Dreated Date"
		],
		data: dataSetExcel,
		columnWidths: colWidths
	});
});



