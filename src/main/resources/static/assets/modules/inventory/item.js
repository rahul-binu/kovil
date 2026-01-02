let tableHeader = ["#", "Item Code", "Item Name", "Item Group", "baseUnit", "Item escription"];
let fullItemData = [];
$(function() {
	getAllItemData();
})


function validateItemData() {
	let errors = [];

	if ($("#itemCode").val().trim() == '') {
		errors.push("Item code cant be empty")
	}
	if ($("#itemGroup").val().trim() == '') {
		errors.push("Item group cant be empty")
	}
	if ($("#itemName").val().trim() == '') {
		errors.push("Item name cant be empty")
	}
	if ($("#baseUnit").val() == '') {
		errors.push("Choose a valid base unit")
	}

	if (errors.length > 0) {
		showErrors(errors, ".errorAppendArea");
		return;
	}
	validateItemCode()
}

async function validateItemCode() {

	if ($("#updateOrSaveItem").attr("data-up") == "1") {
		saveItemData();
		return;
	}

	const itemCode = $("#itemCode").val().trim();
	try {
		const response = await fetch(`/api/inv/item/validate-code/${itemCode}`, {
			method: "GET",
			headers: {
				"Accept": "application/json",
				"Authorization": "Bearer " + localStorage.getItem("jwtToken")
			}
		});
		if (!response.ok) {
			showMessage("Faild", "Failed to validate item code");
		}
		const result = await response.json();
		if (result.message == "true") {
			showErrors(["Item code already exists"], ".errorAppendArea");
			return;
		} else {
			saveItemData();
		}

	} catch (error) {
		showMessage("Error", "Item code validation error:", false);
	}
}
async function saveItemData() {
	const fdata = {
		id: $("#itemId").val() || null,
		itemCode: $("#itemCode").val().trim(),
		itemGroup: $("#itemGroup").val().trim(),
		itemName: $("#itemName").val().trim(),
		baseUnit: $("#baseUnit").val(),
		description: $("#description").val().trim()
	};

	try {
		const response = await fetch("/api/inv/item", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				"Accept": "application/json",
				"Authorization": "Bearer " + localStorage.getItem("jwtToken")
			},
			body: JSON.stringify(fdata)
		});
		if (!response.ok) {
			throw new Error(`Save failed: ${response.status}`);
		}
		const savedItem = await response.json();
		const index = fullItemData.findIndex(item => item.id === savedItem.id);
		if (index !== -1) {
			fullItemData[index] = savedItem;
		} else {
			fullItemData.push(savedItem);
		}
		buildTable();
		showMessage("Success", "Item saved successfully", true);
		buildOrDistroyItemFrom();
		$("#itemId").val(null);
		$("#updateOrSaveItem").text("Save").attr("data-up", "0");
	} catch (error) {
		console.error("Save item error:", error);
		showMessage("Error", "Internal server error", false);
	}

}


function buildOrDistroyItemFrom(data = {}) {
	$("#itemId").val(data.id || "");
	$("#itemCode").val(data.itemCode || "");
	$("#itemGroup").val(data.itemGroup || "");
	$("#itemName").val(data.itemName || "");
	$("#baseUnit").val(data.baseUnit || "");
	$("#description").val(data.description || "");
}
function openItemModal(data = {}) {
	buildOrDistroyItemFrom(data);
	$("#itemModal").removeClass("hidden");
}

function closeItemModal() {
	$("#itemModal").addClass("hidden");
	$("#itemId").val(null);
	$("#updateOrSaveItem").text("Save").attr("data-up", "0");
}
$("#addNewItemBtn").click(openItemModal);


async function getAllItemData() {
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

		fullItemData = await response.json();

		buildTable();


	} catch (error) {
		showMessage("Error", "Somthing went wrong", false);
	}
}

function buildTable() {
	$("#tableLoader").addClass("hidden");

	$("#vendorTable thead").html(getTableHeader(tableHeader));

	if (!Array.isArray(fullItemData) || fullItemData.length === 0) {
		$("#vendorTable tbody").html(emptyTableBody(8));
		return;
	}
	$("#pagination").pagination({
		dataSource: fullItemData,
		pageSize: 11,
		callback: function(pageData, pagination) {
			let startIndex = (pagination.pageNumber - 1) * pagination.pageSize;
			$("#vendorTable tbody").html(buildTableBodyText(pageData, startIndex));
		}
	});
}

function buildTableBodyText(data, startIndex = 0, action = true) {
	let txt = '';
	data.forEach((e, i) => {
		let act = action ?  `<td class="action-col">${actionIcons(e.id, "ItemRow")}</td>` : "";
		txt += `
			<tr>
				<td>${startIndex + i + 1}</td>
				<td>${e.itemCode}</td>
				${act}
				<td>${e.itemName}</td>
				<td>${e.itemGroup}</td>
				<td>${e.baseUnit}</td>
				<td>${e.description}</td>
			</tr>
		`;
	});

	return txt;
}

$('#htmlPrintVendor').click(function() {
	let tbltxt = `
        <table>
            <thead>${getTableHeader(tableHeader ,false)}</thead>
            <tbody>${buildTableBodyText(fullItemData, 0, false)}</tbody>
        </table>
    `;
	let header = `
        <h3 style="text-align:center">Item List</h3>
    `;
	printTableContent(tbltxt, header);
});

async function deleteItemRow(id) {

	try {
		const response = await fetch("/api/inv/item/" + id, {
			method: "DELETE",
			headers: {
				"Accept": "application/json",
				"Authorization": "Bearer " + localStorage.getItem("jwtToken")
			}
		});

		if (!response.ok) {
			showMessage("Faild", `Failed to delete the item}`, false);
		}

		fullItemData = fullItemData.filter(item => item.id !== toSafeNumber(id));
		console.log(fullItemData)
		buildTable();
	} catch (error) {
		showMessage("Error", "Somthing went wrong", false);
	}
}

function editItemRow(id) {
	let data = fullItemData.filter(item => item.id == toSafeNumber(id));
	openItemModal(data[0]);
	$("#updateOrSaveItem").text("Update").attr("data-up", "1");
	$("#itemId").val(toSafeNumber(id));
}


$("#excelExportVendor").click(function() {
	if (!fullItemData || fullItemData.length === 0) {
		alert("No data to export!");
		return;
	}

	let dataSetExcel = fullItemData.map((e, index) => [
		index + 1,
		e.itemCode,
		e.itemName,
		e.itemGroup,
		e.baseUnit,
		e.description
	]);

	const colWidths = [5, 20, 30, 20, 15, 50];

	exportToExcel({
		fileName: "item.xlsx",
		sheetName: "item",
		heading: [
			{ text: "Item List", merge: "A1:F1" }
		],
		headers: tableHeader,
		data: dataSetExcel,
		columnWidths: colWidths
	});
});













