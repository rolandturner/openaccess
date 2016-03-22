// type checking functions

function checkValue(field, property, type, required) {

	if (field.value!="") {
	
		document.images[property + "required"].src="images/clearpixel.gif";
		if (type=="NUMBER" && !isNumber(field.value)) document.images[property + "required"].src="images/ast.gif";
		if (type=="DATE" && !isDate(field.value)) document.images[property + "required"].src="images/ast.gif";
		if (type=="EMAIL" && !isEmail(field.value)) document.images[property + "required"].src="images/ast.gif";
	
	} else {	
		if (required) document.images[property + "required"].src="images/ast.gif";
	}
}

// Return true if value is an e-mail address
function isEmail(value) {
	invalidChars = " /:,;";
	if (value=="") return false;
	
	for (i=0; i<invalidChars.length;i++) {
	   badChar = invalidChars.charAt(i);
	   if (value.indexOf(badChar,0) != -1) return false;
	}
	
	atPos = value.indexOf("@", 1);
	if (atPos == -1) return false;
	if (value.indexOf("@", atPos + 1) != -1) return false;
	
	periodPos = value.indexOf(".", atPos);
	if (periodPos == -1) return false;
	
	if (periodPos+3 > value.length) return false;

	return true;
}



// Return true if value is a number
function isNumber(value) {
	if (value=="") return false;

	var d = parseInt(value);
	if (!isNaN(d)) return true; else return false;		

}

// return true if value is a date
// ie in the format XX/YY/ZZ where XX YY and ZZ are numbers
function isDate(value) {
	if (value=="") return false;
	
	var pos = value.indexOf("/");
	if (pos == -1) return false;
	var d = parseInt(value.substring(0,pos));
	value = value.substring(pos+1, 999);
	pos = value.indexOf("/");
	if (pos==-1) return false;
	var m = parseInt(value.substring(0,pos));
	value = value.substring(pos+1, 999);
	var y = parseInt(value);	
	if (isNaN(d)) return false;	
	if (isNaN(m)) return false;	
	if (isNaN(y)) return false;	
	
	var type=navigator.appName;
	if (type=="Netscape") var lang = navigator.language;
	else var lang = navigator.userLanguage;
	lang = lang.substr(0,2);

	if (lang == "fr") var date = new Date(y, m-1, d);
	else var date = new Date(d, m-1, y);
	if (isNaN(date)) return false;	
	return true;
 }

// menu functions

function initMenu(menu) {
	if (getMenuCookie(menu)=="hide") document.getElementById(menu).style.display="none";
}

function changeMenu(menu) {
if (document.getElementById(menu).style.display=="none") {
	document.getElementById(menu).style.display="";
	document.getElementById(menu+"b").style.display="none";
	setMenuCookie(menu,"show");
}
else {
	var width = document.getElementById(menu).offsetWidth;
	document.getElementById(menu).style.display="none";
	if (navigator.vendor == ("Netscape6") || navigator.product == ("Gecko"))
		document.getElementById(menu+"b").style.width = width;	
	else 
		document.getElementById(menu+"b").width = width;
	document.getElementById(menu+"b").style.display="";
	setMenuCookie(menu,"hide");
}
return false;
}

function setMenuCookie(name, state) {
	var cookie = name + "STRUTSMENU=" + escape(state);
	document.cookie = cookie;
}

function getMenuCookie(name) {
	var prefix = name + "STRUTSMENU=";
	var cookieStartIndex = document.cookie.indexOf(prefix);
	if (cookieStartIndex == -1) return "???";
	var cookieEndIndex = document.cookie.indexOf(";", cookieStartIndex + prefix.length);
	if (cookieEndIndex == -1) cookieEndIndex = document.cookie.length;
	return unescape(document.cookie.substring(cookieStartIndex + prefix.length, cookieEndIndex));
}
