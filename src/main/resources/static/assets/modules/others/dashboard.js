
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

/* =======================
   Init
======================= */
console.log(3)
$(function() {
	initCharts();
	loadDashboard();
});