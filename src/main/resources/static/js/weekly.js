/*
	Build a structure as follows, with the index representing the week:

	[
		{
			'Shoprite': 12.38,
			'Target': 56.78
		}, {
			'Western Beef': 45.93,
			'Costco': 98.76
		}
	]
*/
const oneDay = 1000 * 60 * 60 * 24;

export default class Weekly {
	constructor(date) {
		this.totals = [];
		this.totals.length = 6;
		this.sum = 0;

		this.init(date);
	}

	init = (date) => {
		/*
            1. The following things need to be established:
            - the weekday of the 1st day
            - the last day of the month
            - the weekday of the last day

            2. Once we have this, we have to:
            - determine if there are any days before the first day which belong to the week
            - determine if there are any days past the last day of the month which belong to that week

            3. If there are any days prior to the first, find out what that Sunday equals to.
            Do the reciprocal with the last day.

            With all that information, we can then make an ajax call requesting transactions
            from the first Sunday to the last Saturday.
        */

		// Step 1
		let year = parent.document.getElementById('year').value;

		let now = new Date(year, date.getMonth(), date.getDate());
		let first = new Date(year, now.getMonth(), 1, 0, 0, 0);
		let last = new Date(new Date(year, now.getMonth() + 1, 1) - oneDay);

		// Step 2
		let padFirst = first.getDay();
		let padLast = 7 - last.getDay() - 1;

		// Step 3
		let currentMonth = now.getMonth();
		this.firstSunday = new Date(first - padFirst * oneDay);
		let lastSaturday = new Date(
			currentMonth === 12 ? year + 1 : year,
			currentMonth === 12 ? 1 : currentMonth + 1,
			padLast
		);

		let accountDD = parent.document.getElementById('account');
		let account = accountDD[accountDD.selectedIndex].value;

		fetch(`../api/v1/transaction/list?account=${account}&beginDate=${this.firstSunday.toJSON().substring(0,10)}&endDate=${lastSaturday.toJSON().substring(0,10)}`).then(resp => {
			resp.json().then(data => {
				if(data.code === 0) {
					this.initCalendar(now, last, padFirst);
					for(let item of data.transactions) {
						this.placeData(item);
					}
				} else {
					alert(data.message);
				}
			});
		});

	}

	initCalendar = (now, last, padFirst) => {
		document.getElementById('month').options[now.getMonth()].selected = true;
		// draw left pad
		let row = 1;
		let col = 1;
		for(; col <= padFirst; col++) {
			let id = `row1col${col}`;
			let val = this.firstSunday.getDate() + col - 1;
			document.getElementById(id).innerHTML = `${val}<br/>`;
		}
		// draw days for this month
		for(let i = 1; i <= last.getDate(); i++) {
			let id = `row${row}col${col++}`;
			document.getElementById(id).innerHTML = `${i}<br/>`;
			if(col > 7) {
				col = 1;
				row++;
			}
		}
		// fill in the rest of the calendar
		let day = 1;
		while(row <= 6) {
			while(col <= 7) {
				let id = `row${row}col${col++}`;
				document.getElementById(id).innerHTML = `${day++}<br/>`;
			}
			row++;
			col = 1;
		}
	}

	placeData = (item) => {
		let delta = Math.floor((new Date(`${item.mydate}T00:00:00`) - this.firstSunday) / oneDay);
		let weekOffset = Math.floor(delta / 7);
		let dayOffset = delta - weekOffset * 7;

		let theId = `row${weekOffset + 1}col${dayOffset + 1}`;
		let el = document.getElementById(theId);
		el.innerHTML += `${item.entity}: ${item.amount}<br/>`;
		/*
            amount: "-35.62"
            method: "Debit card"
            mydate: "2021-10-31"
            entity: "Shoprite"
            reconciled: true
            reference: null
            runningTotal: "-35.62"
            sequence: 27362
            visible: true
        */

		// update totals
		if(this.totals[weekOffset] == null) {
			this.totals[weekOffset] = {};
		}
		let current = 0;
		if(item.entity in this.totals[weekOffset]) {
			current = this.totals[weekOffset][item.entity];
		}
		this.totals[weekOffset][item.entity] = Math.round((item.amount + current) * 100) / 100;
	}

	changeMonth = () => {
		let newMon = document.getElementById('month').selectedIndex;
		let year = parent.document.getElementById('year').value;
		let newDate = new Date(year, newMon + 1, 0, 0, 0, 0);

		for(let row = 1; row <= 6; row++) {
			for(let col = 1; col <= 7; col++) {
				let id = `row${row}col${col}`;
				document.getElementById(id).innerHTML = "";
			}
		}

		this.totals = [];
		this.totals.length = 6;
		this.sum = 0;
		this.init(newDate);
	}

	showTotals = (radio) => {
		let idx = Number(radio.value);
		let html = '';
		this.sum = 0;
		Object.keys(this.totals[idx]).sort().forEach((key) => {
			let val = this.totals[idx][key];
			html += `<input type="checkbox" value="${val}" onclick='weeklyApi.modSum(this)'> ${key}: ${val}<br/>`;
		});
		html += 'Total: <span style="font-weight: 700" id="sum"></span>'
		document.getElementById('totals').innerHTML = html;
	}

	modSum = (obj) => {
		let val = Number(obj.value);
		if(obj.checked) {
			this.sum -= val;
		} else {
			this.sum += val;
		}
		document.getElementById('sum').innerHTML = (Math.round(this.sum * 100) / 100).toString();
	}
}
