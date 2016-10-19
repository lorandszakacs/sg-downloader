function showImage(imgURL) {
	var el = document.getElementById('largeImg');
	el.src = imgURL;
	showLargeImagePanel();
  	unselectAll();
}

function showLargeImagePanel() {
    document.getElementById('largeImgPanel').style.visibility = 'visible';
}

function unselectAll() {
    if (document.selection) document.selection.empty();
    if (window.getSelection) window.getSelection().removeAllRanges();
}

function hideMe(obj) {
    obj.style.visibility = 'hidden';
}
