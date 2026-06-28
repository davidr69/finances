export default class MergeStatement {
	haveEntities;
	stagedEntry;

	constructor(statement) {
		this.haveEntities = false;
	}

	mergeSelections = () => {
		let selections = {};
		document.querySelectorAll('input[type=radio][name^=item]:checked').forEach(el => {
			let id = el.name.substring(4);
			selections[id] = el.value;
		})

		let params = getUrlParams();

		fetch(`api/v1/statement_merge?account=${params.account}`, {
			method: 'POST',
			body: JSON.stringify(selections),
			headers: {
				'Content-Type': 'application/json'
			}
		}).then(res =>
			res.json().then(data => {
				console.log(data);
				window.location.reload();
			})
		);
	};

	#hiliteSelection = (selectedId) => {
		const sel = document.getElementById('editEntity');
		for (let option of sel.options) {
			if (option.value === String(selectedId)) {
				option.selected = true;
				break;
			}
		}
	};

	edit = (entryId, selectedId) => {
		const modal = document.getElementById('myModal');
		modal.style.display = 'grid';
		this.stagedEntry = entryId;

		if (haveEntities === true) {
			hiliteSelection(selectedId);
			return;
		}

		const sel = document.getElementById('editEntity');
		fetch('api/v1/select/merchants').then(resp => {
			resp.json().then(data => {
				for (let item of data) {
					sel.options[sel.options.length++] = new Option(item.description, item.id);
				}
				this.haveEntities = true;
				this.#hiliteSelection(selectedId);
			})
		}).catch(e => {
			alert(e);
		});
	};

	cancel = () => {
		document.getElementById('myModal').style.display = 'none';
	}

	updateRecord = () => {
		let newValue = document.getElementById('editEntity').value;
		console.log(`Update id ${stagedEntry} to entity ${newValue}`);

		fetch(`api/v1/update_staging_vendor?entity=${newValue}&id=${stagedEntry}`, {
			method: 'PUT'
		}).then(resp => {
			resp.json().then(data => {
				console.log(data);
				window.location.reload();
			})
		}).catch(e => {
			alert(e);
		});

	};
}
