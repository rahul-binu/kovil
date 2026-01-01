let tableHeader = ["#", "Ledger", "Opening Date", "Amount"];
let mappedLedgerData = {};
let allLedger = [];


let ledgerFromChoice;

$(function() {
	getAllLedgers();
	setTimeout(() => {
		getOpeningData();
	}, 1000);
});


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
				allLedger.push(e);
			});
			buildDropDowns();
		})
		.catch(err => {
			console.log(err)

		});
}

function buildDropDowns() {

	allLedger.forEach(l => {
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
}

function getOpeningData() {
	$("#tableLoader").removeClass("hidden");

	fetch(`/api/account/opening-balance/${$("#fromDate").val()}/${$("#toDate").val()}`, {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		}
	})
		.then(res => res.json())
		.then(data => {
			$("#dataListTable thead").html(getTableHeader(tableHeader, true, 4));

			$("#dataListTable tbody").html(buildTableBody(data));
		})
		.catch(err => {
			console.log(err)

		});
	$("#tableLoader").addClass("hidden");
}

function buildTableBody(data) {
	let txt = '';

	data.forEach((e, i) => {

		let act = actionIcons(e.id, "AccountOpening", false, true);
		txt += `
			<tr>
				<td>${i + 1}</td>
				<td>${getLedgerData(e.ledgerId, "nm")}</td>
				<td>${formatDate(e.openingDate)}</td>
				<td class="ruppie-align">${e.amount}</td>
				<td>${act}</td>
			</tr>
		`;
	})
	return txt;
}



$("#saveUpdateAccountLedger").click(function() {
	let errors = [];

	if (toSafeNumber($("#fromLedger").val()) == 0) {
		errors.push("Select a valid ledger");
	}

	if (toSafeNumber($("#voucherAmount").val()) == 0) {
		errors.push("Enter a valid amount");
	}

	if ($("#voucherDate").val() == '') {
		errors.push("Enter a valid date");
	}

	if (errors.length > 0) {
		showErrors(errors, ".errorAppendArea");
		return;
	}
	openUniversalConfirmModal({
		title: "Save Opening",
		message: "Do you want to save this this opening entry?",
		actionText: "Save",
		onConfirm: saveOpeningBalanceEnrty
	});

});

function saveOpeningBalanceEnrty() {
	let fdata = {
		id: null,
		ledgerId: toSafeNumber($("#fromLedger").val()),
		openingDate: $("#voucherDate").val(),
		amount: toSafeNumber($("#voucherAmount").val()),
		status: "ACTIVE"
	};
	fetch(`/api/account/opening-balance`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		},
		body: JSON.stringify(fdata)
	})
		.then(res => res.json())
		.then(data => {
			if (data.id == null) {
				showMessage("Error", "Faild to save", false);
			} else {
				showMessage("Success", "Opening saved", true);
				getOpeningData();
			}
		})
		.catch(err => {
			showMessage("Error", "Internal server error", false);

		});
}

$("#searchPayments").click(getOpeningData);

function getLedgerData(id, fl) {
	return mappedLedgerData[id]?.[fl] || "UNKNOWN";
}


function deleteAccountOpening(id) {
	openUniversalConfirmModal({
		title: "Delete Opening",
		message: "Do you want to delete this opening?",
		actionText: "Delete",
		onConfirm: () => deleteOpeningBalance(id)
	});
}


function deleteOpeningBalance(id) {
	fetch(`/api/account/opening-balance?id=${id}`, {
		method: "DELETE",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		}
	})
		.then(res => res.json())
		.then(data => {
			if (data.status == "OK") {
				showMessage("Success", data.message, true);
				getOpeningData();
			} else {
				showMessage("Error", "Faild to delete", false);
			}
		})
		.catch(err => {
			showMessage("Error", "Internal server error", false);

		});
}











