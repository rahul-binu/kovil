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

	let poojaGrouped = {};
	poojaList.forEach(pj => {
		let tid = pj["pjtransId"];
		if (!poojaGrouped[tid]) poojaGrouped[tid] = [];
		poojaGrouped[tid].push(pj);
	});

	let transGrouped = {};
	transList.forEach(pt => {
		let tid = pt["pttransId"];
		if (!transGrouped[tid]) transGrouped[tid] = [];
		transGrouped[tid].push(pt);
	});

	let finalMerged = [];

	Object.keys(poojaGrouped).forEach(tid => {
		let pjs = poojaGrouped[tid] || [];
		let pts = transGrouped[tid] || [];

		let maxLen = Math.max(pjs.length, pts.length);

		for (let i = 0; i < maxLen; i++) {
			// Zip the records. If one array is shorter, fall back to its first element
			let pj = pjs[i] || pjs[0] || {};
			let pt = pts[i] || pts[0] || {};

			let cs = vendorMap[pj["pjdevotee"]] || {};

			finalMerged.push({
				...pt,
				...pj,
				...cs
			});
		}
	});

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
		<th>Booking Date</th>
		<th>Booking Close Date</th>
        <th>Pooja</th>
        <th>Amount</th>
        <th>Receipt No</th>
        <th>Devotee</th>
        <th>Mobile</th>
		<th>Nakshatra</th>
		<th>Family Name</th>
		<th>Address</th>
		<th>Description</th>
    </tr>`;
	$("#poojaReportTable thead").html(thead);

	const tbody = $("#poojaTableBody");
	tbody.empty();

	const end = Math.min(start + limit, data.length);
	
	for (let i = start; i < end; i++) {
		const item = data[i];
		/*let action = actionIcons(item['pjtransId'], "PoojaTransaction", false, true, true);*/

		tbody.append(`
            <tr>
                <td>${i + 1}</td>
				<td>${formatDateTime(item["pjdate"] ?? "")}</td>
				<td>${formatDate(item["pjbookingDate"] ?? "")}</td>
				<td>${formatDate(item["pjbookingCloseDate"] ?? "")}</td>
                <td>${item['ptmaster'].name ?? ""}</td>
                <td style="text-align:right;">${toSafeNumber(item["ptamount"] ?? "").toFixed(2)}</td>
                <td>${item["ptreciptno"] ?? ""}</td>
                <td>${item["csname"] ?? ""}</td>
				<td>${item["csmobile"] ?? ""}</td>
				<td>${item["csnakshathra"] ?? ""}</td>
				<td>${item["csfamilyName"] ?? ""}</td>
				<td>${item["csaddress"] ?? ""}</td>
				<td>${item["ptmaster"].description ?? ""}</td>
            </tr>
        `);
	}
	// ---------- SET FOOTER ----------
	let totalAmount = filteredPoojaTransData.reduce((sum, item) => sum + toSafeNumber(item['ptamount'] ?? 0), 0);
	const tfoot = `
	    <tr>
	        <th colspan="5" style="text-align:right;">Total</th>
	        <th style="text-align:right;">${totalAmount.toFixed(2)}</th>
	        <th colspan="7"></th>
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
	                <th>Action</th>
	                <th>Date</th>
					<th>Booking Date</th>
					<th>Booking Close Date</th>
	                <th>Pooja</th>
	                <th>Amount</th>
	                <th>Receipt No</th>
	                <th>Devotee</th>
	                <th>Mobile</th>
	                <th>Nakshatra</th>
	                <th>Family Name</th>
	                <th>Address</th>
	                <th>Description</th>
	            </tr>
	        `);

	$("#poojaTableBody").html(`
	            <tr>
	                <td colspan="14" class="no-data">
					<div class="flex flex-col items-center justify-center text-center py-10 opacity-75">
					    <img src="https://cdn-icons-png.flaticon.com/512/7465/7465722.png" 
					         class="w-14 h-14 mb-3 opacity-50" />
					    <p class="text-gray-600 font-medium">No Pooja transactions found</p>
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
		formatDateTime(item["pjdate"] ?? ""),   		       
		formatDate(item["pjbookingDate"] ?? ""),   		       
		formatDate(item["pjbookingCloseDate"] ?? ""),   
		item['ptmaster']?.name ?? "",           
		toSafeNumber(item["ptamount"] ?? "").toFixed(2), 
		item["ptreciptno"] ?? "",          
		item["csname"] ?? "",              
		item["csmobile"] ?? "",            
		item["csnakshathra"] ?? "",        
		item["csfamilyName"] ?? "",        
		item["csaddress"] ?? "",           
		item["ptmaster"]?.description ?? ""
	]);

	let totalAmount = filteredPoojaTransData.reduce((sum, item) => sum + toSafeNumber(item['ptamount'] ?? 0), 0);

	poojaDataSetExcel.push(["", "", "", "", "Total", totalAmount.toFixed(2), "", "", "", "", "", "", ""]);

	const colWidths = [5, 20, 30, 20, 20, 30, 25, 25, 30, 35, 60];

	exportToExcel({
		fileName: "vazhipad.xlsx",
		sheetName: "Vazhipad",
		heading: [
			{ text: "Temple Vazhipad Report " + $("#fromDate").val() + " - " + $("#toDate").val(), merge: "A1:K1" }
		],
		headers: [
			"#",
			"Date",
			"Booking Date",
			"Booking Close Date",
			"Pooja",
			"Amount",
			"Receipt No",
			"Devotee",
			"Mobile",
			"Nakshatra",
			"Family Name",
			"Address",
			"Description"
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







	
	
	
	