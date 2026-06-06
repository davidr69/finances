function getDropDownValue(obj) {
	let dropdown = document.getElementById(obj);
	return dropdown[dropdown.selectedIndex].value;
}

function fadeOut(obj) {
	let el = document.getElementById(obj);
	let interval = setInterval(function() {
		let opacity = Number(window.getComputedStyle(el).getPropertyValue('opacity'));
		if(opacity > 0) {
			opacity -= 0.05;
			el.style.opacity = opacity.toString();
		} else {
			clearInterval(interval);
			el.innerHTML = null;
			el.style.opacity = '1';
		}
	}, 100);
}

function fadeIn(obj) {
	let el = document.getElementById(obj);
	let interval = setInterval(function() {
		let opacity = Number(window.getComputedStyle(el).getPropertyValue('opacity'));
		if(opacity < 1) {
			opacity += 0.05;
			el.style.opacity = opacity.toString();
		} else {
			clearInterval(interval);
		}
	}, 25);
}

const getUrlParams = () => {
	let params = {};
	window.location.href.split('?')[1].split('&').
		forEach((item, idx) => { let vals = item.split('='); params[vals[0]] = vals[1]; });
	return params;
}
