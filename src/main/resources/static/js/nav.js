export default class Nav {
	constructor() {
		this.pathParams = new Map();
		this.loc = null;
		this.#init();
	}

	#init = () => {
		let now = new Date();
		document.getElementById('year').value = now.getFullYear();
		this.#resize();
		window.addEventListener("resize", this.#resize);
		let opt = new Option('-- all --', '0');
		let acct = document.getElementById('account');
		acct.options.add(opt, 0);
	}

	#newUrl = (path) => {
		this.pathParams.clear();
		if(!(typeof(path) == 'undefined' || path === '')) {
			let parts = path.split('?');
			this.loc = parts[0];
			if(parts.length > 1) {
				let kvp = parts[1].split('&');
				kvp.forEach(el => {
					let arr = el.split('=');
					this.pathParams.set(arr[0], arr[1]);
				});
			}
		}
	}

	#resize = () => {
		let frameHeight = window.innerHeight - 60;
		let frameWidth = window.innerWidth - 20;
		let e = document.getElementById('content');
		e.setAttribute('style', `height: ${frameHeight}px; width: ${frameWidth}px`);
	}

	show = (dest) => {
		this.#newUrl(dest);
		document.getElementById('content').src = dest;
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
		let url = `reports/byEntity?year=${values.year}&account=${values.account}`;
		this.show(url);
	}

	entityByAmount = () => {
		let values = this.#getValues();
		let url = `reports/summaryByYear?startYear=${values.year}&account=${values.account}`;
		this.show(url);
	}

	budget = () => {
		let values = this.#getValues();
		let url = `reports/weekly?year=${values.year}&account=${values.account}&month=6`;
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
