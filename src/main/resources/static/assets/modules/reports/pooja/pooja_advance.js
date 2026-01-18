const itemsOnPage = 14;
let poojaTransData = [];
let filteredPoojaTransData = [];
let payModes = [];

$(function() {
	getPoojaData();
	getPaymodes();
})
/* ----------------------------------------------------
   FETCH REPORT DATA
---------------------------------------------------- */
$("#serchPooja").click(() => getPoojaData());

function getPoojaData() {
	let sdata = {
		"fields": {
			"pt": ["pt.transId", "pt.createdAt", "pt.amount", "pt.poojaMaster", "pt.receiptNo"],
			"pj": ["pj.devotee", "pj.user", "pj.transId", "pj.date", "pj.amount", "pj.advanceAmount", "pj.bookingDate", "pj.bookingStatus", "pj.bookingCloseDate"],
			"cs": ["cs.transId", "cs.fullName", "cs.mobile", "cs.familyName", "cs.address", "cs.nakshathra", "cs.accountId"]
		},
		"filters": {
			"pj": {
				"pj.date >=": $("#fromDate").val().toString(),
				"pj.date <=": $("#toDate").val().toString(),
				"pj.bookingStatus =": "ACTIVE"
			}
		},
		"nameFields": {
			"pt": ["pttransId", "ptcreated", "ptamount", "ptmaster", "ptreciptno"],
			"pj": ["pjdevotee", "pjuser", "pjtransId", "pjdate", "pjamount", "pjadvanceAmount", "pjbookingDate", "pjbookingStatus", "pjbookingCloseDate"],
			"cs": ["cstransId", "csname", "csmobile", "csfamilyName", "csaddress", "csnakshathra", "cdaccountId"]
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

	let finalMerged = [];

	poojaList.forEach(p => {
		let transactions = transList[p["pjtransId"]] || [];
		let cs = vendorMap[p["pjdevotee"]] || {};

		finalMerged.push({
			...p,
			...cs,
			transactions
		});
	});

	poojaTransData = finalMerged;
	filteredPoojaTransData = poojaTransData;
	applyPagination();
}

/* ----------------------------------------------------
   BUILD TABLE
---------------------------------------------------- */
function buildPoojaTransactionTable(data, start, limit) {
	poojaTableHeading();

	const tbody = $("#poojaTableBody");
	tbody.empty();

	const end = Math.min(start + limit, data.length);
	
	let tbl = '';
	for (let i = start; i < end; i++) {
		const item = data[i];
		
		let action = `
		  <i class="fa-solid fa-eye blue pointer"
		     title="View details"
		     onclick="showPoojaDetails(${i})"></i>

			 <i class="fa-solid fa-print green pointer"
		     title="print details"
		     onclick="printPoojaDetails(${i})"></i>
			 `;
		tbl += `
            <tr>
                <td>${i + 1}</td>
				<td style="text-align:center">${action}</td>
				<td>${formatDateTime(item["pjdate"] ?? "")}</td>
                <td>${formatDateTime(item['pjbookingDate'] ?? "")}</td>
				<td>${item['pjbookingStatus'] ?? ""}</td>
				<td>${formatDateTime(item['pjbookingCloseDate'] ?? "")}</td>
				<td class="ruppie-align">${item['pjamount'] ?? ""}</td>
				<td class="ruppie-align">${item['pjadvanceAmount'] ?? ""}</td>
				<td>${item['csname'] ?? ""}</td>
				<td>${item['csmobile'] ?? ""}</td>
				<td>${item['csnakshathra'] ?? ""}</td>
				<td>${item['csfamilyName'] ?? ""}</td>
				<td>${item['csaddress'] ?? ""}</td>
            </tr>
        `;
	}
	tbody.html(tbl);
	// ---------- SET FOOTER ----------
	let totalAmount = filteredPoojaTransData.reduce((sum, item) => sum + toSafeNumber(item['pjadvanceAmount'] ?? 0), 0);
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

function emptyPoojaTable() {
	poojaTableHeading();
	$("#poojaTableBody").html(`
	            <tr>
	                <td colspan="12" class="no-data">
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
function poojaTableHeading() {
	$("#poojaReportTable thead").html(`
	            <tr>
	                <th>#</th>
	                <th>Action</th>
	                <th>Date</th>
	                <th>Booking Date</th>
					<th>Booking Status</th>
					<th>Booking Close Date</th>
	                <th>Amount</th>
	                <th>Advance Amount</th>
	                <th>Devotee</th>
	                <th>Mobile</th>
	                <th>Nakshatra</th>
	                <th>Family Name</th>
	                <th>Address</th>
	            </tr>
	        `);

}
/* ----------------------------------------------------
   GLOBAL SEARCH — FILTER + REBUILD
---------------------------------------------------- */
$("#allSearchBox").on("input", function() {
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
		formatDateTime(item['pjbookingDate'] ?? ""),
		item['pjbookingStatus'] ?? "",
		formatDateTime(item['pjbookingCloseDate'] ?? ""),
		item['pjamount'] ?? "",
		item['pjadvanceAmount'] ?? "",
		item['csname'] ?? "",
		item['csmobile'] ?? "",
		item['csnakshathra'] ?? "",
		item['csfamilyName'] ?? "",
		item['csaddress'] ?? ""
	]);
	let totalAmount = filteredPoojaTransData.reduce((sum, item) => sum + toSafeNumber(item['pjadvanceAmount'] ?? 0), 0);

	poojaDataSetExcel.push(["", "", "", "", "", "Total", totalAmount.toFixed(2), "", "", "", "", ""]);

	const colWidths = [5, 20, 30, 20, 20, 30, 25, 25, 30, 35, 60];

	exportToExcel({
		fileName: "vazhipad_advance.xlsx",
		sheetName: "adv",
		heading: [
			{ text: "Temple Vazhipad Advance Report " + $("#fromDate").val() + " - " + $("#toDate").val(), merge: "A1:K1" }
		],
		headers: [
			"#",
			"Date",
			"Booking Date",
			"Booking Status",
			"Booking Close Date",
			"Amount",
			"Advance Amount",
			"Devotee",
			"Mobile",
			"Nakshatra",
			"Family Name",
			"Address"
		],
		data: poojaDataSetExcel,
		columnWidths: colWidths
	});
});


function showPoojaDetails(id) {
	let pooja = filteredPoojaTransData[id];
	let transactions = pooja.transactions;

	if (!transactions || transactions.length === 0) return;

	if (pooja.pjbookingStatus != "ACTIVE") {
		$("#paymodeAppendBlock").addClass("hidden");
	} else {
		$("#paymodeAppendBlock").removeClass("hidden")
	}

	$("#md-devoteeDetails").text(pooja.csname);
	$("#md-bookingDate").text(pooja.pjbookingDate);
	$("#md-advanceAmount").text(pooja.pjadvanceAmount);
	$("#md-poojaTotalAmount").text(pooja.pjamount);
	let reminingAmount = pooja.pjamount - pooja.pjadvanceAmount;
	if (reminingAmount < 0) reminingAmount = 0;
	$("#payingAmount").val(reminingAmount);

	$("#savePooja").attr("data-id", pooja.pjtransId);
	$("#savePooja").attr("data-vid", pooja.cdaccountId);

	$("#transactionTable tbody").empty();

	transactions.forEach((t, i) => {
		let master = t.ptmaster;

		$("#transactionTable tbody").append(`
	      <tr class="hover:bg-gray-50">
	        <td class="px-3 py-2 border text-center">${i + 1}</td>
	        <td class="px-3 py-2 border">${new Date(t.ptcreated).toLocaleString()}</td>
	        <td class="px-3 py-2 border">${t.ptreciptno}</td>
			<td class="px-3 py-2 border text-right font-medium">
			  ₹ ${t.ptamount}
			</td>
	        <td class="px-3 py-2 border text-xs">${master.name}</td>
			<td class="px-3 py-2 border text-xs">${master.groupName}</td>
			<td class="px-3 py-2 border text-xs">${master.materialsList}</td>
	      </tr>
    `);
	});

	const select = document.getElementById("payModeChosen");

	payModes.forEach(n => {
		const opt = document.createElement("option");
		opt.value = n.id;
		opt.textContent = n.ledgerName;
		select.appendChild(opt);
	});

	$("#poojaDetailsModal").removeClass("hidden");
}

function closePoojaModal() {
	$("#poojaDetailsModal").addClass("hidden");
}




function getPaymodes() {
	fetch("/api/account/ledgers?under=11,10", {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		}
	})
		.then(response => {
			if (!response.ok) {
				throw new Error("Network response was not ok: " + response.status);
			}
			return response.json();
		})
		.then(data => {
			payModes = data;
		})
		.catch(error => {
			console.error("Error fetching paymodes:", error);
		});
}

$("#savePooja").click(function() {
	let tid = $(this).attr("data-id");
	

	let fdata = {
		transId: tid,
		payingAmount: toSafeNumber($("#payingAmount").val()),
		payMode: $("#payModeChosen").val(),
		referenceNumber: $("#payingReferenceNumber").val(),
		referenceDate: $("#payingReferenceDate").val(),
		remark: $("#payingRemark").val(),
		oldAdvance: toSafeNumber($("#md-poojaTotalAmount").val()),
		closeDate: $("#md-closeDate").val(),
		vendorAccId: $(this).attr("data-vid")
	}
	
	fetch("/api/pooja/advance-close", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		},
		body: JSON.stringify(fdata)
	})
		.then(res => {
			if (!res.ok) {
				showMessage("Faild", "Somthing went Wrong", false);
			}
			return res.json();
		})
		.then(data => {
			showMessage("Success", data.message, true);
		})
		.catch(err => {
			console.error(err);
			showMessage("Faild", "Unable to process payment", false);
		});
})

function printPoojaDetails(id){
	let pooja = filteredPoojaTransData[id];
 	let transactions = pooja.transactions;

	if (!transactions || transactions.length === 0) return;
	let tbl = '';
	transactions.forEach((t, i) => {
		let master = t.ptmaster;

		tbl+=`
	      <tr class="hover:bg-gray-50">
	        <td class="px-3 py-2 border text-center">${i + 1}</td>
	        <td class="px-3 py-2 border">${new Date(t.ptcreated).toLocaleString()}</td>
	        <td class="px-3 py-2 border">${t.ptreciptno}</td>
			<td class="px-3 py-2 border text-right font-medium">
			  ₹ ${t.ptamount}
			</td>
	        <td class="px-3 py-2 border text-xs">${master.name}</td>
			<td class="px-3 py-2 border text-xs">${master.groupName}</td>
			<td class="px-3 py-2 border text-xs">${master.materialsList}</td>
	      </tr>
    	`;
	});
	let tbltxt = `
		<div style="font-family: 'Times New Roman', serif; width: 650px; margin: auto; color: #000; padding: 20px; border: 1px solid #000;">

			<!-- Header -->
			<div style="text-align: center; margin-bottom: 20px;">
				<h1 style="margin:0; font-size: 28px; letter-spacing: 1px;">${$("#clientName").val()}</h1>
				<h4 style="margin:0; font-size: 20px; letter-spacing: 1px;">${$("#clientAddress").val()}</h4>
				<h5 style="margin:0; font-size: 20px; letter-spacing: 1px;">Pooja Booking Receipt</h5>
				<p style="margin:2px 0; font-size: 14px;">${formatDateTime(pooja.pjdate)}</p>
			</div>

 			<div style="margin-bottom: 20px; line-height: 1.6;">
				<p><strong>Devotee Name:</strong> ${pooja.csname}</p>
				<p><strong>Devotee Nakshatra:</strong> ${pooja.csnakshathra}</p>
				<p><strong>Devotee Address:</strong>${pooja.csfamilyName} ${pooja.csaddress} ${pooja.csmobile}</p>
			</div>

			<table>${tbl}</table>


			<!-- Amount & Remarks -->
			<div style="margin-bottom: 30px; line-height: 1.5;">
				<p><strong>Advance Paid Amount:</strong> <span style="font-size: 18px; font-weight: bold;">${pooja.pjadvanceAmount || "-"}</span></p>
				<p><strong>Total Pooja Amount:</strong> <span style="font-size: 18px; font-weight: bold;">${pooja.pjamount || "-"}</span></p>
				<p><strong>Remining Payable Amount:</strong> <span style="font-size: 18px; font-weight: bold;">${(pooja.pjamount - pooja.pjadvanceAmount) || "-"}</span></p>
				
			</div>

			<!-- Footer / Signatures -->
			<div style="display: flex; justify-content: space-between; margin-top: 40px; text-align: center;">
				<div>
					<p>Prepared By</p>
					<p>________________</p>
				</div>
				<div>
					<p>Checked By</p>
					<p>________________</p>
				</div>
				<div>
					<p>Authorized By</p>
					<p>________________</p>
				</div>
			</div>

			<!-- Optional Note -->
			<div style="text-align: center; margin-top: 30px; font-size: 12px;">
				<em>Note: This is a computer-generated voucher</em>
			</div>

		</div>
	`;



	printTableContent(tbltxt, "");
}