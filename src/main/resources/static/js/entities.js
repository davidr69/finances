export default class Entities {
	validate = false;
	changedSet = new Set();

	constructor() {
		this.#init();
	}

	deleteEntity = (eId) => {
		fetch(`/api/v1/entities/${eId}`, {
			method: 'DELETE'
		}).then(resp => {
			resp.json().then(data => {
				if(data.code === 0) {
					window.location = window.location.href;
				}
				alert(data.message);
			});
		}).catch((error) => {
			console.error('Error occurred while deleting entity:', error);
		});
	}

	validateEntity = (id, el) => {
		if(this.validate === false) {
			this.validate = true;

			// publishing a Kafka message is asynchronous, so don't need a promise or any type of synchronous "workflow"
			fetch(`/api/v1/entities/accept/${id}`, {
				method: 'PUT'
			}).catch((error) => {
				console.error('Error occurred while validating entity:', error);
			});

			let klass = el.getAttribute('class');
			el.setAttribute('class', 'fa fa-spinner fa-pulse small-glyph fa-fw');
			setInterval(() => {
				console.log('Polling for validation...');

				fetch(`api/v1/entities/${id}`).then(resp => {
					if(resp != null) {
						resp.json().then(data => {
							console.log(data.validated);
							if(data.validated) {
								window.location = window.location.href;
							}
						});
					}
				}).catch((error) => {
					console.error('Error occurred while polling entity validation:', error);
					el.setAttribute('class', 'fa fa-exclamation-triangle small-glyph');
				});
			}, 5000);
		} else {
			console.log('validation in progress...');
		}
	}

	#init = () => {
		// add "onclick" to "delete" and "validate" glyphs
		document.querySelectorAll('span[data-id]').forEach((el) => {
			let buttons = el.querySelectorAll('i');

			buttons[0].addEventListener('click', () => {
				this.deleteEntity(el.getAttribute('data-id'));
			});

			buttons[1].addEventListener('click', () => {
				this.validateEntity(el.getAttribute('data-id'), buttons[1]);
			});
		});

		// don't show a "save" icon until the RAG field has changed
		document.querySelectorAll('input[data-rag]').forEach((el) => {
			el.addEventListener('keyup', () => {
				let num = el.getAttribute('data-rag');
				if (!this.changedSet.has(num)) {
					let i = document.getElementById(`rag${num}`);
					i.setAttribute('class', 'fa fa-save small-glyph');
					i.addEventListener('click', () => {
						let rag = document.getElementById(`rag${num}`);
						i.setAttribute('class', 'fa fa-spinner fa-pulse small-glyph fa-fw');

						let value = document.getElementById(`input${num}`).value;
						fetch(`api/v1/entities/${num}`, {
							method: 'PATCH',
							headers: {'Content-Type': 'application/json'},
							body: JSON.stringify({rag: value})
						}).then((resp) => {
							resp.json().then(data => {
								if(data.code === 0) {
									console.log('RAG updated successfully');
									i.removeAttribute('class');
									this.changedSet.delete(num);
								} else {
									console.error('Error occurred while updating RAG');
									i.setAttribute('class', 'fa fa-save small-glyph');
								}
							})
						}).catch(e => {
							console.error('Error occurred while updating RAG', e);
							i.setAttribute('class', 'fa fa-exclamation-triangle small-glyph');
						});
					});
					this.changedSet.add(num);
				}
			})
		});
	}
}
