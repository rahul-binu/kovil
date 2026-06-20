const itemsOnPage = 14;
let poojaTransData = [];
let filteredPoojaTransData = [];

/* ----------------------------------------------------
   FETCH REPORT DATA
---------------------------------------------------- */
$("#serchPooja").click(() => getPoojaData());

function getPoojaData() {
	let sdata = {
		"fields": {
			"pt": ["pt.transId", "pt.createdAt", "pt.amount", "pt.poojaMaster", "pt.receiptNo"],
			"pj": ["pj.devotee", "pj.user", "pj.transId", "pj.date", "pj.amount", "pj.bookingDate", "pj.bookingCloseDate"],
			"cs": ["cs.transId", "cs.fullName", "cs.mobile", "cs.familyName", "cs.address", "cs.nakshathra"]
		},
		"filters": {
			"pj": {
				"pj.date >=": $("#fromDate").val().toString(),
				"pj.date <=": $("#toDate").val().toString(),
				"pj.bookingStatus <>":"ACTIVE"
			}
		},
		"nameFields": {
			"pt": ["pttransId", "ptcreated", "ptamount", "ptmaster", "ptreciptno"],
			"pj": ["pjdevotee", "pjuser", "pjtransId", "pjdate", "pjamount", "pjbookingDate", "pjbookingCloseDate"],
			"cs": ["cstransId", "csname", "csmobile", "csfamilyName", "csaddress", "csnakshathra"]
		}
	};

	fetch("/api/report/s-pooja", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		},
		body: JSON.stringify(sdata)
	})
		.then(res => res.json())
		.then(data => mergeData(data))
		.catch(err => emptyPoojaTable(err));
}

/* ----------------------------------------------------
   MERGING LOGIC
---------------------------------------------------- */
function mergeData(data) {
	let vendorMap = mapToFieldsWithKey("cstransId", data.cs.label, data.cs.data);
	let poojaList = mapToFields(data.pj.label, data.pj.data);
	let transList = mapToFields(data.pt.label, data.pt.data);

	let finalMerged = [];
	let grouped = {};

	transList.forEach(pt => {
		const tid = pt["pttransId"];

		let pj = poojaList.find(x => x["pjtransId"] === tid) || {};
		let cs = vendorMap[pj["pjdevotee"]] || {};

		let poojaName = pt["ptmaster"] ? pt["ptmaster"].name : "Unknown";
		let dateVal = pj["pjdate"] ? pj["pjdate"].substring(0, 10) : "Unknown";
		let key = poojaName + "_" + dateVal;

		let amt = toSafeNumber(pt["ptamount"] == 0 ? pj["pjamount"] : pt["ptamount"]);
		let devoteeName = cs["csname"] || "";

		if (!grouped[key]) {
			grouped[key] = {
				poojaName: poojaName,
				date: dateVal,
				count: 0,
				totalAmount: 0,
				devotees: []
			};
		}
		grouped[key].count += 1;
		grouped[key].totalAmount += amt;
		if (devoteeName) grouped[key].devotees.push(devoteeName);
	});

	for (let k in grouped) {
		finalMerged.push({
			poojaName: grouped[k].poojaName,
			date: grouped[k].date,
			count: grouped[k].count,
			totalAmount: grouped[k].totalAmount,
			devotees: grouped[k].devotees.join(", ")
		});
	}

	poojaTransData = finalMerged;
	filteredPoojaTransData = poojaTransData;
	applyPagination();
}

/* ----------------------------------------------------
   BUILD TABLE
---------------------------------------------------- */
function buildPoojaTransactionTable(data, start, limit) {
	let thead = `
    <tr>
        <th>#</th>
		<th>Date</th>
        <th>Pooja Name</th>
        <th>Count</th>
        <th>Total Amount</th>
        <th>Devotees</th>
    </tr>`;
	$("#poojaReportTable thead").html(thead);

	const tbody = $("#poojaTableBody");
	tbody.empty();

	const end = Math.min(start + limit, data.length);
	
	for (let i = start; i < end; i++) {
		const item = data[i];

		tbody.append(`
            <tr>
                <td>${i + 1}</td>
				<td>${formatDate(item.date)}</td>
                <td>${item.poojaName}</td>
                <td>${item.count}</td>
                <td style="text-align:right;">${item.totalAmount.toFixed(2)}</td>
                <td style="white-space: normal; max-width: 300px; line-height: 1.4;">${item.devotees}</td>
            </tr>
        `);
	}
	// ---------- SET FOOTER ----------
	let totalAmount = filteredPoojaTransData.reduce((sum, item) => sum + item.totalAmount, 0);
	const tfoot = `
	    <tr>
	        <th colspan="4" style="text-align:right;">Total</th>
	        <th style="text-align:right;">${totalAmount.toFixed(2)}</th>
	        <th></th>
	    </tr>
	`;

	$("#poojaReportTable tfoot").html(tfoot);
}

/* ----------------------------------------------------
   PAGINATION
---------------------------------------------------- */
function applyPagination() {
	$("#pager").pagination({
		items: filteredPoojaTransData.length,
		itemsOnPage: itemsOnPage,
		cssStyle: 'light-theme',
		onPageClick: function(pageNumber) {
			const start = (pageNumber - 1) * itemsOnPage;
			buildPoojaTransactionTable(filteredPoojaTransData, start, itemsOnPage);
		}
	});

	buildPoojaTransactionTable(filteredPoojaTransData, 0, itemsOnPage);
}

/* ----------------------------------------------------
   INIT
---------------------------------------------------- */
getPoojaData();

function emptyPoojaTable() {
	$("#poojaReportTable thead").html(`
	            <tr>
	                <th>#</th>
					<th>Date</th>
	                <th>Pooja Name</th>
	                <th>Count</th>
	                <th>Total Amount</th>
	                <th>Devotees</th>
	            </tr>
	        `);

	$("#poojaTableBody").html(`
	            <tr>
	                <td colspan="6" class="no-data">
					<div class="flex flex-col items-center justify-center text-center py-10 opacity-75">
					    <img src="https://cdn-icons-png.flaticon.com/512/7465/7465722.png" 
					         class="w-14 h-14 mb-3 opacity-50" />
					    <p class="text-gray-600 font-medium">No records found</p>
					</div>
	                </td>
	            </tr>
	        `);

	$("#poojaReportTable tfoot").html("");
	return;
}

/* ----------------------------------------------------
   GLOBAL SEARCH — FILTER + REBUILD
---------------------------------------------------- */
$("#allSearchBox").on("input", function () {
	let searchTerm = $(this).val().toLowerCase().trim();

	if (searchTerm === "") {
		applyPagination();
		return;
	}

	filteredPoojaTransData = poojaTransData.filter(row => {
		return Object.values(row).some(v => {
			if (!v) return false;
			if (typeof v === "object") {
				return Object.values(v).some(objVal =>
					String(objVal ?? "").toLowerCase().includes(searchTerm)
				);
			}

			return String(v).toLowerCase().includes(searchTerm);
		});
	});

	buildPoojaTransactionTable(filteredPoojaTransData, 0, filteredPoojaTransData.length);


});

$("#excelExportPooja").click(function() {
	if (!filteredPoojaTransData || filteredPoojaTransData.length === 0) {
		alert("No data to export!");
		return;
	}

	let poojaDataSetExcel = filteredPoojaTransData.map((item, index) => [
		index + 1,               
		formatDate(item.date),   		       
		item.poojaName,           
		item.count,           
		item.totalAmount.toFixed(2), 
		item.devotees          
	]);

	let totalAmount = filteredPoojaTransData.reduce((sum, item) => sum + item.totalAmount, 0);

	poojaDataSetExcel.push(["", "", "", "Total", totalAmount.toFixed(2), ""]);

	const colWidths = [5, 20, 30, 10, 20, 60];

	exportToExcel({
		fileName: "grouped_vazhipad.xlsx",
		sheetName: "Grouped_Vazhipad",
		heading: [
			{ text: "Grouped Pooja Report " + $("#fromDate").val() + " - " + $("#toDate").val(), merge: "A1:F1" }
		],
		headers: [
			"#",
			"Date",
			"Pooja Name",
			"Count",
			"Total Amount",
			"Devotees"
		],
		data: poojaDataSetExcel,
		columnWidths: colWidths
	});
});

function printPoojaTransaction(id){
	$("#printFrame").attr("src", `/web/pooja/receipt/0/${id}`);
	$("#printModal").removeClass("hidden");
}

function printIframe() {
    const iframe = document.getElementById("printFrame");
    const iframeWindow = iframe.contentWindow;
    iframeWindow.focus();
    iframeWindow.onafterprint = () => {
        iframe.src = iframe.src;
    };
    iframeWindow.print();
}

function closeModal() {
	document.getElementById("printModal").classList.add("hidden");
	document.getElementById("printFrame").src = "";
	location.reload();
}







	
	
	
	