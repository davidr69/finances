export default class Nav {
	constructor() {
		this.pathParams = [];
		this.#init();
	}

	#init = () => {
		this.pathParams = Object.fromEntries(new URLSearchParams(window.location.search));

		const yearEl = document.getElementById('year');
		if(typeof(this.pathParams['year']) === 'undefined') {
			let now = new Date();
			yearEl.value = now.getFullYear();
			this.pathParams['year'] = yearEl.value;
		} else {
			yearEl.value = this.pathParams['year'];
		}

		let opt = new Option('-- all --', '0');
		let acct = document.getElementById('account');
		acct.options.add(opt, 0);

		document.getElementById('account').value = this.pathParams['account'];
	}

	navOpen = () => {
		const el = document.getElementById('popup');
		el.style.display = 'block';

		setTimeout(()=>{
			el.style.display = 'none';
		}, 5000);
	}

	newTransaction = () => {
		let values = this.#getValues();
		window.location.href = `transaction?account=${values.account}&year=${values.year}`;
	}

	cashbook = () => {
		let values = this.#getValues();
		window.location.href = `cashbook?account=${values.account}&year=${values.year}`;
	}

	entities = () => {
		window.location.href = "./entities?" + Object.entries(this.pathParams).map(([k, v]) => `${k}=${v}`).join('&');
	}

	balanceSheet = () => {
		let values = this.#getValues();
		window.location.href = `reportBalanceSheet?account=${values.account}`;
	}

	updateAccount = () => {
		let values = this.#getValues();
		this.pathParams['account'] = values.account;
		window.location.href = window.location.href.split('?')[0] + '?' + Object.entries(this.pathParams).map(([k, v]) => `${k}=${v}`).join('&');
	}

	updateYear = () => {
		let values = this.#getValues();
		this.pathParams['year'] = values.year;
		window.location.href = window.location.href.split('?')[0] + '?' + Object.entries(this.pathParams).map(([k, v]) => `${k}=${v}`).join('&');
	}

	entityReport = () => {
		let values = this.#getValues();
		window.location.href = `reportByEntity?year=${values.year}&account=${values.account}`;
	}

	entityByAmount = () => {
		let values = this.#getValues();
		window.location.href = `reportSummaryByYear?startYear=${values.year}&account=${values.account}`;
	}

	budget = () => {
		let values = this.#getValues();
		window.location.href = `reportWeekly?year=${values.year}&account=${values.account}&month=6`;
	}

	logout = () => {
		window.location.href = 'logout';
	};

	/**
	 * Returns {year:year, account:account}
	 */
	#getValues = () => {
		let year = document.getElementById('year').value;
		let obj = document.getElementById('account');
		let account = obj[obj.selectedIndex].value;
		return {year:year, account:account};
	}
}
