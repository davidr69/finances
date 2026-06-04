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

	show = (dest) => {
		window.location = dest;
//		this.#newUrl(dest);

//		document.getElementById('content').src = dest;
	}

	navOpen = () => {
		alert("open sesame!");
	}

	newTransaction = () => {
		let values = this.#getValues();
		window.location = `transaction?account=${values.account}&year=${values.year}`;
	}

	cashbook = () => {
		let values = this.#getValues();
		let url = `cashbook?account=${values.account}&year=${values.year}`;
		this.show(url);
	}

	balanceSheet = () => {
		let values = this.#getValues();
		let url = `reports/balance_sheet?account=${values.account}`;
		this.show(url);
	}

	updateAccount = () => {
		let values = this.#getValues();
		this.pathParams.set('account', values.account);
		this.#reloadFrame();
	}

	updateYear = () => {
		let values = this.#getValues();
		this.pathParams.set('year', values.year);
		this.#reloadFrame();
	}

	#reloadFrame = () => {
		// is the iFrame empty?
		if(document.getElementById('content').src === '') {
			return;
		}
		// reconstruct based on "loc" and "params"
		let paramsList = [];
		this.pathParams.forEach(function(v,k) { paramsList.push( k + '=' + v); });
		let newUrl = this.loc + '?' + paramsList.join('&');
		this.show(newUrl);
	}

	entityReport = () => {
		let values = this.#getValues();
		let url = `reportByEntity?year=${values.year}&account=${values.account}`;
		this.show(url);
	}

	entityByAmount = () => {
		let values = this.#getValues();
		let url = `reportSummaryByYear?startYear=${values.year}&account=${values.account}`;
		this.show(url);
	}

	budget = () => {
		let values = this.#getValues();
		let url = `reportWeekly?year=${values.year}&account=${values.account}&month=6`;
		this.show(url);
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
