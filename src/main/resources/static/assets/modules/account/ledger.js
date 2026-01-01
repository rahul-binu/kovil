let mappedGroup = {};
let ledgerData = [];
let mappedLedgerData = {};

let groupUnderChoice = null;

$(function() {
	getGroupData();
	getLedgerData();
});
function getGroupData() {
	$("#tableLoader").removeClass("hidden");

	fetch("/api/account/groups", {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		}
	})
		.then(res => res.json())
		.then(data => {
			mappedGroup = mapArrWithAField(data, "id");
			data.forEach(g => {
				const opt = document.createElement("option");
				opt.value = g.id;
				opt.textContent = `${g.groupName} - ${g.groupType}`;
				document.getElementById("ledgerUnder").appendChild(opt);
			});
			if (groupUnderChoice) groupUnderChoice.destroy();

			groupUnderChoice = new Choices("#ledgerUnder", {
				searchEnabled: true,
				itemSelectText: "",
				placeholderValue: "Search Group",
				searchPlaceholderValue: "Search...",
				shouldSort: false,
				classNames: {
					containerOuter: "w-full"
				}
			});
		})
		.catch(err => {
			console.log(err);
		});
}

function getLedgerData() {
	$("#tableLoader").removeClass("hidden");

	fetch("/api/account/all-ledgers", {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		}
	})
		.then(res => res.json())
		.then(data => {
			ledgerData = data;
			mappedLedgerData = mapArrWithAField(data, "id");
			buildGrupTableData(ledgerData);
		})
		.catch(err => {
			console.log(err)
			$("#groupTable tbody").html(`
				            <tr>
				                <td colspan="12" class="no-data">
				                    <div class="flex flex-col items-center justify-center text-center py-10 opacity-75">
				                        <img src="https://cdn-icons-png.flaticon.com/512/7465/7465722.png" 
				                             class="w-14 h-14 mb-3 opacity-50" />
				                        <p class="text-gray-600 font-medium">No group found</p>
				                    </div>
				                </td>
				            </tr>
				        `);
			$("#tableLoader").addClass("hidden");
		});
}

function buildGrupTableData(data) {
	let pageSize = 10;
	$("#paginationPage").pagination({
		items: data.length,
		itemsOnPage: pageSize,
		cssStyle: 'light-theme',
		onPageClick: function(pageNumber) {
			const start = (pageNumber - 1) * pageSize;
			$("#ledgerTable tbody").html(getTableText(data, start, pageSize));
		}
	});

	// Load first page by default
	$("#ledgerTable tbody").html(getTableText(data, 0, pageSize));

	$("#tableLoader").addClass("hidden");
}

function getTableText(d, s, l) {

	let tbl = '';
	for (let i = s; i < s + l && i < d.length; i++) {
		let act = actionIcons(d[i].id, "LedgerRow", (d[i].appLock > 0), (d[i].appLock > 1));
		tbl += `
		<tr>
			<td>${i + 1}</td>
			<td>${d[i].ledgerName || "-"}</td>
			<td>${getGroupDataByIdAndFl(d[i].groupUnder, "groupName") || "-"}</td>
			<td>${getGroupDataByIdAndFl(d[i].groupUnder, "groupType") || "-"}</td>
			<td>${d[i].orderNo || "0"}</td>
			<td>${d[i].description || "-"}</td>
			<td style="text-align:center">${act}</td>
		</tr>
		`;
	}

	return tbl;
}

function getGroupDataByIdAndFl(id, fl) {
	return mappedGroup?.[id]?.[fl] ?? "-";
}




function editLedgerRow(id) {
	let l = mappedLedgerData[id];
	$("#ledgerName").val(l.ledgerName);
	groupUnderChoice.setChoiceByValue(l.groupUnder + '');
	$("#ledgerOrder").val(l.orderNo);
	$("#ledgerDescription").val(l.description);
	$("#saveUpdateAccountLedger").text("Update");
	$("#saveUpdateAccountLedger").attr("data-ledger-id", id);
}

function deleteLedgerRow(id) {
	fetch("/api/account/ledger/" + id, {
		method: "DELETE",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		}
	})
		.then(async (res) => {
			const data = await res.json();

			if (!res.ok) {
				throw new Error(data.message || "Something went wrong");
			}
			return data;
		})
		.then(data => {
			showMessage("Success", data.message, true);
			getLedgerData();
		})
		.catch(err => {
			showMessage("Ops", err.message, false);
		});
}




$("#saveUpdateAccountLedger").click(function() {
	let errors = [];
	if ($("#ledgerName").val().trim() == '') {
		errors.push("Enter ledger name");
	} 
	if (toSafeNumber(groupUnderChoice.getValue(true)) == 0) {
		errors.push("Choose a ledger group");
	}
	if (errors.length > 0) {
		showErrors(errors, ".errorAppendArea");
		return;
	}


	let method = "POST";
	let ledgerId = null;

	if ($(this).text() != "Save") {
		method = "PUT";
		ledgerId = $(this).data("ledger-id");
	}

	let ledger = {
		"id": ledgerId,
		"ledgerName": $("#ledgerName").val().trim(),
		"groupUnder": toSafeNumber(groupUnderChoice.getValue(true)),
		"description": $("#ledgerDescription").val().trim(),
		"tenantId": "",
		"appLock": 2,
		"orderNo": $("#ledgerOrder").val().trim()
	}

	fetch("/api/account/ledger", {
		method: method,
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		},
		body: JSON.stringify(ledger)
	})
		.then(async (res) => {
			const data = await res.json();

			if (!res.ok) {
				throw new Error(data.message || "Something went wrong");
			}
			return data;
		})
		.then(data => {
			showMessage("Success", data.message, true);
			$(".clear-input").val('');
			getLedgerData();
		})
		.catch(err => {
			showMessage("Ops", err.message, false);
		});

	$(this).text("Save");
	$(this).attr("data-ledger-id", null);
});







