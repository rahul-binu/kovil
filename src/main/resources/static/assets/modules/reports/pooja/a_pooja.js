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
				"pj.bookingStatus <>": "ACTIVE"
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
	let transList = mapToFieldsGrouped("pttransId", data.pt.label, data.pt.data);

	let groupedByTransId = {};

	poojaList.forEach(p => {
		let transactions = transList[p["pjtransId"]] || [];
		let cs = vendorMap[p["pjdevotee"]] || {};

		let transId = p["pjtransId"];
		if (!groupedByTransId[transId]) {
			groupedByTransId[transId] = {
				transId: transId,
				date: p["pjdate"],
				bookingDate: p["pjbookingDate"],
				bookingCloseDate: p["pjbookingCloseDate"],
				transactions: transactions,
				poojaAmount: 0,
				devotees: []
			};
		}

		groupedByTransId[transId].poojaAmount += toSafeNumber(p["pjamount"] ?? 0);
		groupedByTransId[transId].devotees.push({
			...cs,
			pjamount: p["pjamount"]
		});
	});

	poojaTransData = Object.values(groupedByTransId);
	filteredPoojaTransData = poojaTransData;
	applyPagination();
}

/* ----------------------------------------------------
   BUILD TABLE
---------------------------------------------------- */
function buildPoojaTransactionTable(data, start, limit) {
	let thead = `
    <tr>
        <th style="width: 5%">#</th>
		<th style="width: 30%">Pooja Details</th>
		<th style="width: 40%">Devotee Details</th>
        <th style="width: 15%; text-align:right;">Amount</th>
		<th style="width: 10%; text-align:center;">Action</th>
    </tr>`;
	$("#poojaReportTable thead").html(thead);

	const tbody = $("#poojaTableBody");
	tbody.empty();

	const end = Math.min(start + limit, data.length);

	for (let i = start; i < end; i++) {
		const item = data[i];
		let action = actionIcons(item.transId, "PoojaTransaction", false, true, true);

		let poojaNames = item.transactions.map(t => t.ptmaster?.name).filter(Boolean).join(", ") || "Unknown Pooja";
		let receiptNos = item.transactions.map(t => t.ptreciptno).filter(Boolean).join(", ");

		let devoteeHtml = item.devotees.map(d => `
			<div class="mb-1 border-b border-gray-100 last:border-0 pb-1">
				<span class="font-semibold text-gray-800">${d.csname ?? ""}</span>
				${d.csmobile ? `<span class="text-sm text-gray-600 ml-2"><i class="fas fa-phone-alt text-xs"></i> ${d.csmobile}</span>` : ""}
				<div class="text-xs text-gray-500 mt-0.5">
					${[d.csnakshathra, d.csfamilyName, d.csaddress].filter(Boolean).join(" | ")}
				</div>
			</div>
		`).join("");

		tbody.append(`
            <tr>
                <td>${i + 1}</td>
				<td>
					<div class="font-bold text-blue-600 text-base mb-1">${poojaNames}</div>
					${receiptNos ? `<div class="text-xs text-gray-600 mb-1">Receipt No: <span class="font-medium">${receiptNos}</span></div>` : ''}
					<div class="text-xs text-gray-500">Date: ${formatDateTime(item.date ?? "")}</div>
					<div class="text-xs text-gray-500">Booking: ${formatDate(item.bookingDate ?? "")} to ${formatDate(item.bookingCloseDate ?? "")}</div>
				</td>
				<td>
					${devoteeHtml}
				</td>
				<td style="text-align:right;" class="font-medium">
					${toSafeNumber(item.poojaAmount ?? 0).toFixed(2)}
				</td>
				<td style="text-align:center; vertical-align: middle;">${action}</td>
            </tr>
        `);
	}
	// ---------- SET FOOTER ----------
	let totalAmount = filteredPoojaTransData.reduce((sum, item) => sum + toSafeNumber(item.poojaAmount ?? 0), 0);
	const tfoot = `
	    <tr>
	        <th colspan="3" style="text-align:right; font-size: 1.1em;">Total Amount</th>
	        <th style="text-align:right; font-size: 1.1em; color: #15803d;">${totalAmount.toFixed(2)}</th>
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
		onPageClick: function (pageNumber) {
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
	                <th style="width: 5%">#</th>
					<th style="width: 30%">Pooja Details</th>
					<th style="width: 40%">Devotee Details</th>
			        <th style="width: 15%; text-align:right;">Amount</th>
					<th style="width: 10%; text-align:center;">Action</th>
	            </tr>
	        `);

	$("#poojaTableBody").html(`
	            <tr>
	                <td colspan="5" class="no-data">
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
		let searchableText = [
			row.transId,
			row.date,
			row.bookingDate,
			row.bookingCloseDate,
			...row.transactions.map(t => `${t.ptmaster?.name} ${t.ptreciptno} ${t.ptmaster?.description}`),
			...row.devotees.map(d => `${d.csname} ${d.csmobile} ${d.csnakshathra} ${d.csfamilyName} ${d.csaddress}`)
		].join(" ").toLowerCase();

		return searchableText.includes(searchTerm);
	});

	buildPoojaTransactionTable(filteredPoojaTransData, 0, filteredPoojaTransData.length);


});

$("#excelExportPooja").click(function () {
	if (!filteredPoojaTransData || filteredPoojaTransData.length === 0) {
		alert("No data to export!");
		return;
	}

	let poojaDataSetExcel = [];
	let idx = 1;

	filteredPoojaTransData.forEach((item) => {
		let poojaName = item.transactions.map(t => t.ptmaster?.name).filter(Boolean).join(", ");
		let poojaDesc = item.transactions.map(t => t.ptmaster?.description).filter(Boolean).join(", ");
		let receiptNos = item.transactions.map(t => t.ptreciptno).filter(Boolean).join(", ");

		item.devotees.forEach((d) => {
			poojaDataSetExcel.push([
				idx++,
				formatDateTime(item.date ?? ""),
				formatDate(item.bookingDate ?? ""),
				formatDate(item.bookingCloseDate ?? ""),
				poojaName,
				toSafeNumber(d.pjamount ?? 0).toFixed(2),
				receiptNos,
				d.csname ?? "",
				d.csmobile ?? "",
				d.csnakshathra ?? "",
				d.csfamilyName ?? "",
				d.csaddress ?? "",
				poojaDesc
			]);
		});
	});

	let totalAmount = filteredPoojaTransData.reduce((sum, item) => sum + toSafeNumber(item.poojaAmount ?? 0), 0);

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
let ptid = "";
$("#confirmPrint").on("change", function () {
	printPoojaTransaction(ptid);

});

function printPoojaTransaction(id) {
	ptid = id;
	let ism = $("#confirmPrint").is(":checked") == true ? 1 : 0;
	$("#printFrame").attr("src", `/web/pooja/receipt/0/${id}/${ism}`);
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

function deletePoojaTransaction(id) {
	openUniversalConfirmModal({
		title: "Delete Voucher",
		message: "Are you sure you want to delete this pooja entry?",
		actionText: "Delete",
		onConfirm: () => confirmDeletePoojaRow(id)
	});
}

function confirmDeletePoojaRow(tid) {
	fetch(`/api/pooja/offering/${tid}`, {
		method: "DELETE",
		headers: {
			"Accept": "application/json",
			"Authorization": "Bearer " + localStorage.getItem("jwtToken")
		}
	})
		.then(res => {
			if (!res.ok) {
				throw new Error(`Delete failed: ${res.status}`);
			}
			return res.json();
		})
		.then(data => {
			showMessage("Success", " Entry is Deleted", true);
			getPoojaData();
		})
		.catch(err => {
			showMessage("Failed", " Entry failed to Delete", false);
		});
}








