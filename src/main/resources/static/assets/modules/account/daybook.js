let tableHeader = ["Date", "Particulars(Narration)", "V. No", "Receipt(Dr)", "Payments(Cr)", "Bank Rec(Dr)", "Bank Pay(Cr)", "Adjustments"];

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
		console.log(data);

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

function buildDataArray() {
	let tdata = [];
	tableText = "";
	let emptyTableRow = "<tr><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>";

	let bankOpening = 0
	let cashOpening = 0;
	let closingDate = "";
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
	for (let i = 0; i < allDayTransactionsLength; i++) {

		let t = allDayTransactions[i];

		let amountNum = toSafeNumber(t.amt);
		let startingDate = formatDate(t.tdt);
		if (closingDate != startingDate) {

			if (closingDate != "") {
				tableText += '<tr class="bold-row"><td>' + formatDate(closingDate) + '</td><td style="text-align:right!important">Closing</td><td></td><td>' + ((cashOpening >= 0) ? formatRupee(cashOpening) : "") + '</td><td>' + ((cashOpening < 0) ? formatRupee(cashOpening) : "") + '</td><td>' + ((bankOpening >= 0) ? formatRupee(bankOpening) : "") + '</td><td>' + ((bankOpening < 0) ? formatRupee(bankOpening) : "") + '</td><td></td></tr>';
				tableText += emptyTableRow;
			}

			closingDate = startingDate;

			tableText += '<tr class="bold-row"><td>' + formatDate(closingDate) + '</td><td style="text-align:right!important">Opening</td><td></td><td>' + ((cashOpening >= 0) ? formatRupee(cashOpening) : "") + '</td><td>' + ((cashOpening < 0) ? formatRupee(cashOpening) : "") + '</td><td>' + ((bankOpening >= 0) ? formatRupee(bankOpening) : "") + '</td><td>' + ((bankOpening < 0) ? formatRupee(bankOpening) : "") + '</td><td></td></tr>';
		}

		if (t.crd == 10) {
			tdata.push(daybookCustruct(formatDateTime(t.tdt), getLedgetDetails(t.deb, "nm") + "[" + getLedgetDetails(t.crd, "nm") + "]", t.rmk, t.vno, "", formatRupee(t.amt), "", "", ""));

			cashOpening -= amountNum;
			cashPayment += amountNum;
			tableText += `
				<tr>
					<td>${formatDateTime(t.tdt)}</td>
					<td>
						${getLedgetDetails(t.deb, "nm")} [${getLedgetDetails(t.crd, "nm")}]
						${t.rmk ? `-- <span class="light-text">${t.rmk}</span>` : ""}
					</td>
					<td>${t.vno}</td>
					<td></td>
					<td class="ruppie-align">${formatRupee(t.amt)}</td>
					<td></td>
					<td></td>
					<td></td>
				</tr>
			`;

			if (allBankIds.includes(t.deb)) {
				tdata.push(daybookCustruct(formatDateTime(t.tdt), getLedgetDetails(t.deb, "nm") + "[" + getLedgetDetails(t.crd, "nm") + "]", t.rmk, t.vno, "", "", formatRupee(t.amt), "", ""));

				bankOpening += amountNum;
				bankReceipt[t.deb] += amountNum;
				
				tableText += `
					<tr>
						<td>${formatDateTime(t.tdt)}</td>
						<td>
							${getLedgetDetails(t.crd, "nm")} [${getLedgetDetails(t.deb, "nm")}]
							${t.rmk ? `-- <span class="light-text">${t.rmk}</span>` : ""}
						</td>
						<td>${t.vno}</td>
						<td></td>
						<td></td>
						<td class="ruppie-align">${formatRupee(t.amt)}</td>
						<td></td>
						<td></td>
					</tr>
				`;
			}
		} else if (t.deb == 10) {
			tdata.push(daybookCustruct(formatDateTime(t.tdt), getLedgetDetails(t.deb, "nm") + "[" + getLedgetDetails(t.crd, "nm") + "]", t.rmk, t.vno, formatRupee(t.amt), "", "", "", ""));

			cashOpening += amountNum;
			cashReceipt += amountNum;

			tableText += `
				<tr>
					<td>${formatDateTime(t.tdt)}</td>
					<td>
						${getLedgetDetails(t.deb, "nm")} [${getLedgetDetails(t.crd, "nm")}]
						${t.rmk ? `-- <span class="light-text">${t.rmk}</span>` : ""}
					</td>
					<td>${t.vno}</td>
					<td class="ruppie-align">${formatRupee(t.amt)}</td>
					<td></td>
					<td></td>
					<td></td>
					<td></td>
				</tr>
			`;

			if (allBankIds.includes(t.crd)) {
				tdata.push(daybookCustruct(formatDateTime(t.tdt), getLedgetDetails(t.deb, "nm") + "[" + getLedgetDetails(t.crd, "nm") + "]", t.rmk, t.vno, "", "", "", formatRupee(t.amt), ""));

				bankOpening -= amountNum;
				bankPayment[t.crd] += amountNum;
				
				tableText += `
							<tr>
								<td>${formatDateTime(t.tdt)}</td>
								<td>
									${getLedgetDetails(t.crd, "nm")} [${getLedgetDetails(t.deb, "nm")}]
									${t.rmk ? `-- <span class="light-text">${t.rmk}</span>` : ""}
								</td>
								<td>${t.vno}</td>
								<td></td>
								<td></td>
								<td></td>
								<td class="ruppie-align">${formatRupee(t.amt)}</td>
								<td></td>
							</tr>
						`;

			}
		} else if (allBankIds.includes(t.deb)) {
			tdata.push(daybookCustruct(formatDateTime(t.tdt), getLedgetDetails(t.deb, "nm") + "[" + getLedgetDetails(t.crd, "nm") + "]", t.rmk, t.vno, "", "", formatRupee(t.amt), "", ""));

			bankOpening += amountNum;
			bankReceipt[t.deb] += amountNum;

			tableText += `
				<tr>
					<td>${formatDateTime(t.tdt)}</td>
					<td>
						${getLedgetDetails(t.deb, "nm")} [${getLedgetDetails(t.crd, "nm")}]
						${t.rmk ? `-- <span class="light-text">${t.rmk}</span>` : ""}
					</td>
					<td>${t.vno}</td>
					<td></td>
					<td></td>
					<td class="ruppie-align">${formatRupee(t.amt)}</td>
					<td></td>
					<td></td>
				</tr>
			`;
		} else if (allBankIds.includes(t.crd)) {
			tdata.push(daybookCustruct(formatDateTime(t.tdt), getLedgetDetails(t.deb, "nm") + "[" + getLedgetDetails(t.crd, "nm") + "]", t.rmk, t.vno, "", "", "", formatRupee(t.amt), ""));

			bankOpening -= amountNum;

			bankPayment[t.crd] += amountNum;

			tableText += `
				<tr>
					<td>${formatDateTime(t.tdt)}</td>
					<td>
						${getLedgetDetails(t.deb, "nm")} [${getLedgetDetails(t.crd, "nm")}]
						${t.rmk ? `-- <span class="light-text">${t.rmk}</span>` : ""}
					</td>
					<td>${t.vno}</td>
					<td></td>
					<td></td>
					<td></td>
					<td class="ruppie-align">${formatRupee(t.amt)}</td>
					<td></td>
				</tr>
			`;
		} else {
			tdata.push(daybookCustruct(formatDateTime(t.tdt), getLedgetDetails(t.deb, "nm") + "[" + t.rmk + "]", t.rmk, t.vno, "", "", "", "", formatRupee(t.amt) + " DR"));
			tdata.push(daybookCustruct(formatDateTime(t.tdt), getLedgetDetails(t.crd, "nm") + "[" + t.rmk + "]", t.rmk, t.vno, "", "", "", "", formatRupee(t.amt) + " CR"));
			tableText += `
				<tr>
					<td>${formatDateTime(t.tdt)}</td>
					<td>
						${getLedgetDetails(t.deb, "nm")} [${getLedgetDetails(t.crd, "nm")}]
						${t.rmk ? `-- <span class="light-text">${t.rmk}</span>` : ""}
					</td>
					<td>${t.vno}</td>
					<td></td>
					<td></td>
					<td></td>
					<td></td>
					<td class="ruppie-align">${formatRupee(t.amt)} DR</td>
				</tr>
			`;
			tableText += `
				<tr>
					<td>${formatDateTime(t.tdt)}</td>
					<td>
						${getLedgetDetails(t.crd, "nm")} [${getLedgetDetails(t.deb, "nm")}]
						${t.rmk ? `-- <span class="light-text">${t.rmk}</span>` : ""}
					</td>
					<td>${t.vno}</td>
					<td></td>
					<td></td>
					<td></td>
					<td></td>
					<td class="ruppie-align">${formatRupee(t.amt)} CR</td>
				</tr>
			`;
		}
	}

	if (closingDate == "") {
		closingDate = fromDate + "";
		tableText += '<tr class="bold-row"><td>' + formatDate(closingDate) + '</td><td style="text-align:right!important">Opening</td><td></td><td>' + ((cashOpening >= 0) ? formatRupee(cashOpening) : "") + '</td><td>' + ((cashOpening < 0) ? formatRupee(cashOpening) : "") + '</td><td>' + ((bankOpening >= 0) ? formatRupee(bankOpening) : "") + '</td><td>' + ((bankOpening < 0) ? formatRupee(bankOpening) : "") + '</td><td></td></tr>';
		tableText += emptyTableRow;
	}
	tableText += '<tr class="bold-row"><td>' + formatDate(closingDate) + '</td><td style="text-align:right!important">Closing</td><td></td><td>' + ((cashOpening >= 0) ? formatRupee(cashOpening) : "") + '</td><td>' + ((cashOpening < 0) ? formatRupee(cashOpening) : "") + '</td><td>' + ((bankOpening >= 0) ? formatRupee(bankOpening) : "") + '</td><td>' + ((bankOpening < 0) ? formatRupee(bankOpening) : "") + '</td><td></td></tr>';




	$("#daybookTable thead").html(getTableHeader(tableHeader, false));

	$("#daybookTable tbody").html(tableText);
	loadSummary();
}




function loadSummary() {

	let co = 0;

	let bot = "", brt = "", bpt = "", bct = "";
	let bo = 0, br = 0, bp = 0, bc=0;

	let bankClosing = {};
	
	ledgerOpeningBalance.forEach(o => {
		if (o.id == 10) {
			co += o.amt;
		} else {
			bot += `${getLedgetDetails(o.id, "nm")} : ${formatINR(o.amt)}<br>`;
			bo += o.amt;
			bankClosing[o.id] = o.amt;
		}
	});

	$("#cashOpening").text(formatINR(co));
	$("#bankOpeningDetails").html(bot);
	$("#bankOpeningTotal").text(`Total : ${formatINR(bo)}`);

	$("#cashReceipt").text(formatINR(cashReceipt));
	$("#cashPayment").text(formatINR(cashPayment));
	$("#cashClosing").text(formatINR(co + cashReceipt - cashPayment));


	Object.entries(bankReceipt).forEach(([i, e]) => {
		brt += `${getLedgetDetails(i, "nm")} : ${formatINR(e)}<br>`;
		bankClosing[i] += e;
		br += e;
	});

	Object.entries(bankPayment).forEach(([i, e]) => {
		bpt += `${getLedgetDetails(i, "nm")} : ${formatINR(e)}<br>`;
		bankClosing[i] -= e;
		bp += e;
	});

	Object.entries(bankClosing).forEach(([i, e]) => {
		bct += `${getLedgetDetails(i, "nm")} : ${formatINR(e)}<br>`;
		bc += e;
	});

	$("#bankReceiptDetails").html(brt);
	$("#bankPaymentDetails").html(bpt);
	$("#bankClosingDetails").html(bct);

	$("#bankReceiptTotal").text(`Total : ${formatINR(br)}`);
	$("#bankPaymentTotal").text(`Total : ${formatINR(bp)}`);
	$("#bankClosingTotal").text(`Total : ${formatINR(bc)}`);
}


$('#htmlPrintButton').click(function() {
	let tbltxt = `
        <table style="font-size: 11px; line-height: 1.3;">
            <thead>${getTableHeader(tableHeader, false)}</thead>
            <tbody>${tableText}</tbody>
        </table>
    `;
	let header = `
        <h3 style="text-align:center">Day Book</h3>
		<h4 style="text-align:center">${$("#fromDate").val()} - ${$("#toDate").val()}</h4>
    `;
	printTableContent(tbltxt, header);
});


