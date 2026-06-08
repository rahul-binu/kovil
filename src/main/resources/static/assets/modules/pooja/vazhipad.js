let delayTimer = null;
let devoteeSelectedIndex = -1;
let devoteeSuggestions = [];

let nakshatraChoice = null;
let poojaMasterChoice = null;

let objPoojaMasterData;

$(function () {
	$("#fullName").focus();
	getAllPoojaMaster();
	getAllPoojaNakshatra();
	getPaymodes();
});

function getAllPoojaMaster() {
	fetch("/api/pooja/master-data", {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		},
	})
		.then(res => res.json())
		.then(poojas => {
			const select = document.getElementById("poojaMaster");

			objPoojaMasterData = mapArrWithAField(poojas, "id");

			poojas.forEach(p => {
				const opt = document.createElement("option");
				opt.value = p.id;
				opt.textContent = p.name;
				select.appendChild(opt);
			});

			if (poojaMasterChoice) poojaMasterChoice.destroy();
			poojaMasterChoice = new Choices("#poojaMaster", {
				searchEnabled: true,
				itemSelectText: "",
				placeholderValue: "Search Pooja/Offering",
				searchPlaceholderValue: "Search...",
				shouldSort: false,
				classNames: {
					containerOuter: "w-full"
				}
			});
		})
}


function getAllPoojaNakshatra() {
	fetch("/api/pooja/nakshathra-data", {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		},
	})
		.then(res => res.json())
		.then(nakshatras => {
			const select = document.getElementById("nakshatra");

			nakshatras.forEach(n => {
				const opt = document.createElement("option");
				opt.value = n;
				opt.textContent = n;
				select.appendChild(opt);
			});

			if (nakshatraChoice) nakshatraChoice.destroy();

			nakshatraChoice = new Choices("#nakshatra", {
				searchEnabled: true,
				itemSelectText: "",
				placeholderValue: "Search Nakshathram",
				searchPlaceholderValue: "Search...",
				shouldSort: false,
				classNames: {
					containerOuter: "w-full"
				}
			});
		});
}

const customerSearch = new AutoSuggest({
	input: "#fullName",
	resultsBox: "#customerResults",
	api: (q) => `/api/vendor/search/${q}`,
	headerKey: "Authorization",
	headerValue: localStorage.getItem("jwtToken"),
	onSelect: (devotee) => {
		selectCustomer(devotee);
	}
});

function getPaymodes() {
	fetch("/api/account/ledgers?under=11,10", {
		method: "GET",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		}
	})
		.then(response => {
			if (!response.ok) {
				throw new Error("Network response was not ok: " + response.status);
			}
			return response.json();
		})
		.then(data => {
			const select = document.getElementById("payModeChosen");

			data.forEach(n => {
				const opt = document.createElement("option");
				opt.value = n.id;
				opt.textContent = n.ledgerName;
				select.appendChild(opt);
			});
		})
		.catch(error => {
			console.error("Error fetching paymodes:", error);
		});
}



function selectCustomer(customer) {
	$("#vendorId").val(customer.transId);
	$("#vendorAccountId").val(customer.accountId);
	$("#fullName").val(customer.fullName);
	$("#phoneNumber").val(customer.mobile);
	$("#familyName").val(customer.familyName);
	$("#address").val(customer.address);

	if (customer.nakshathra && nakshatraChoice) {
		nakshatraChoice.setChoiceByValue(customer.nakshathra);
	}

	$("#customerResults").addClass("hidden");
}

$("#poojaMaster").on("change", function () {
	let p = objPoojaMasterData[toSafeNumber($(this).val())];
	if (p == undefined) return;
	// fields load
	$("#poojaAmount").val(p.amount);
	let year = $("#poojaDate").val() ? new Date($("#poojaDate").val()).getFullYear() : "";
	let prefix = (p.prefix ?? "@N@").replace("@Y@", year);
	$("#poojaPrefixPrefix").val(prefix);
});

$("#clearDevoteeFields").click(function () {
	clearDevoteeFields();
});

function clearDevoteeFields() {
	$(".devotee-fields").val("");
	if (nakshatraChoice) nakshatraChoice.setChoiceByValue("");
}

let choosedDevotees = [];
$("#addDevoteeBtn").click(function () {
	addDevoteeToList();
});

$("#addDevoteeBtn").keyup(function (e) {
	if (e.which == 13) {
		addDevoteeToList();
	}
});

function addDevoteeToList() {
	let fname = $("#fullName").val();
	let pooja = $("#poojaMaster").val();
	let errors = [];
	if (pooja == null || pooja == "") {
		errors.push("Choose a pooja first");
	}
	if (fname == null || fname == "") {
		errors.push("Enter Devotee Full Name");
	}
	if (errors.length > 0) {
		showErrors(errors, ".errorAppendArea");
		return;
	}

	let devotee = {
		vendorId: $("#vendorId").val(),
		vendorAccountId: $("#vendorAccountId").val(),
		fullName: $("#fullName").val(),
		phoneNumber: $("#phoneNumber").val(),
		familyName: $("#familyName").val(),
		address: $("#address").val(),
		nakshatra: $("#nakshatra").val()
	};

	choosedDevotees.push(devotee);
	chosenDevoteeTable();
	clearDevoteeFields();
	$("#fullName").focus();
}

function chosenDevoteeTable() {
	$("#chosenDevoteeDetailsTable tbody").html("");

	let poojaAmount = toSafeNumber($("#poojaAmount").val());
	let totalAmount = 0;

	let tbl = "";
	choosedDevotees.forEach((e, i) => {
		tbl += `
            <tr>
                <td>${i + 1}</td>
                <td>${e.fullName}</td>
				<td>${e.nakshatra}</td>
				<td>${e.phoneNumber}</td>
				<td>${poojaAmount}</td>
                <td>
                    <i class="fa-solid fa-trash text-red-600 cursor-pointer hover:text-red-800"
                       onclick="deleteDevoteeRow(${i})"></i>
                </td>
            </tr>
        `;
		totalAmount += poojaAmount;
	});

	$("#chosenDevoteeDetailsTable tbody").html(tbl);
	$("#payingAmount, #totalAmount").val(totalAmount);
}

function deleteDevoteeRow(i) {
	choosedDevotees.splice(i, 1);
	chosenDevoteeTable();
}


$("#savePooja").click(function () {
	let errors = [];
	let pooja = $("#poojaMaster").val();
	if (pooja == null || pooja == "") {
		errors.push("Choose a pooja");
	}
	if (choosedDevotees.length == 0) {
		errors.push("Add at least one devotee");
	}
	if (document.getElementById("poojaBooking").checked && $("#poojaBookingDate").val() == '') {
		errors.push("Select booking date");
	}
	if (toSafeNumber($("#payModeChosen").val()) == 0) {
		errors.push("Chose any paymode");
	}

	if (errors.length > 0) {
		showErrors(errors, ".errorAppendArea");
		return;
	}
	saveBulkPooja();
});

function saveBulkPooja() {
	let poojaMasterId = $("#poojaMaster").val();
	let poojaAmount = toSafeNumber($("#poojaAmount").val());
	let poojaPrefix = $("#poojaPrefixPrefix").val();
	let poojaDate = $("#poojaDate").val();
	let paymode = $("#payModeChosen").val();

	let totalAmountVal = toSafeNumber($("#totalAmount").val());
	let payingAmountVal = toSafeNumber($("#payingAmount").val());

	// Apportion the advance across devotees if booked
	let apportionedAdvance = payingAmountVal / choosedDevotees.length;

	let offerings = choosedDevotees.map(devotee => {
		return {
			vendorId: devotee.vendorId,
			vendorAccountId: devotee.vendorAccountId,
			vendorName: devotee.fullName,
			vendorPhone: devotee.phoneNumber,
			vendorFamilyName: devotee.familyName,
			venodrAddress: devotee.address,
			vendorNakshatra: devotee.nakshatra,

			paymode: paymode,

			pooja: {
				id: null,
				user: null,
				transId: null,
				devotee: devotee.vendorId,
				date: poojaDate,
				amount: poojaAmount,
				status: "ACTIVE"
			},

			bookingStatus: document.getElementById("poojaBooking").checked ? "ACTIVE" : "NONE",
			booking: document.getElementById("poojaBooking").checked,
			bookingDate: $("#poojaBookingDate").val(),
			advanceAmount: document.getElementById("poojaBooking").checked ? apportionedAdvance : 0,

			poojaTrans: [{
				id: null,
				pooja: null,
				transId: null,
				poojaMaster: { "id": poojaMasterId },
				amount: poojaAmount,
				prefix: poojaPrefix,
				status: "ACTIVE"
			}]
		};
	});

	console.log("Posting Bulk Offering Data:", offerings);

	fetch("/api/pooja/offering/bulk", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		},
		body: JSON.stringify(offerings)
	})
		.then(r => r.json())
		.then(res => {
			console.log("Saved:", res);
			let tids = res.map(r => r.transId).join(",");
			localStorage.setItem("lastPoojaTid", tids);
			openPrintModal(tids);
			// success toast or redirect
		})
		.catch(err => console.error("Error:", err));
}



$("#poojaBooking").on("change", function () {
	if ($(this).is(":checked")) {
		bookingEnabled();
	} else {
		bookingDisabled();
	}
});

function bookingEnabled() {
	$("#poojaBookingDate").removeClass("no-drop");
	$("#payingAmountLabel").text("Advance Amount Paying");
}

function bookingDisabled() {
	$("#poojaBookingDate").addClass("no-drop");
	$("#payingAmountLabel").text("Amount Paying");
	$("#poojaBookingDate").val('');
}

// util 
function getPoojaMasterData(id, fl) {
	return objPoojaMasterData[id]?.[fl];
}
let ptid = "";

function openPrintModal(tid = "pja.260104120217.96959e") {
	ptid = tid;
	if (true || confirm("Would you like to print using the Dot Matrix printer?\n\nClick OK for Dot Matrix.\nClick Cancel for Normal HTML Print.")) {
		fetch(`/api/print/dotmatrix/pooja/${ptid}`, {
			method: "GET",
			headers: {
				"Authorization": localStorage.getItem("jwtToken")
			}
		})
		.then(res => res.json())
		.then(data => {
			showMessage("Success", "Sent to Dot Matrix Printer", true);
			setTimeout(() => location.reload(), 1000);
		})
		.catch(err => {
			console.error("Error printing to dot matrix:", err);
			showMessage("Error", "Failed to send to Dot Matrix Printer", false);
		});
	} else {
		let iframe = document.getElementById("hiddenPrintFrame");
		if (!iframe) {
			iframe = document.createElement("iframe");
			iframe.id = "hiddenPrintFrame";
			iframe.style.display = "none";
			document.body.appendChild(iframe);
		}
		iframe.src = `/web/pooja/receipt/0/${ptid}/1`;
		iframe.onload = function() {
			iframe.contentWindow.focus();
			iframe.contentWindow.print();
			setTimeout(() => location.reload(), 1000);
		};
	}
}

$(document).ready(function() {
	let lastTid = localStorage.getItem("lastPoojaTid");
	if (lastTid) {
		$("#printPreviousPooja").removeClass("hidden");
	}

	$("#printPreviousPooja").on("click", function() {
		let tid = localStorage.getItem("lastPoojaTid");
		if (tid) {
			openPrintModal(tid);
		}
	});
});





