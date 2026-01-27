let tableHeader = ["Ledger", "Opening", "Receipt(Dr)", "Payments(Cr)", "Closing"];

let accountTableEntry = [];
let ledgerOpeningBalance = [];
let allDayTransactions = [];

let fromDate;

let tableText = "";

let allBankIds = [];
let cashReceipt = 0, cashPayment = 0;
let bankReceipt = {}, bankPayment = {};

let allLedgerObjects = {};

function daybookCustruct(dt, pr = "", rmk = "", vn = "", cd = "", cr = "", bd = "", bc = "", ad = "") {
	return [dt, pr, rmk, vn, cd, cr, bd, bc, ad];
}



$(function() {
	fromDate = $("#fromDate").val();
	getDayBookData();
});

$("#searchButton").click(getDayBookData);


function getDayBookData() {

	let fromDate = $("#fromDate").val();
	let toDate = $("#toDate").val();

	fetch(`/api/account/daybook/${fromDate}/${toDate}`, {
		method: "GET",
		headers: {
			"Accept": "application/json",
			"Authorization": "Bearer " + localStorage.getItem("jwtToken")
		},
	}).then(res => {
		if (!res.ok) throw new Error("API Error");
		return res.json();
	}).then(data => {

		ledgerOpeningBalance = mapToFields(data.opening.label, data.opening.data);

		allDayTransactions = mapToFields(data.transc.label, data.transc.data);

		allLedgerObjects = mapToFieldsWithKey("id", data.ledger.label, data.ledger.data);

		allBankIds = data.ledger.data.filter(l => l[2] === 11).map(l => l[0]);

		allBankIds.forEach(e => {
			bankReceipt[e] = 0;
			bankPayment[e] = 0;
		})
		buildDataArray();
	}).catch(error => {
		console.log(error);
		showMessage("", "Internal Server Error", false);
	})
}

function getLedgetDetails(id, fl) {
	return allLedgerObjects[id]?.[fl] ?? "Unknown Ledger";
}

let bankOpening = 0
let cashOpening = 0;
function buildDataArray() {
	let tdata = [];
	tableText = "";
	let emptyTableRow = "<tr><td></td><td></td><td></td><td></td><td></td></tr>";

	bankOpening = 0
	cashOpening = 0;
	cashReceipt = 0, cashPayment = 0;

	ledgerOpeningBalance.forEach(e => {
		if (e.id == 10) {
			cashOpening += e.amt;
		} else {
			bankOpening += e.amt;
		}
	});
	tdata.push(daybookCustruct());


	let allDayTransactionsLength = allDayTransactions.length;
	let ledgerWiseAmounts = {};

	function ensureLedger(ledgerId) {
		if (!ledgerWiseAmounts[ledgerId]) {
			ledgerWiseAmounts[ledgerId] = {
				ledger: ledgerId,
				opening: 0,
				receipt: 0,
				payment: 0,
				closing: 0
			};
		}
	}

	for (let i = 0; i < allDayTransactionsLength; i++) {
		let t = allDayTransactions[i];
		let amt = Number(t.amt) || 0;
		if (t.deb) {
			ensureLedger(t.deb);
			ledgerWiseAmounts[t.deb].receipt += amt;
		}
		if (t.crd) {
			ensureLedger(t.crd);
			ledgerWiseAmounts[t.crd].payment += amt;
		}
	}

	Object.values(ledgerWiseAmounts).forEach(l => {
		l.closing = l.opening + l.receipt - l.payment;
	});
	let report = Object.values(ledgerWiseAmounts).map(l => ({
		Ledger: l.ledger,
		Opening: l.opening,
		"Dr": l.receipt,
		"Cr": l.payment,
		Closing: l.closing
	}));

	tableText = generateLedgerReportHTML(report);

	$("#daybookTable").html("");
	$("#daybookTable").append(tableText);
}

function generateLedgerReportHTML(report) {

	let tableHTML = "<thead>" + getTableHeader(tableHeader, false) + "</thead>";
	tableHTML += "<tbody>";

	let totalOpening = 0;
	let totalReceipt = 0;
	let totalPayment = 0;
	let totalClosing = 0;

	for (let i = 0; i < report.length; i++) {
		const r = report[i];
		const ledgerId = r.Ledger;

		let opening = r.Opening;

		// override opening only
		if (ledgerId === 10) {
			opening = cashOpening;
		} else if (allBankIds.includes(ledgerId)) {
			opening = bankOpening;
		}

		// ALWAYS compute closing fresh
		let closing = opening + r["Dr"] - r["Cr"];

		if (ledgerId != 10 && !allBankIds.includes(ledgerId)) {
			closing = 0;
		}

		totalOpening += opening;
		totalReceipt += r["Dr"];
		totalPayment += r["Cr"];
		totalClosing += closing;

		let ledgerName = ledgerId === 10
			? "Cash"
			: getLedgetDetails(ledgerId, "nm");

		tableHTML += `
			<tr>
				<td>${ledgerName}</td>
				<td align="right">${opening.toFixed(2)}</td>
				<td align="right">${r["Dr"].toFixed(2)}</td>
				<td align="right">${r["Cr"].toFixed(2)}</td>
				<td align="right">${closing.toFixed(2)}</td>
			</tr>
		`;
	}

	tableHTML += `
	</tbody>
	<tfoot>
		<tr>
			<th>Total</th>
			<th align="right">${totalOpening.toFixed(2)}</th>
			<th align="right">${totalReceipt.toFixed(2)}</th>
			<th align="right">${totalPayment.toFixed(2)}</th>
			<th align="right">${totalClosing.toFixed(2)}</th>
		</tr>
	</tfoot>
	`;

	return tableHTML;
}




$('#htmlPrintButton').click(function() {
	let tbltxt = `
        <table style="font-size: 11px; line-height: 1.3;">
           ${tableText}
        </table>
    `;
	tbltxt += $("#daybookSummaryBlock").html();
	let header = `
        <h3 style="text-align:center">Ledger Book</h3>
		<h4 style="text-align:center">${$("#fromDate").val()} - ${$("#toDate").val()}</h4>
    `;
	printTableContent(tbltxt, header);
});


