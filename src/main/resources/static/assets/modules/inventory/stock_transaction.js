let tableHeader = ["#", "Transaction Date", "ItemGroup", "Item Name", "Transaction Type", "Quantity", "Remarks"];

let fullItemData = {};

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
		let actBtn = (action) ? `<td>${actionIcons(e.id, "StockRow")}</td>` : "";
		txt += `
			<tr>
				<td>${start + i +1}</td>
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


function getItemDetails(id, fl) {
	return fullItemData?.[id]?.[fl]??"";
}







