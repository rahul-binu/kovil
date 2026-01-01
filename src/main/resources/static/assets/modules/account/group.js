let groupData = [];
let mappedGroup = {};

$(function() {
	getGroupData();
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
			buildGrupTableData(data)
		})
		.catch(err => {
			console.log(err);
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
			$("#groupTable tbody").html(getTableText(data, start, pageSize));
		}
	});

	// Load first page by default
	$("#groupTable tbody").html(getTableText(data, 0, pageSize));

	$("#tableLoader").addClass("hidden");
}

function getTableText(d, s, l) {

	let tbl = '';

	for (let i = s; i < s + l && i < d.length; i++) {
		let act = actionIcons(d[i].id, "GroupRow", (d[i].appLock > 0), (d[i].appLock > 1));
		tbl += `
		<tr>
			<td>${i + 1}</td>
			<td>${d[i].groupName || "-"}</td>
			<td>${d[i].groupType || "-"}</td>
			<td>${d[i].orderNo || "0"}</td>
			<td>${d[i].description || "-"}</td>
			<td style="text-align:center">${act}</td>
		</tr>
		`;
	}

	return tbl;
}

function editGroupRow(id) {
	let g = mappedGroup[id];
	$("#groupName").val(g.groupName);
	$("#groupUnder").val(g.groupUnder);
	$("#groupOrder").val(g.orderNo);
	$("#groupDescription").val(g.description);
	$("#saveUpdateAccountGroup").text("Update");
	$("#saveUpdateAccountGroup").attr("data-group-id", id);
}

function deleteGroupRow(id) {
	fetch("/api/account/group/" + id, {
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
			getGroupData();
		})
		.catch(err => {
			showMessage("Ops", err.message, false);
		});
}

$("#saveUpdateAccountGroup").click(function() {
	let errors = [];
	if ($("#groupName").val().trim() == '') {
		errors.push("Enter group name");
	}
	if (errors.length > 0) {
		showErrors(errors, ".errorAppendArea");
		return;
	}

	let method = "POST";
	let groupId = null;

	if ($(this).text() != "Save") {
		method = "PUT";
		groupId = $(this).data("group-id");
	}

	let group = {
		"id": groupId,
		"groupName": $("#groupName").val(),
		"groupUnder": toSafeNumber($("#groupUnder").val()),
		"description": $("#groupDescription").val(),
		"orderNo": $("#groupOrder").val(),
		"appLock": 2,
		"groupType": $("#groupUnder option:selected").text()
	};

	fetch("/api/account/group", {
		method: method,
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		},
		body: JSON.stringify(group)
	})
		.then(async (res) => {
			const data = await res.json();

			if (!res.ok) {
				throw new Error(data.message || "Something went wrong");
			}
			return data;
		})
		.then(data => {
			showMessage("Success", "Group succesfully created/modified", true);
			$(".clear-input").val('');
			getGroupData();
		})
		.catch(err => {
			showMessage("Ops", err.message, false);
		});

	$(this).text("Save");
	$(this).attr("data-group-id", null);
});











