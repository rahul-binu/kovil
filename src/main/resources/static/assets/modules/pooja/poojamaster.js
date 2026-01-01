let poojaList;
let poojaDataSet = {};
let allowedDaysChoice;
let poojaDataSetExcel = [];
$(function() {
	allowedDaysChoice = new Choices('#allowedDays', {
		removeItemButton: true,
		searchEnabled: false,
		itemSelectText: '',
		shouldSort: false,
		/*renderSelectedChoices: "always",*/
	});
	allowedDaysChoice.disable();
	getPoojaData();
});

function numberin(n) {
	const num = Number(n);
	return isNaN(num) ? 0 : num;
}


function savePooja() {
	let poojaId = (numberin($("#saveItem").attr("data-pid")) == 0) ? null : numberin($("#saveItem").attr("data-pid"));
	let pooja = {
		id: poojaId,
		name: $("#name").val(),
		groupName: $("#groupName").val(),
		description: $("#description").val(),

		amount: numberin($("#amount").val()),
		specialAmount: numberin($("#specialAmount").val()),
		variableRate: $("#variableAmount").prop("checked"),

		durationInMinutes: numberin($("#durationInMinutes").val()),

		onlyOnSpecificDays: $("input[name='onlyOnSpecificDays']:checked").val() === "true",

		allowedDays: ($("input[name='onlyOnSpecificDays']:checked").val() === "true") ? $("#allowedDays").val().toString() : "",

		requiresBookingDate: $("#requiresBookingDate").prop("checked"),
		cutoffTime: $("#cutOffTime").val(),

		requiresNakshathra: $("#requiresNakshathra").prop("checked"),
		requiresGothra: $("#requiresGothra").prop("checked"),

		tokenLimitPerDay: numberin($("#tokenLimitPerDay").val()),

		materialsList: $("#materialsList").val(),
		displayOrder: numberin($("#displayOrder").val()),
		prefix: $("#prefixPreview").text()
	};

	if (poojaId == null) {
		fetch("/api/pooja/master", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				"Authorization": localStorage.getItem("jwtToken")
			},
			body: JSON.stringify(pooja)
		})
			.then(res => res.json())
			.then(data => {

				if (data.id != null) {
					showMessage("Success", "Pooja Master saved successfully", true);
					$(".clear-input").val("");
					/*getPoojaData();*/
				} else {
					showMessage("Faild", "Failed to save Pooja", false);
				}
			})
			.catch(err => showMessage("Error", "Error while saving Pooja", false));
	}
	else {
		fetch("/api/pooja/master", {
			method: "PUT",
			headers: {
				"Content-Type": "application/json",
				"Authorization": localStorage.getItem("jwtToken")
			},
			body: JSON.stringify(pooja)
		})
			.then(res => res.json())
			.then(data => {
				if (data.id != null) {
					showMessage("Success", "Pooja Master updated successfully", true);
					$(".clear-input").val("");
					/*getPoojaData();*/
				} else {
					showMessage("Faild", "Failed to update Pooja", false);
				}
			})
			.catch(err => showMessage("Error", "Error while updating Pooja", false));
	}
}

// click functions
$("#clearItem").click(function() {
	$(".clear-input").val("");
});

$(".clear-input").on("keyup", function() {
	$(this).removeClass("border-red-500");
	$(this).next(".error-label").addClass("hidden");
});

$("input[name='onlyOnSpecificDays']").change(function() {
	if ($(this).val() === "true") {
		allowedDaysChoice.enable();
	} else {
		allowedDaysChoice.disable();

	}
});

$("#saveItem").click(function() {

	if ($("#name").val().trim() == "") {
		$("#name").addClass("border-red-500");
		$("#nameError").removeClass("hidden");
		return;
	}
	if ($("#groupName").val().trim() == "") {
		$("#groupName").addClass("border-red-500");
		$("#groupNameError").removeClass("hidden");
		return;
	}

	if (($("input[name='onlyOnSpecificDays']:checked").val() === "true") && $("#allowedDays").val().toString().trim() == "") {
		$("#allowedDays").addClass("border-red-500");
		$("#allowedDaysError").removeClass("hidden");
		return;
	} else {
		$("#allowedDaysError").addClass("hidden");
	}

	openUniversalConfirmModal({
		title: "Save Changes?",
		message: "Do you want to save this record?",
		actionText: "Save",
		onConfirm: savePooja
	});

});

$("#openPoojaModal").on("click", function() {
	$("#poojaModal").removeClass("hidden");
});

$("#closePoojaModal").on("click", function() {
	$("#poojaModal").addClass("hidden");
	window.location.reload();
});

$("#excelExportPooja").click(function() {

	exportToExcel({
		fileName: "PoojaMaster.xlsx",
		sheetName: "Pooja Masters",
		heading: [
			{ text: "Temple Pooja Master", merge: "A1:O1" }
		],
		headers: ["#",
			"Pooja Name",
			"Group Name",
			"Amount",
			"Variable Amount",
			"Special Amount",
			"Pooja Duration (Minutes)",
			"Only Specific Days?",
			"Allowed Days",
			"Requires Booking Date",
			"Cutoff Time",
			"Requires Nakshathra",
			"Requires Gothra",
			"Token Limit Per Day",
			"Materials List"],
		data: poojaDataSetExcel
	});

})

async function deletePooja(id) {
	try {
		const res = await fetch(`/api/pooja/master/${id}`, {
			method: "DELETE",
			headers: {
				"Content-Type": "application/json",
				"Authorization": localStorage.getItem("jwtToken")
			}
		});

		if (res.ok) {
			showMessage("Success", "Pooja Master deleted successfully", true);
			getPoojaData();
		} else {
			showMessage("Faild", "Failed to delete Pooja", false);
		}
	} catch (e) {
		showMessage("Error", "Error while deleting", false);
	}
}

function getPoojaData() {
	fetch("/api/pooja/master-data", {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		}
	})
		.then(res => res.json())
		.then(data => {
			buildPoojaTable(data);
		}).catch(err => console.log(err));

}


function buildPoojaTable(data) {
	let txt = '';
	data.forEach((e, i) => {
		poojaDataSet[e.id] = e;
		let a = [i + 1,
		e.name,
		e.groupName,
		e.amount,
		e.variableRate,
		e.specialAmount,
		e.durationInMinutes,
		e.onlyOnSpecificDays,
		e.allowedDays,
		e.requiresBookingDate,
		e.cutoffTime,
		e.requiresNakshathra,
		e.requiresGothra,
		e.tokenLimitPerDay,
		e.materialsList];
		poojaDataSetExcel.push(a);
		let act = actionIcons(e.id, "PoojaMasterRow");
		txt += `
				<tr>
					<td>${++i}</td>
					<td class="name">${e.name}</td>
					<td class="groupName">${e.groupName}</td>
					<td class="amount">${e.amount}</td>
					<td class="amount">${e.prefix}</td>
					<td>${e.variableRate}</td>
					<td>${e.specialAmount}</td>
					<td>${e.durationInMinutes}</td>
					<td>${e.onlyOnSpecificDays}</td>
					<td>${e.allowedDays}</td>
					<td>${e.requiresBookingDate}</td>
					<td>${e.cutoffTime}</td>
					<td>${e.requiresNakshathra}</td>
					<td>${e.requiresGothra}</td>
					<td>${e.tokenLimitPerDay}</td>
					<td>${e.materialsList}</td>
					<td>${act}</td>
				</tr>`;
	});

	$("#poojaMasterTable tbody").html(txt);

	if (window.poojaList) {
		if (poojaList.destroy) poojaList.destroy();
		else if (poojaList.remove) poojaList.remove();
	}

	poojaList = new List("poojaMasterPaginateArea", {
		valueNames: ['name', 'groupName', 'amount'],
		page: 12,
		pagination: true
	});

}

function deletePoojaMasterRow(id) {
	openUniversalConfirmModal({
		title: "Delete Record?",
		message: "This action cannot be undone.",
		actionText: "Delete",
		onConfirm: () => deletePooja(id)
	});
}

function editPoojaMasterRow(id) {
	let pooja = poojaDataSet[id];

	$("#saveItem").attr("data-pid", id);

	$("#name").val(pooja.name);
	$("#groupName").val(pooja.groupName);
	$("#description").val(pooja.description);
	$("#amount").val(pooja.amount);
	$("#specialAmount").val(pooja.specialAmount);
	$("#variableAmount").prop("checked", pooja.variableRate);

	$("#durationInMinutes").val(pooja.durationInMinutes);

	$("input[name='onlyOnSpecificDays'][value='" + pooja.onlyOnSpecificDays + "']").prop("checked", true);
	
	$("#prefixInput").val(pooja.prefix);
	$("#prefixPreview").text(pooja.prefix);

	if (pooja.onlyOnSpecificDays) {
		allowedDaysChoice.enable();
		allowedDaysChoice.setChoiceByValue(pooja.allowedDays.split(','));
	} else {
		allowedDaysChoice.clearStore();
		allowedDaysChoice.disable();
	}

	$("#requiresBookingDate").prop("checked", pooja.requiresBookingDate);
	$("#cutOffTime").val(pooja.cutoffTime);
	$("#requiresNakshathra").prop("checked", pooja.requiresNakshathra);
	$("#requiresGothra").prop("checked", pooja.requiresGothra);

	$("#tokenLimitPerDay").val(pooja.tokenLimitPerDay);
	$("#materialsList").val(pooja.materialsList);
	$("#displayOrder").val(pooja.displayOrder);


	$("#poojaModal").removeClass("hidden");
	$("#saveItem").html("Update");
}


function updatePreview() {
	let format = $("#prefixInput").val();

	$("#prefixPreview").text(format || "P/@N@");
}

// Live update while typing
$("#prefixInput").on("keyup change", updatePreview);

// Add placeholder buttons
$("#addNBtn").on("click", function () {
	insertAtCursor("prefixInput", "@N@");
	updatePreview();
});

$("#addYBtn").on("click", function () {
	insertAtCursor("prefixInput", "@Y@");
	updatePreview();
});

function insertAtCursor(id, text) {
	const input = document.getElementById(id);
	const start = input.selectionStart;
	const end = input.selectionEnd;

	const value = input.value;
	input.value = value.substring(0, start) + text + value.substring(end);

	input.focus();
	input.selectionStart = input.selectionEnd = start + text.length;
}





