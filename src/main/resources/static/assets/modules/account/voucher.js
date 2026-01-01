let mappedLedgerData = {};
let ledgerAssets = [];
let ledgerIncome = [];
let ledgerExpense = [];
let ledgerLiability = [];

let ledgerFromChoice;
let ledgerToChoice;

let voucherPaymnetData = [];
let itemsOnPage = 10;

let pageHeading = "";
$(function() {
	pageHeading = $("#voucherType").val().charAt(0).toUpperCase() + $("#voucherType").val().slice(1).toLowerCase()
	$("#headdingType").text(pageHeading);
	getAllLedgers();
	
	setTimeout(()=>{
		getAllVoucherData();
	}, 1000);
})

function getAllLedgers() {
	$("#tableLoader").removeClass("hidden");

	fetch("/api/account/all-ledgers4voucher", {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		}
	})
		.then(res => res.json())
		.then(data => {
			mappedLedgerData = mapToFieldsWithKey("id", data.label, data.data);
			let l = mapToFields(data.label, data.data);
			l.forEach(e => {
				switch (e.gt) {
					case "ASSET":
						ledgerAssets.push(e);
						break;
					case "INCOME":
						ledgerIncome.push(e);
						break;
					case "EXPENSE":
						ledgerExpense.push(e);
						break;
					case "LIABILITY":
						ledgerLiability.push(e);
						break;
					case "Equity":
						ledgerEquity.push(e);
						break;
					default:
						console.warn("Unknown group type:", e.gt, e);
						break;
				}
			});
			buildDropDowns();
		})
		.catch(err => {
			console.log(err)

		});
}

function buildDropDowns() {
	/*console.log(ledgerAssets)
	console.log(ledgerExpense)
	console.log(ledgerIncome)
	console.log(ledgerLiability)*/

	let fromLedger = [];
	let toLedger = [];

	switch ($("#voucherType").val()) {
		case "payment":
			fromLedger = [...ledgerAssets];
			toLedger   = [...ledgerExpense, ...ledgerAssets, ...ledgerLiability];
			break;
		case "receipt":
			fromLedger = [...ledgerIncome, ...ledgerLiability];
			toLedger   = [...ledgerAssets];
			break;
		case "contra":
			fromLedger = [...ledgerAssets];
			toLedger = [...ledgerAssets];
			break;
		default: // joural entry
			fromLedger = [...ledgerAssets, ...ledgerIncome, ...ledgerExpense, ...ledgerLiability];
			toLedger = [...ledgerAssets, ...ledgerIncome, ...ledgerExpense, ...ledgerLiability];
			break;
	}

	fromLedger.forEach(l => {
		const opt = document.createElement("option");
		opt.value = l.id;
		opt.textContent = `${l.nm} - ${l.gt}`;
		document.getElementById("fromLedger").appendChild(opt);
	});
	if (ledgerFromChoice) ledgerFromChoice.destroy();

	ledgerFromChoice = new Choices("#fromLedger", {
		searchEnabled: true,
		itemSelectText: "",
		placeholderValue: "Search Ledger",
		searchPlaceholderValue: "Search...",
		shouldSort: false,
		classNames: {
			containerOuter: "w-full"
		}
	});

	toLedger.forEach(l => {
		const opt = document.createElement("option");
		opt.value = l.id;
		opt.textContent = `${l.nm} - ${l.gt}`;
		document.getElementById("toLedger").appendChild(opt);
	});
	if (ledgerToChoice) ledgerToChoice.destroy();

	ledgerToChoice = new Choices("#toLedger", {
		searchEnabled: true,
		itemSelectText: "",
		placeholderValue: "Search Ledger",
		searchPlaceholderValue: "Search...",
		shouldSort: false,
		classNames: {
			containerOuter: "w-full"
		}
	});
}

$("#saveUpdateAccountLedger").click(function() {
	let errors = [];

	if (toSafeNumber($("#fromLedger").val()) == 0) {
		errors.push("Select a valid from ledger");
	}

	if (toSafeNumber($("#toLedger").val()) == 0) {
		errors.push("Select a valid to ledger");
	}

	if (toSafeNumber($("#voucherAmount").val()) == 0) {
		errors.push("Enter a valid amount");
	}

	if (errors.length > 0) {
		showErrors(errors, ".errorAppendArea");
		return;
	}
	openUniversalConfirmModal({
		title: "Save the " + pageHeading,
		message: "Do you want to save this " + pageHeading + "?",
		actionText: "Save",
		onConfirm: saveVoucherEnrty
	});

});

function saveVoucherEnrty() {
	let fdata = {
		vdt: $("#voucherDate").val(),
		fml: toSafeNumber($("#fromLedger").val()),
		tol: toSafeNumber($("#toLedger").val()),
		amt: toSafeNumber($("#voucherAmount").val()),
		ren: $("#referenceNo").val().trim(),
		red: $("#referenceDate").val(),
		rem: $("#remark").val().trim(),
		vtp: $("#voucherType").val().toUpperCase()
	};


	fetch("/api/account/voucher", {
		method: "Post",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		},
		body: JSON.stringify(fdata)
	})
		.then(res => res.json())
		.then(data => {
			if (data){
				showMessage("Success", pageHeading + " entry is successfull", true);
				setTimeout(()=>{
					location.reload();
				}, 300);
			} else{
				showMessage("Faild", "Something went wrong", false);
			}
			$(".clear-input").val('');
		})
		.catch(err => {
			console.log(err)
			showMessage("Error", err.message, false);
		});
}



function getAllVoucherData() {
	$("#tableLoader").removeClass("hidden");
		
	let headers = ["#", "Type", "Date", "V.No", "From", "To", "Amount", "Ref No", "Ref Date", "Remark"];

	$("#voucherListTable thead").html("<tr>" + buildReportHeaderRow(headers, false) + "<th>Action</th></tr>");

	let fromDt = $("#fromDate").val();
	let toDt = $("#toDate").val();
	let vtyp= $("#voucherType").val().toUpperCase() + " VOUCHER";
	let url = `/api/account/voucher-payments/${fromDt}/${toDt}/${vtyp}`;
	
	fetch(url, {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		}
	})
		.then(res => res.json())
		.then(response => {
			voucherPaymnetData = mapToFields(response.label ,response.data);
			paginateTable();
		})
		.catch(err => {
			console.log(err)
			$("#voucherListTable tbody").html(`
					            <tr>
					                <td colspan="11" class="no-data">
					                    <div class="flex flex-col items-center justify-center text-center py-10 opacity-75">
					                        <img src="https://cdn-icons-png.flaticon.com/512/7465/7465722.png" 
					                             class="w-14 h-14 mb-3 opacity-50" />
					                        <p class="text-gray-600 font-medium">No entry found</p>
					                    </div>
					                </td>
					            </tr>
					        `);
			$("#tableLoader").addClass("hidden");
		});
}

$("#toggleAccordion").click(function() {
	$("#accordionContent").toggleClass("hidden");
	const arrow = $("#arrow");
	arrow.toggleClass("rotate-180");
});

function buildReportHeaderRow(hed, tr = false) {
	let thead = (tr) ? "<tr>" : "";

	hed.forEach(e => {
		thead += `<th>${e}</th>`;
	});
	thead += (tr) ? "<tr>" : "";
	return thead;
}

function paginateTable(){
	let filterdData = voucherPaymnetData;
	$("#pagination").pagination({
		items: filterdData.length,
		itemsOnPage: itemsOnPage,
		cssStyle: 'light-theme',
		onPageClick: function(pageNumber) {
			const start = (pageNumber - 1) * itemsOnPage;
			buildReportTableBody(filterdData, start, itemsOnPage);
		}
	});

	buildReportTableBody(filterdData, 0, itemsOnPage);
}

$("#searchVoucherPayments").click(function(){
	getAllVoucherData();
});

function buildReportTableBody(data, s, l){
	let t = '';
	const end = Math.min(s + l, data.length);
	for(let i = s; i < end; i++){
		let d = data[i];
		let act = actionIcons(d.tid, "VoucherRow")
		t+=`
		<tr>
			<td>${i+1}</td>
			<td>${d.vt}</td>
			<td>${formatDateTime(d.td ?? "")}</td>
			<td>${d.vn}</td>
			<td>${mappedLedgerData[d.cl].nm}</td>
			<td>${mappedLedgerData[d.dl].nm}</td>
			<td style="text-align:right;">${toSafeNumber(d.am ?? "").toFixed(2)}</td>
			<td>${d.rn}</td>
			<td>${formatDate(d.rd ?? "")}</td>
			<td>${d.rm}</td>
			<td>${act}</td>
		</tr>
		`;
	}
	$("#voucherListTable tbody").html(t);
	
	$("#tableLoader").addClass("hidden");
}

function deleteVoucherRow(id){
	console.log(id)
}

function editVoucherRow(id){
	console.log(id)
}



