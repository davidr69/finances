const months = [
	null, 'January', 'February', 'March', 'April', 'May', 'June',
	'July', 'August', 'September', 'October', 'November', 'December'
];

const formatter = new Intl.NumberFormat('en-US', {
	style: 'currency',
	currency: 'USD',
	minimumFractionDigits: 2
});

export default class BalanceSheet {
	constructor(account) {
		this.#init(account);
	}

	#init = (account) => {
		fetch(`../api/v1/reports/balanceSheet?account=${account}`).then(resp => {
			resp.json().then(data => {
				if(data.code === 0) {
					this.#render(data.balanceList);
				} else {
					alert(data.message);
				}
			});
		});

	}

	#render = (data) => {
		/**
		 *
		 * Data looks like:
		 *
		 {
		 "2001": [
		 {
		 "month": 2,
		 "credits": 6883.6,
		 "debits": -1458.62,
		 "diff": 5424.98,
		 "balance": 5424.98
		 },
		 {
		 "month": 3,
		 "credits": 4831.16,
		 "debits": -5598.13,
		 "diff": -766.97,
		 "balance": 4658.01
		 },

		 */
		let years = Object.keys(data);

		let table = document.getElementById('content');

		years.forEach(function (year) {
			let monthCount = data[year].length;
			let tr = document.createElement('tr');
			let td = document.createElement('td');

			td.setAttribute('rowspan', monthCount);
			td.setAttribute('class', 'vert_year');
			td.innerHTML = `${year}`.split('').join('<br/>');

			tr.appendChild(td);

			data[year].forEach(function(tuple, idx) {
				if(idx > 0) {
					tr = document.createElement('tr');
				}

				td = document.createElement('td');
				let text = document.createTextNode(months[tuple.month]);
				td.appendChild(text);
				tr.appendChild(td);

				td = document.createElement('td');
				td.setAttribute('class', 'right');
				text = document.createTextNode(formatter.format(tuple.credits));
				td.appendChild(text);
				tr.appendChild(td);

				td = document.createElement('td');
				td.setAttribute('class', 'right');
				text = document.createTextNode(formatter.format(tuple.debits));
				td.appendChild(text);
				tr.appendChild(td);

				td = document.createElement('td');
				if(tuple.diff < 0) {
					td.setAttribute('class', 'negative');
				} else {
					td.setAttribute('class', 'positive');
				}
				text = document.createTextNode(formatter.format(tuple.diff));
				td.appendChild(text);
				tr.appendChild(td);

				td = document.createElement('td');
				if(tuple.balance < 0) {
					td.setAttribute('class', 'negative');
				} else {
					td.setAttribute('class', 'positive');
				}
				text = document.createTextNode(formatter.format(tuple.balance));
				td.appendChild(text);
				tr.appendChild(td);

				table.appendChild(tr);
			});
		});
		table.style.display = 'inline';
	}
}
