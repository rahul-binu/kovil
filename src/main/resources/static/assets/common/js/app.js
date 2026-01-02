function turn_on_loader() {
	$("#preloader").fadeIn(10);
	setTimeout(function() {
		$('#preloader_status').fadeIn();
		$('.preloader-text').fadeIn();
	}, 20);
}

function turn_off_loader() {
	setTimeout(function() {
		$('#preloader_status').fadeOut();
		$('.preloader-text').fadeOut();
		$('#preloader').fadeOut('slow');
	}, 10);
	$("#marqueeContenttext1").html(getDailySpiritualMessage());
	$("#marqueeContenttext2").html(getDailySpiritualMessage());

}


function showMessage(head = "Oops..", content = "Unexpectd error coccures", status = false) {
	var messageBox = $('#main-message-box');
	$('#main-message-head').text(head);
	$('#main-message-content').text(content);

	if (status)
		messageBox.css({ 'right': '20px' }).removeClass().addClass('main-success-message');
	else
		messageBox.css({ 'right': '20px' }).removeClass().addClass('main-error-message');

	setTimeout(function() {
		$('.close-main-message').trigger('click');
	}, 6500);
}

// jquery starts here
$(function() {
	turn_off_loader();
});


function openLogoutModal() {
	document.getElementById('logoutModal').classList.remove('hidden');
}

function closeLogoutModal() {
	document.getElementById('logoutModal').classList.add('hidden');
}

function confirmLogout() {
	// Remove JWT token
	localStorage.removeItem('jwtToken');
	// Redirect to login page
	window.location.href = '/web/auth/login';
}

/*
function openUniversalConfirmModal(options = {}) {
	$("#universalConfirmHead").text(options.title || "Are you sure?");
	$("#universalConfirmText").text(options.message || "You are about to perform this action.");
	$("#universalConfirmBtn").text(options.actionText || "Confirm");

	universalCallback = options.onConfirm || null;

	$("#universalConfirmModal")
		.addClass("show")
		.fadeIn(150);
}*/
function openUniversalConfirmModal(options = {}) {
    $("#universalConfirmHead").text(options.title || "Are you sure?");
    $("#universalConfirmText").text(options.message || "You are about to perform this action.");
    $("#universalConfirmBtn").text(options.actionText || "Confirm");

    // Wrap callback with args
    universalCallback = () => options.onConfirm?.(...(options.args || []));

    $("#universalConfirmModal").addClass("show").fadeIn(150);
}

// click function



// uni modal button
$(".cancelMyModalBtn").click(function() {
	$("#universalConfirmModal").removeClass("show").fadeOut(150);
});

$("#universalConfirmBtn").click(function() {
	if (universalCallback) universalCallback();
	$("#universalConfirmModal").removeClass("show").fadeOut(150);
});

$('.close-main-message').on('click', function() {
	$('#main-message-box').css({ 'right': '-300px' });
});



function getDailySpiritualMessage() {
	const day = new Date().getDay();

	const data = {
		0: {
			title: "Aadityavaram • Sunday",
			mantra: "Om Suryaya Namaha",
			affirmation: "May the radiant energy of Surya Bhagavan illuminate my path today.",
			wisdom: "Just like the rising sun removes darkness, let positivity remove all obstacles from your mind.",
			blessing: "Wishing you strength, clarity, and a bright, uplifting Sunday."
		},
		1: {
			title: "Somavaram • Monday",
			mantra: "Om Namah Shivaya",
			affirmation: "I welcome peace, calmness, and inner stability into my life.",
			wisdom: "As the Ganga flows from Shiva’s jata, let serenity flow through your thoughts today.",
			blessing: "May Lord Shiva guide you with wisdom and protect you with grace."
		},
		2: {
			title: "Mangalavaram • Tuesday",
			mantra: "Om Hanumate Namaha",
			affirmation: "I face challenges with courage, strength, and devotion.",
			wisdom: "True bravery is staying steady even when the world tests you — just like Lord Hanuman.",
			blessing: "May divine strength support you throughout this powerful Tuesday."
		},
		3: {
			title: "Budhavaram • Wednesday",
			mantra: "Om Namo Bhagavate Vasudevaya",
			affirmation: "Clarity, creativity, and intelligence flow naturally to me.",
			wisdom: "A calm mind is a sharp mind — let Krishna’s grace bring harmony to your decisions.",
			blessing: "Wishing you a thoughtful, productive, and joyful Wednesday."
		},
		4: {
			title: "Guruvaram • Thursday",
			mantra: "Om Gurave Namaha",
			affirmation: "Every moment, I grow wiser and more aligned with my purpose.",
			wisdom: "Knowledge is the only wealth that increases when shared — cherish it today.",
			blessing: "May blessings of your gurus guide you towards growth and positivity."
		},
		5: {
			title: "Shukravaram • Friday",
			mantra: "Om Shreem Mahalakshmyai Namaha",
			affirmation: "Abundance, harmony, and prosperity flow into my life effortlessly.",
			wisdom: "Gratitude turns what you have into more than enough — nurture it today.",
			blessing: "May Goddess Lakshmi bring peace, beauty, and prosperity to your Friday."
		},
		6: {
			title: "Shanivar • Saturday",
			mantra: "Om Shanicharaya Namaha",
			affirmation: "I remain patient, strong, and grounded in every situation.",
			wisdom: "Patience is the highest form of strength — trust the process like Shani Dev teaches.",
			blessing: "May discipline and divine protection guide your Saturday."
		}
	};

	const d = data[day];

	return `${d.title} • Mantra: ${d.mantra} • ${d.affirmation} • ${d.wisdom} • ${d.blessing}`;
}



// util funcation
function mapArrWithAField(a, f) {
	let ar = {};
	a.forEach(e => {
		ar[e[f]] = e;
	});
	return ar;
}

function mapArrWithAFieldToObjArr(arr, key) {
	const result = {};

	if (!Array.isArray(arr)) return result;

	arr.forEach(obj => {
		const k = obj[key];
		if (!k) return;

		if (!result[k]) {
			result[k] = [];
		}

		result[k].push(obj);
	});

	return result;
}


function mapToFields(flds, arr) {
	if (!flds || !arr || flds.length === 0 || arr.length === 0) {
		return [];
	}
	let finalData = [];
	arr.forEach(row => {
		let obj = {};
		for (let i = 0; i < flds.length; i++) {
			obj[flds[i]] = row[i] ?? null;
		}
		finalData.push(obj);
	});
	return finalData;
}

function mapToFieldsWithKey(fld, flds, arr) {
	if (!fld || !flds || !arr || flds.length === 0 || arr.length === 0) {
		return {};
	}

	let finalMap = {};

	arr.forEach(row => {
		let obj = {};
		for (let i = 0; i < flds.length; i++) {
			obj[flds[i]] = row[i] ?? null;
		}
		const key = obj[fld];
		if (key !== undefined && key !== null) {
			finalMap[key] = obj;
		}
	});

	return finalMap;
}

function mapToFieldsGrouped(keyField, labels, data) {
    const map = {};

    data.forEach(row => {
        const obj = {};
        labels.forEach((label, i) => {
            obj[label] = row[i];
        });

        const key = obj[keyField];
        if (!map[key]) {
            map[key] = [];
        }
        map[key].push(obj);
    });

    return map;
}


function toSafeNumber(value, fallback = 0) {
	if (value === null || value === undefined) return fallback;
	if (typeof value === "number" && !isNaN(value)) return value;
	const num = Number(String(value).trim().replace(/[, ]+/g, ""));
	return isNaN(num) ? fallback : num;
}
function showErrors(errors, target) {
	const container = document.querySelector(target);
	if (!container) return;

	// Clear text only
	container.innerHTML = "";

	if (!errors || errors.length === 0) return;

	// Add only error styles
	container.classList.add(
		"bg-red-100",
		"border",
		"border-red-300",
		"text-red-700",
		"p-3",
		"rounded",
		"transition-opacity",
		"duration-500"
	);

	let html = `<ul class="list-disc pl-5">`;
	errors.forEach(err => {
		html += `<li>${err}</li>`;
	});
	html += `</ul>`;

	container.innerHTML = html;

	// Auto fade & remove
	setTimeout(() => {
		container.style.opacity = "0";
		setTimeout(() => {
			container.innerHTML = "";

			container.classList.remove(
				"bg-red-100",
				"border",
				"border-red-300",
				"text-red-700",
				"p-3",
				"rounded",
				"transition-opacity",
				"duration-500"
			);

			container.style.opacity = "1";
		}, 500);
	}, 3000);
}

$(document).on("keydown", ".move-next", function(e) {
	if (e.key === "Enter") {
		e.preventDefault();
		focusNext();
	}
});


function focusNext() {
	let fields = $(".move-next");
	let active = document.activeElement;
	let index = fields.index(active);

	if (index >= 0 && index < fields.length - 1) {

		let next = fields.eq(index + 1);
		next.focus();
		if (next.is("select")) {
			let choiceInput = next.parent().find("input");
			if (choiceInput.length > 0) {
				choiceInput.focus();
			}
		}
	}
}



function actionIcons(id, rowName, isEdit = true, isDel = true) {

	return `
	<div class="action-icons" data-id="${id}">
		
		<!-- EDIT BUTTON -->
		${isEdit ? `
			<button class="action-btn edit" title="Edit" onclick="edit${rowName}('${id}')" aria-label="Edit">
				<svg width="16" height="16" viewBox="0 0 24 24" fill="none">
					<path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25z" 
						stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
					<path d="M20.71 7.04a1 1 0 0 0 0-1.41L18.37 3.29a1 1 0 0 0-1.41 0l-1.83 1.83 
						3.75 3.75 1.83-1.83z" 
						stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
				</svg>
			</button>
		` : `
		`}

		<!-- DELETE BUTTON -->
		${isDel ? `
			<button class="action-btn delete" title="Delete" onclick="delete${rowName}('${id}')" aria-label="Delete">
				<svg width="16" height="16" viewBox="0 0 24 24" fill="none">
					<path d="M3 6h18" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
					<path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" 
						stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
					<path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" 
						stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
					<path d="M10 11v6" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
					<path d="M14 11v6" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
				</svg>
			</button>
		` : `
		`}
	</div>
	`;
}

function mapToFields(flds, arr) {
	if (!flds || !arr || flds.length === 0 || arr.length === 0) {
		return [];
	}
	let finalData = [];
	arr.forEach(row => {
		let obj = {};
		for (let i = 0; i < flds.length; i++) {
			obj[flds[i]] = row[i] ?? null;
		}
		finalData.push(obj);
	});
	return finalData;
}

function mapToFieldsWithKey(fld, flds, arr) {
	if (!fld || !flds || !arr || flds.length === 0 || arr.length === 0) {
		return {};
	}

	let finalMap = {};

	arr.forEach(row => {
		let obj = {};
		for (let i = 0; i < flds.length; i++) {
			obj[flds[i]] = row[i] ?? null;
		}
		const key = obj[fld];
		if (key !== undefined && key !== null) {
			finalMap[key] = obj;
		}
	});

	return finalMap;
}

function formatDateTime(input) {
	if(input==null || input=="") return "00-00-0000 00:00:00";
    const date = new Date(input);

    let dd = String(date.getDate()).padStart(2, '0');
    let mm = String(date.getMonth() + 1).padStart(2, '0');
    let yy = String(date.getFullYear());

    let hours = date.getHours();
    let minutes = String(date.getMinutes()).padStart(2, '0');
    let ampm = hours >= 12 ? 'PM' : 'AM';

    hours = hours % 12 || 12; // convert 0 → 12 and 13-23 → 1-11
    hours = String(hours).padStart(2, '0');

    return `${dd}-${mm}-${yy} ${hours}:${minutes} ${ampm}`;
}


function formatDate(input) {
	if(input==null || input=="") return "00-00-0000";
    const date = new Date(input);

    let dd = String(date.getDate()).padStart(2, '0');
    let mm = String(date.getMonth() + 1).padStart(2, '0');
    let yy = String(date.getFullYear());

    return `${dd}-${mm}-${yy}`;
}


/*###########################################################################################################################
												Report functions
############################################################################################################################*/
function getTableHeader(head, act = true, actp = 2) {
	let txt = '<tr>';
	let i=0;
	head.forEach((e, i) => {
		if (act && i == actp) {
			txt += "<th>Actoin</th>";
		}
		txt += `<th>${e}</th>`;
	});
	if (act && i < actp && actp!= 2) {
		txt += "<th>Actoin</th>";
	}
	txt += '</tr>'
	return txt;
}

function emptyTableBody(colspan = 1) {
	return (`
	            <tr>
	                <td colspan="${colspan}" class="no-data">
					<div class="flex flex-col items-center justify-center text-center py-10 opacity-75">
					    <img src="https://cdn-icons-png.flaticon.com/512/7465/7465722.png" 
					         class="w-14 h-14 mb-3 opacity-50" />
					    <p class="text-gray-600 font-medium">No transactions found</p>
					</div>
	                </td>
	            </tr>
	        `);
}


function printTableContent(table, header) {

	$("#undocumentToPrint").remove();

	let iframe = $('<iframe>', {
		id: 'undocumentToPrint',
		style: 'display:none'
	}).appendTo('body');

	let doc = iframe[0].contentWindow.document;

	doc.open();
	doc.write(`
        <html><head><title>Print</title><style>table {width: 100%;border-collapse: collapse;}th, td {border: 1px solid #000;padding: 6px;text-align: left;}@media print {@page {margin: 10mm;}}</style></head><body>${header}${table}</body></html>`);
	doc.close();
	setTimeout(() => {
		iframe[0].contentWindow.focus();
		iframe[0].contentWindow.print();
		iframe.remove();
	}, 500);
}



