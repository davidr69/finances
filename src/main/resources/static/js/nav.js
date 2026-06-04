export default class Nav {
	constructor() {
		this.pathParams = [];
		this.loc = null;
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
		alert("open sesame!");
	}

	newTransaction = () => {
		let values = this.#getValues();
		window.location.href = `transaction?account=${values.account}&year=${values.year}`;
	}

	cashbook = () => {
		let values = this.#getValues();
		window.location.href = `cashbook?account=${values.account}&year=${values.year}`;
	}

	balanceSheet = () => {
		let values = this.#getValues();
		window.location.href = `reportBalanceSheet?account=${values.account}`;
	}

	updateAccount = () => {
		let values = this.#getValues();
		this.pathParams.set('account', values.account);
//		this.#reloadFrame();
	}

	updateYear = () => {
		let values = this.#getValues();
		this.pathParams.set('year', values.year);
//		this.#reloadFrame();
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
