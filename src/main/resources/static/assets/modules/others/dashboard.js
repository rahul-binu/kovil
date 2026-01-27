
/* =======================
   Utils
======================= */

function parseChartData(rawData) {
	return {
		labels: rawData.map(r => r[0]),
		values: rawData.map(r => r[1])
	};
}

function monthName(monthNo) {
	return new Date(2024, monthNo - 1, 1)
		.toLocaleString('en-IN', { month: 'short' });
}

/* =======================
   Chart Instances
======================= */

let poojaChart, devoteeChart, vazhipadChart;

function initCharts() {
	poojaChart = new Chart($('#poojaChart')[0], {
		type: 'bar',
		data: { labels: [], datasets: [] },
		options: {
			responsive: true,
			scales: { y: { beginAtZero: true } }
		}
	});

	devoteeChart = new Chart($('#devoteeChart')[0], {
		type: 'line',
		data: { labels: [], datasets: [] },
		options: { responsive: true }
	});

	vazhipadChart = new Chart($('#vazhipadChart')[0], {
		type: 'doughnut',
		data: { labels: [], datasets: [] },
		options: { responsive: true }
	});
}

/* =======================
   Bind Functions
======================= */

function bindPoojaChart(apiData) {
	const parsed = parseChartData(apiData.data);

	poojaChart.data.labels = parsed.labels.map(monthName);
	poojaChart.data.datasets = [{
		label: 'Poojas',
		data: parsed.values,
		backgroundColor: 'rgba(59,30,138,0.7)',
		borderRadius: 6
	}];

	poojaChart.update();
}

function bindDevoteeChart(apiData) {
	const parsed = parseChartData(apiData.data);

	devoteeChart.data.labels = parsed.labels.map(monthName);
	devoteeChart.data.datasets = [{
		label: 'Vendors',
		data: parsed.values,
		fill: true,
		tension: 0.3,
		backgroundColor: 'rgba(90,53,196,0.2)',
		borderColor: 'rgba(90,53,196,1)'
	}];

	devoteeChart.update();
}

function bindVazhipadChart(apiData) {
	const parsed = parseChartData(apiData.data);

	vazhipadChart.data.labels = parsed.labels;
	vazhipadChart.data.datasets = [{
		label: 'Poojas',
		data: parsed.values,
		backgroundColor: [
			'rgba(59,30,138,0.7)',
			'rgba(90,53,196,0.7)',
			'rgba(155,102,250,0.7)'
		],
		borderColor: '#fff',
		borderWidth: 2
	}];

	vazhipadChart.update();
}

/* =======================
   API
======================= */

function loadDashboard() {
	$.ajax({
		url: '/api/dashboard',
		method: 'GET',
		headers: {
			"Content-Type": "application/json",
			"Authorization": "Bearer " + localStorage.getItem("jwtToken")
		},
		success: function(dashboard) {
			bindPoojaChart(dashboard.ypooja);
			bindDevoteeChart(dashboard.yvend);
			bindVazhipadChart(dashboard.tpooja);
		},
		error: function(err) {
			console.error('Dashboard API failed', err);
		}
	});
}


function loadBooking() {
	let date = $("#poojaBookingDate").val();
	$.ajax({
		url: '/api/dashboard/day/pbooking',
		method: 'GET',
		headers: {
			"Content-Type": "application/json",
			"Authorization": "Bearer " + localStorage.getItem("jwtToken")
		},
		data: {
			date: date
		},
		success: function(res) {
			buildPoojaBookingList(res);
		},
		error: function(err) {
			console.error('Dashboard API failed', err);
		}
	});
}

function buildPoojaBookingList(data) {
	const tbody = document.querySelector("tbody");
	tbody.innerHTML = "";

	const devotees = Object.fromEntries(
		data.v.map(d => [d.transId, d])
	);

	const transactionsByTransId = {};
	data.pt.forEach(t => {
		if (!transactionsByTransId[t.transId]) {
			transactionsByTransId[t.transId] = [];
		}
		transactionsByTransId[t.transId].push(t);
	});

	data.pj.forEach(pj => {
		const devotee = devotees[pj.devotee];
		if (!devotee) return;

		const tr = document.createElement("tr");
		tr.innerHTML = `
            <td class="px-3 py-2">${devotee.fullName}</td>
            <td class="px-3 py-2">${devotee.mobile}</td>
            <td class="px-3 py-2 text-center">
                <button
                  class="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded-md"
                  onclick='openPoojaModal(
                    ${JSON.stringify(pj)},
                    ${JSON.stringify(devotee)},
                    ${JSON.stringify(transactionsByTransId[pj.transId] || [])}
                  )'>
                  View
                </button>
            </td>
        `;
		tbody.appendChild(tr);
	});
}
function openPoojaModal(pj, v, pts) {
	const modal = document.getElementById("poojaModal");
	const content = document.getElementById("modalContent");

	let ptRows = pts.map(pt => `
        <tr class="border-b">
          <td class="py-1">${pt.poojaMaster.name}</td>
          <td class="py-1 text-right">₹${pt.amount}</td>
        </tr>
    `).join("");

	content.innerHTML = `
        <div><strong>Devotee:</strong> ${v.fullName}</div>
        <div><strong>Mobile:</strong> ${v.mobile}</div>
        <div><strong>Booking Date:</strong> ${pj.bookingDate}</div>
        <div><strong>Advance:</strong> ₹${pj.advanceAmount}</div>

        <div class="mt-3">
          <table class="w-full text-sm border">
            <thead class="bg-gray-100">
              <tr>
                <th class="text-left px-2 py-1">Pooja</th>
                <th class="text-right px-2 py-1">Amount</th>
              </tr>
            </thead>
            <tbody>
              ${ptRows}
            </tbody>
          </table>
        </div>

        <div class="text-right font-semibold mt-2">
          Total: ₹${pj.amount}
        </div>
    `;

	modal.classList.remove("hidden");
	modal.classList.add("flex");
}
function closePoojaModal() {
	const modal = document.getElementById("poojaModal");
	modal.classList.add("hidden");
	modal.classList.remove("flex");
}

/* =======================
   Init
======================= */
$(function() {
	initCharts();
	loadDashboard();
	loadBooking();
});