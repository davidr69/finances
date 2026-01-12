export default class Transaction {
	constructor() {
		this.#init();
	}

	#init = () => {
		// [{"id":808,"account":null,"description":"1234 Gas","address":null}]
		fetch('api/v1/select/merchants').then(resp => {
			resp.json().then(data => {
				const el = document.getElementById('entity');
				el.options.length = 0;
				for(let item of data) {
					el[el.length++] = new Option(item.description, item.id);
				}
			})
		}).catch(e => {
			alert(`Coud not create list: ${e}`);
		});

		// [{"id":1,"description":"Automotive"}]
		fetch('api/v1/select/categories').then(resp => {
			resp.json().then(data => {
				const el = document.getElementById('category');
				el.options.length = 0;
				for(let item of data) {
					el[el.length++] = new Option(item.description, item.id);
				}
			})
		}).catch(e => {
			alert(`Coud not create list: ${e}`);
		});

		// [{"id":5,"description":"ATM Withdrawal","creditDebit":"-"}]
		fetch('api/v1/select/transaction_types').then(resp => {
			resp.json().then(data => {
				const el = document.getElementById('method');
				el.options.length = 0;
				for(let item of data) {
					el[el.length++] = new Option(item.description, item.id);
				}
			})
		}).catch(e => {
			alert(`Coud not create list: ${e}`);
		});
	}

	commit = () => {
		let accountDD = parent.document.getElementById('account');
		let account = accountDD[accountDD.selectedIndex].value;

		let year = parent.document.getElementById('year').value;
		let entity = getDropDownValue('entity');
		let mm = document.getElementById('mm').value;
		let dd = document.getElementById('dd').value;
		let amount = document.getElementById('amount').value;
		let method = getDropDownValue('method');
		let reference = document.getElementById('reference').value;
		let category = getDropDownValue('category');

		let data = {
			month: mm,
			day: dd,
			year: year,
			account: account,
			entity: entity,
			amount: amount,
			method: method,
			reference: reference,
			category: category
		};

		const post_headers = {
			'Content-Type': 'application/json',
			'Accept': 'application/json'
		};

		fetch('api/v1/transaction', {
			method:	'POST',
			headers: post_headers,
			body: JSON.stringify(data)
		}).then(resp => {
			resp.json().then(data => {
				console.log(data);
				if(data.code === 0) {
					document.forms[0].reset();
					document.getElementById('entity').focus();
					document.getElementById('status').innerHTML = data.message;
					fadeOut('status');
				} else {
					document.getElementById('status').innerHTML = `Error: ${data.message}`;
				}
			}).catch(e => {
				console.error(e);
				document.getElementById('status').innerHTML = e;
			});
		});
	}
}
