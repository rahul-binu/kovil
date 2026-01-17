let delayTimer = null;
let devoteeSelectedIndex = -1;
let devoteeSuggestions = [];

let nakshatraChoice = null;
let poojaMasterChoice = null;

let objPoojaMasterData;

$(function() {
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

$("#poojaMaster").on("change", function() {
	let p = objPoojaMasterData[toSafeNumber($(this).val())];
	if (p == undefined) return;
	// fields load
	$("#poojaAmount").val(p.amount);
	let year = $("#poojaDate").val() ? new Date($("#poojaDate").val()).getFullYear() : "";
	let prefix = (p.prefix ?? "@N@").replace("@Y@", year);
	$("#poojaPrefixPrefix").val(prefix);
});

$("#clearPoojaFields").click(function() {
	clearPoojaFields();
});

function clearPoojaFields() {
	$(".pooja-fields").val(0);
	poojaMasterChoice.setChoiceByValue("");
}

let choosedPoojas = [];
$("#addPoojaBtn").click(function() {
	let pooja = $("#poojaMaster").val();
	let errors = [];
	if (pooja == null || pooja == "") {
		errors.push("Choose a pooja");
	}
	if (errors.length > 0) {
		showErrors(errors, ".errorAppendArea");
		return;
	}

	let amount = toSafeNumber($("#poojaAmount").val());
	let prefix = $("#poojaPrefixPrefix").val();
	choosedPoojas.push({ pooja: pooja, amount: amount, prefix: prefix });
	chosenPoojaTable();
	clearPoojaFields();
});

function chosenPoojaTable() {
	$("#chosenPoojaDetailsTable tbody").html("");

	let totalAmount = 0;

	let tbl = "";
	choosedPoojas.forEach((e, i) => {
		tbl += `
            <tr>
                <td>${i + 1}</td>
                <td>${getPoojaMasterData(e.pooja, "name")}</td>
				<td>${e.prefix}</td>
				<td>${e.amount}</td>
                <td>
                    <i class="fa-solid fa-trash text-red-600 cursor-pointer hover:text-red-800"
                       onclick="deletePoojaRow(${i})"></i>
                </td>
            </tr>
        `;
		totalAmount += e.amount;
	});

	$("#chosenPoojaDetailsTable tbody").html(tbl);

	$("#payingAmount, #totalAmount").val(totalAmount);
}

function deletePoojaRow(i) {
	choosedPoojas.splice(i, 1);
	chosenPoojaTable();
}


$("#savePooja").click(function() {
	let errors = [];
	let fname = $("#fullName").val();
	if (fname == "") {
		errors.push("Please enter a name");
	}
	if (choosedPoojas.length == 0) {
		errors.push("Select atleast one pooja");
	}
	if (document.getElementById("poojaBooking").checked && $("#poojaBookingDate").val() == '') {
		errors.push("Select booking date");
	}
	if (toSafeNumber($("#payModeChosen").val()) == 0) {
		errors.push("Chose any paymode");
	}
	if ($("#phoneNumber").val().length != 10) {

	}

	if (errors.length > 0) {
		showErrors(errors, ".errorAppendArea");
		return;
	}
	openUniversalConfirmModal({
		title: "Save Pooja?",
		message: "Do you want to save this pooja?",
		actionText: "Save",
		onConfirm: savePooja
	});
});

function savePooja() {
	let data = {
		vendorId: $("#vendorId").val(),
		vendorName: $("#fullName").val(),
		vendorPhone: $("#phoneNumber").val(),
		vendorFamilyName: $("#familyName").val(),
		venodrAddress: $("#address").val(),
		vendorNakshatra: $("#nakshatra").val(),
		
		vendorAccountId: $("#vendorAccountId").val(),

		paymode: $("#payModeChosen").val(),

		pooja: {
			id: null,
			user: null,
			transId: null,
			devotee: $("#vendorId").val(),
			date: $("#poojaDate").val(),
			amount: toSafeNumber($("#totalAmount").val()),
			status: "ACTIVE"
		},

		bookingStatus: document.getElementById("poojaBooking").checked ? "ACTIVE" : "NONE",
		booking: document.getElementById("poojaBooking").checked,
		bookingDate: $("#poojaBookingDate").val(),
		advanceAmount: document.getElementById("poojaBooking").checked ? toSafeNumber($("#payingAmount").val()) : 0,

		poojaTrans: choosedPoojas.map(p => ({
			id: null,
			pooja: null,
			transId: null,
			poojaMaster: { "id": p.pooja },
			amount: p.amount,
			prefix: p.prefix,
			status: "ACTIVE"
		}))
	};

	console.log("Posting Offering Data:", data);

	fetch("/api/pooja/offering", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
			"Authorization": localStorage.getItem("jwtToken")
		},
		body: JSON.stringify(data)
	})
		.then(r => r.json())
		.then(res => {
			console.log("Saved:", res);
			openPrintModal(res.transId);
			// success toast or redirect
		})
		.catch(err => console.error("Error:", err));
}



$("#poojaBooking").on("change", function() {
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

function openPrintModal(tid = "pja.260104120217.96959e") {
	$("#printFrame").attr("src", `/web/pooja/receipt/0/${tid}`)
	$("#printModal").removeClass("hidden");
}

function printIframe() {
    const iframe = document.getElementById("printFrame");
    const iframeWindow = iframe.contentWindow;
    iframeWindow.focus();
    iframeWindow.onafterprint = () => {
        iframe.src = iframe.src;
    };
    iframeWindow.print();
}

function closeModal() {
	document.getElementById("printModal").classList.add("hidden");
	document.getElementById("printFrame").src = "";
	location.reload();
}





