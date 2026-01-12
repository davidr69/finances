import CustomModal from './modal.js';

export default class Cashbook {
	constructor() {
		this.originalIncludes = Array.from(document.querySelectorAll('input[name^=visi]')).map(function(obj) { return obj.checked; });
		this.cache = {};
		this.editRecord = 0;
		this.ajaxCalls = 0;

		this.originalRecord = null;

		this.editAccount = document.getElementById('editAccount');
		this.editEntity = document.getElementById('editEntity');
		this.editMethod = document.getElementById('editMethod');
		this.editCategory = document.getElementById('editCategory');

		document.getElementsByName('month')[0].selectedIndex = document.getElementById('selectedMonth').value;

		this.done = new Event('done');
		this.ajaxDone = document.createElement('a');
		this.ajaxDone.addEventListener('done', () => {
			this.#modalCounter();
		});

		// Initialize custom modal
		this.modal = new CustomModal('myModal');
	}

	#getDetails = () => {
		fetch(`api/v1/transaction/${this.editRecord}`).then(resp => {
			resp.json().then(data => {
				this.originalRecord = data;
				/*
                    {
                        "amount":"-34.23",
                        "mydate":"2017-12-15",
                        "reference":null,
                        "entity":546,
                        "method":11,
                        "category":null,
                        "sequence":20495}
                */
                const selectedEntity = Array.from(this.editEntity.options).find(obj => obj.value === String(data.entity));
                if (selectedEntity) {
                    selectedEntity.selected = true;
                }

                const selectedMethod = Array.from(this.editMethod.options).find(obj => obj.value === String(data.method));
                if (selectedMethod) {
                    selectedMethod.selected = true;
                }

                const selectedAccount = Array.from(this.editAccount.options).find(obj => obj.value === String(data.account));
                if (selectedAccount) {
                    selectedAccount.selected = true;
                }

				const date = new Date(data.mydate);
				document.getElementById('editMonth').value = date.getUTCMonth() + 1;
				document.getElementById('editDay').value = date.getUTCDate();
				document.getElementById('editYear').value = date.getUTCFullYear();
				document.getElementById('editAmount').value = data.amount;

				if(data.reference == null) {
					document.getElementById('editReference').value = '';
				} else {
					document.getElementById('editReference').value = data.reference;
				}

				if(data.category != null) {
                    const selectedCategory = Array.from(this.editCategory.options).find(obj => obj.value === String(data.category));
                    if (selectedCategory) {
                        selectedCategory.selected = true;
                    }
				}

			}).catch(e => {
				console.log(e);
			});
		});
	}

	#modalCounter = () => {
		if(++this.ajaxCalls === 4) {
			this.ajaxCalls = 0;
			this.#getDetails();
		}
	};

	changeMonth = (month) => {
		let accountDD = parent.document.getElementById('account');
		let account = accountDD[accountDD.selectedIndex].value;

		let year = parent.document.getElementById('year').value;
		window.location = `cashbook?account=${account}&month=${month}&year=${year}`;
	};

	include = () => {
		let added = [];
		let removed = [];

		// diff original against dirty records
		Array.from(document.querySelectorAll('input[name^=visi]')).forEach((item, idx) => {
			if(item.checked !== this.originalIncludes[idx]) {
				if(item.checked === true) { // new is checked, old is unchecked, so added
					added.push(item.name.substring(4));
				} else {
					removed.push(item.name.substring(4));
				}
			}
		});

		let params = JSON.stringify({add:added,remove:removed});
		fetch('api/v1/transaction/include', {
			method: 'PUT',
			body: params,
			headers: {
				'Content-Type': 'application/json'
			}
		}).then(resp => {
			resp.json().then(() => {
				location.reload();
			}).catch(e => {
				console.log(e);
			});
		});
	};

	reconcile = () => {
		const nodeList = Array.from(document.querySelectorAll('input[name^=reconcile]'))
		const reconcile = nodeList.filter(function(obj) { if(obj.checked) return obj }).map(function(obj) { return obj.name.substring(9) })
		const params = JSON.stringify({entries: reconcile});

		fetch('api/v1/transaction/reconcile', {
			method:		'PUT',
			body:		params,
			headers:	{
				'Content-Type': 'application/json'
			}
		}).then(() => {
			document.forms[0].reset();	// this hack fixes the issue that shouldn't exist
			location.reload();
		}).catch(e => {
			console.log(e);
		});
	};

	modify = (record) => {
		this.editRecord = record;

		this.#getMerchants();
		this.#getTransactionTypes();
		this.#getAccounts();
		this.#getCategories();

		// Open the modal
		this.modal.open();
	};

	#getMerchants = () => {
		if('merchants' in this.cache) {
			this.ajaxDone.dispatchEvent(this.done);
		} else {
			this.cache['merchants'] = true;
			fetch('api/v1/select/merchants').then(resp => {
				resp.json().then(data => {
					for(let item of data) {
						this.editEntity.options[this.editEntity.options.length++] = new Option(item.description, item.id);
					}
					this.ajaxDone.dispatchEvent(this.done);
				})
			}).catch(e => {
				this.ajaxDone.dispatchEvent(this.done);
				alert(e);
			});
		}
	}

	#getTransactionTypes = () => {
		if('transactions' in this.cache) {
			this.ajaxDone.dispatchEvent(this.done);
		} else {
			this.cache['transactions'] = true;
			fetch('api/v1/select/transaction_types').then(resp => {
				resp.json().then(data => {
					for(let item of data) {
						this.editMethod.options[this.editMethod.options.length++] = new Option(item.description, item.id);
					}
					this.ajaxDone.dispatchEvent(this.done);
				})
			}).catch(e => {
				this.ajaxDone.dispatchEvent(this.done);
				alert(e);
			});
		}
	}

	#getAccounts = () => {
		if('accounts' in this.cache) {
			this.ajaxDone.dispatchEvent(this.done);
		} else {
			this.cache['accounts'] = true;
			fetch('api/v1/select/accounts').then(resp => {
				resp.json().then(data => {
					for(let item of data) {
						this.editAccount.options[this.editAccount.options.length++] = new Option(item.description, item.id);
					}
					this.ajaxDone.dispatchEvent(this.done);
				})
			}).catch(e => {
				this.ajaxDone.dispatchEvent(this.done);
				alert(e);
			});
		}
	}

	#getCategories = () => {
		if('categories' in this.cache) {
			this.ajaxDone.dispatchEvent(this.done);
		} else {
			this.cache['categories'] = true;
			fetch('api/v1/select/categories').then(resp => {
				resp.json().then(data => {
					this.editCategory.options[this.editCategory.options.length++] = new Option("", "");
					for(let item of data) {
						this.editCategory.options[this.editCategory.options.length++] = new Option(item.description, item.id);
					}
					this.ajaxDone.dispatchEvent(this.done);
				})
			}).catch(e => {
				this.ajaxDone.dispatchEvent(this.done);
				alert(e);
			});
		}
	}

	updateRecord = () => {
		let params = {};
		const saved = this.originalRecord;

		params['amount'] = document.getElementById('editAmount').value;
		params['entity'] = getDropDownValue('editEntity');
		params['method'] = getDropDownValue('editMethod');
		params['account'] = getDropDownValue('editAccount');
		params['category'] = getDropDownValue('editCategory');
		params['reference'] = document.getElementById('editReference').value;
		params['sequence'] = saved.sequence
		params['visible'] = saved.visible

		const mon = document.getElementById('editMonth').value;
		const day = document.getElementById('editDay').value;
		const year = document.getElementById('editYear').value;
		params['mydate'] = new Date(year, mon - 1, day);

		fetch('api/v1/transaction', {
			method: 'PUT',
			body: JSON.stringify(params),
			headers: {
				'Content-Type': 'application/json'
			}
		}).then(resp => {
			resp.json().then(data => {
				if(data.code === 0) {
					this.modal.close();
					location.reload();
				} else {
					alert(`error: ${data.message}`);
				}
			});
		}).catch(e => {
			alert(`error: ${e}`);
		});
	};

	confirm_del = () => {
		if(confirm("Do you really want to remove this?")) {
			fetch(`api/v1/transaction/${this.originalRecord.sequence}`, {
				method: 'DELETE'
			}).then(() => {
				this.modal.close();
				location.reload();
			}).catch(e => {
				alert(`error: ${e}`);
			});
		}
	};
}
