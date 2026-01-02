let tableHeader = ["#", "Transaction Date", "ItemGroup", "Item Name", "Transaction Type", "Quantity", "Remarks"];

let fullItemData = {};
let conversionUnit = {};

let itemListChoice;

$(function() {
	getInitials();
});

$("#searchBtn").click(getCurrentStockTransactions);

async function getInitials() {
	try {
		const response = await fetch("/api/inv/item", {
			method: "GET",
			headers: {
				"Accept": "application/json",
				"Authorization": "Bearer " + localStorage.getItem("jwtToken")
			}
		});
		if (!response.ok) {
			showMessage("Faild", `Failed to fetch items: ${response.status}`, false);
		}
		fullItemData = mapArrWithAField(await response.json(), "id");
		getCurrentStockTransactions();
		stockModelInitialization();
	} catch (error) {
		showMessage("Error", "Somthing went wrong", false);
	}

	try {
		const response = await fetch("/api/inv/item/unit-convertions", {
			method: "GET",
			headers: {
				"Accept": "application/json",
				"Authorization": "Bearer " + localStorage.getItem("jwtToken")
			}
		});
		if (!response.ok) {
			showMessage("Faild", `Failed to fetch conversions: ${response.status}`, false);
		}
		conversionUnit = mapArrWithAFieldToObjArr(await response.json(), "fromUnit");
	} catch (error) {
		showMessage("Error", "Somthing went wrong", false);
	}
}



function getCurrentStockTransactions() {
	let from = $("#fromDate").val();
	let to = $("#toDate").val();
	fetch(`/api/inv/stock/all-transactions?from=${from}&to=${to}`, {
		method: "GET",
		headers: {
			"Accept": "application/json",
			"Authorization": "Bearer " + localStorage.getItem("jwtToken")
		}
	}).then(res => {
		if (!res.ok) throw new Error("API error");
		return res.json();
	})
		.then(data => {
			prepareStockTable(data);
		})
		.catch(error => {
			console.error(error);
			showMessage("Error", "Internal Server Error", false);
		});
}


function prepareStockTable(data) {

	fullStockData = mapToFields(data.stock.label, data.stock.data);

	$("#tableLoader").addClass("hidden");

	$("#stockTable thead").html(getTableHeader(tableHeader));

	if (!Array.isArray(fullStockData) || fullStockData.length === 0) {
		$("#stockTable tbody").html(emptyTableBody(8));
		return;
	}
	$("#pagination").pagination({
		dataSource: fullStockData,
		pageSize: 11,
		callback: function(pageData, pagination) {
			let startIndex = (pagination.pageNumber - 1) * pagination.pageSize;
			$("#stockTable tbody").html(buildTableBodyText(pageData, startIndex));
		}
	});
}

function buildTableBodyText(data, start, action = true) {
	let txt = "";

	data.forEach((e, i) => {
		let actBtn = (action) ? `<td class="action-col">${actionIcons(e.id, "StockRow", false)}</td>` : "";
		txt += `
			<tr>
				<td>${start + i + 1}</td>
				<td>${formatDateTime(e.transDt)}</td>
				${actBtn}
				<td>${getItemDetails(e.item, "itemGroup")}</td>
				<td>${getItemDetails(e.item, "itemChode")}  ${getItemDetails(e.item, "itemName")}</td>
				<td>${e.transTyp}</td>
				<td>${e.quantity * e.unitMul} ${getItemDetails(e.item, "baseUnit")}</td>
				<td>${e.remark}</td>
			</tr>
		`;
	});

	return txt;
}

function deleteStockRow(id) {

	openUniversalConfirmModal({
		title: "Delete Stock",
		message: "Are you sure you want to delete this stock entry?",
		actionText: "Delete",
		onConfirm: () => confirmDeleteStockRow(id)
	});
}

function confirmDeleteStockRow(id) {
	if (!id) return;

	fetch(`/api/inv/stock/${id}`, {
		method: "DELETE",
		headers: {
			"Content-Type": "application/json",
			"Authorization": "Bearer " + localStorage.getItem("jwtToken")
		}
	})
		.then(response => {
			if (!response.ok) {
				throw new Error("Failed to delete stock");
			}
			return response.text();
		})
		.then(() => {
			getCurrentStockTransactions();
		})
		.catch(error => {
			console.error(error);
			showMessage("", "Unable to delete stock. Please try again.", false);
		});
}


$('#htmlPrinttTable').click(function() {
	let tbltxt = `
        <table>
            <thead>${getTableHeader(tableHeader, false)}</thead>
            <tbody>${buildTableBodyText(fullStockData, 0, false)}</tbody>
        </table>
    `;
	let header = `
        <h3 style="text-align:center">Stock Transaction</h3>
		<h4 style="text-align:center">${$("#fromDate").val()} - ${$("#toDate").val()}</h4>
    `;
	printTableContent(tbltxt, header);
});


function getItemDetails(id, fl) {
	return fullItemData?.[id]?.[fl] ?? "";
}

// ######################################################################################

function stockModelInitialization() {
	const select = document.getElementById("stockItem");
	select.innerHTML = '<option value="">Select Item</option>';

	Object.values(fullItemData).forEach(e => {
		const opt = document.createElement("option");
		opt.value = e.id;
		opt.textContent = `${e.itemName} - ${e.itemCode}`;
		select.appendChild(opt);
	});

	if (itemListChoice) itemListChoice.destroy();

	itemListChoice = new Choices("#stockItem", {
		searchEnabled: true,
		itemSelectText: "",
		placeholderValue: "Search Item",
		searchPlaceholderValue: "Search...",
		shouldSort: false,
		classNames: {
			containerOuter: "w-full"
		}
	});
}
function openStockTransModal() {
	$("#stockTransModal").removeClass("hidden");
}

function validateStockTransactionEntry() {
	let errors = [];

	let itemId = $("#stockItem").val();
	let quantity = Number($("#quantity").val());
	let transactionUnit = $("#transactionUnit").val();

	if ($("#StockTransactionDate").val() === "") {
		errors.push("Choose a valid transaction date");
	}

	if (itemId === "") {
		errors.push("Select an item");
	}

	if (!quantity || quantity <= 0) {
		errors.push("Enter a valid quantity");
	}

	if (itemId && transactionUnit) {
		const baseUnit = fullItemData[itemId].baseUnit;

		if (!isValidUnitConversion(transactionUnit, baseUnit)) {
			const allowedUnits = getAllowedUnits(baseUnit);
			errors.push(
				`Please choose a valid unit type (${allowedUnits.join(" / ")})`
			);
		}
	}


	if (errors.length > 0) {
		showErrors(errors, ".errorAppendArea");
		return;
	}

	openUniversalConfirmModal({
		title: "Save Stock",
		message: "Do you want to save this stock entry?",
		actionText: "Save",
		onConfirm: saveStockTransactionEntry
	});
}


function saveStockTransactionEntry() {
	let itemId = toSafeNumber($("#stockItem").val());
	let baseUnit = fullItemData[itemId].baseUnit;
	let transactionUnit = $("#transactionUnit").val();
	let transactionType = $("#stockTransactionType").val();

	let stockDirection = "IN";
	if (transactionType === "SALE" || transactionType === "RETURN_OUT") {
		stockDirection = "OUT";
	}

	let unitMultiplier = resolveUnitMultiplier(
		transactionUnit,
		baseUnit
	);

	let fdata = {
		id: null,
		item: itemId,
		stockDirection: stockDirection,
		transactionType: transactionType,
		quantity: toSafeNumber($("#quantity").val()),
		transactionUnit: transactionUnit,
		unitMultiplier: unitMultiplier,
		remarks: $("#remarks").val(),
		transactionDate: $("#StockTransactionDate").val()
	};

	fetch(`/api/inv/stock`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			"Authorization": "Bearer " + localStorage.getItem("jwtToken")
		},
		body: JSON.stringify(fdata)
	})
		.then(response => {
			if (!response.ok) {
				throw new Error("Failed to save stock");
			}
			return response.text();
		})
		.then(() => {
			showMessage("", "Successfully saved stock entry", true);

		})
		.catch(error => {
			console.error(error);
			showMessage("", "Unable to save stock. Please try again.", false);
		});
}

function resolveUnitMultiplier(fromUnit, toUnit) {
	if (fromUnit === toUnit) return 1;

	const conversions = conversionUnit[fromUnit];
	if (!conversions) return 1;

	const match = conversions.find(c => c.toUnit === toUnit);
	return match ? Number(match.multiplier) : 1;
}
function isValidUnitConversion(fromUnit, toUnit) {
	if (fromUnit === toUnit) return true;

	const conversions = conversionUnit[fromUnit];
	if (!conversions) return false;

	return conversions.some(c => c.toUnit === toUnit);
}
function getAllowedUnits(baseUnit) {
	const allowed = new Set();

	allowed.add(baseUnit);

	Object.keys(conversionUnit).forEach(fromUnit => {
		const conversions = conversionUnit[fromUnit] || [];
		conversions.forEach(c => {
			if (c.toUnit === baseUnit) {
				allowed.add(fromUnit);
			}
		});
	});

	return Array.from(allowed);
}

function resetStockForm() {
	$("#stockItem").val("");
	$("#stockTransactionType").val("");
	$("#transactionUnit").val("");
	$("#quantity").val("");
	$("#remarks").val("");
	if (itemListChoice) {
		itemListChoice.setValue([]);
	}
}







