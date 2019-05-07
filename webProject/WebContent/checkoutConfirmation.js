function reinitializePageInfo(){
	sessionStorage.page = 0;
	
	sessionStorage.search_type = "search";
	sessionStorage.hidden_search_query = "none";
	
	return true;
}

function updateCartValue(){
	console.log("Updating Cart Button Value");
	var cart = JSON.parse(sessionStorage.cart);
	var sum = 0;
	for (let i = 0; i < cart.length; i++){
		sum += cart[i]['quantity'];
	}
	document.getElementById("cartButton").value = "Cart " + sum;
}

function handleGenreResult(genreData){
	console.log("Inserting Genre Hyperlinks")
	let genreBrowseElement = document.getElementById("genreBrowse");
	let genreHtml = "";
	let counter = 0;
	for (let i = 0; i < genreData.length; i++){
		let genre_id = genreData[i]["genre_id"];
		let genre_name = genreData[i]["genre_name"];
		genreHtml += "<a href='index.html?search_text=&search_type=genre&hidden_search_query=" + genre_id  + "&pagenum=" + Number(sessionStorage.page) + "&offset=" + sessionStorage.numPerPage + "\'>" + 
					genre_name + 
					"</a>";
		if (counter == 11){
			counter = 0;
			genreHtml += "<br>";
		} else if(i == genreData.length -1){
			break;
		} else{
			genreHtml += " || ";
			counter += 1;
		}
	}
	genreBrowseElement.innerHTML = genreHtml;
}

function getGenres(){
	jQuery.ajax({
		dataType: "json",
		method: "GET",
		url: "api/genre?genrequery=general",
		success: (genreData) => handleGenreResult(genreData)
	});
}

function genCharArray(charA, charZ) {
	//Function is from stackoverflow by User Paul S.
	// https://stackoverflow.com/questions/24597634/how-to-generate-an-array-of-alphabet-in-jquery
    var a = [], i = charA.charCodeAt(0), j = charZ.charCodeAt(0);
    for (; i <= j; ++i) {
        a.push(String.fromCharCode(i));
    }
    return a;
}

function insertAlphabet(){
	console.log("Inserting Alphabetical Hyperlinks");
	let alphabeticalBrowseElement = document.getElementById("alphabeticalBrowse");
	let alphabet = genCharArray('A', 'Z');
	let aHTML = "";
	for (let i = 0; i < alphabet.length; i++){
		aHTML += "<a href='index.html?search_text=&search_type=alphabet&hidden_search_query=" + alphabet[i] + "&pagenum=" + Number(sessionStorage.page) + "&offset=" + sessionStorage.numPerPage + "\'>" + 
		alphabet[i] +"\t</a>|\t";
	}
	aHTML += "<a href='index.html?search_text=&search_type=alphabet&hidden_search_query=etc&pagenum=" + Number(sessionStorage.page) + "&offset=" + sessionStorage.numPerPage + "\'>etc</a>";
	alphabeticalBrowseElement.innerHTML = aHTML;
}

function loadCart(){
	console.log("Loading cart data into table")
	var cart = JSON.parse(sessionStorage.cart);
	let movieTableBodyElement = jQuery("#movie_table_body");
	
	for (let i = 0; i < cart.length; i++){
		let rowHTML = "";
		rowHTML += "<tr>";
			rowHTML += "<th>" + cart[i]["id"] + "</th>";
			rowHTML += "<th>" + cart[i]["rating"] + "</th>";
			rowHTML += "<th>" + '<a href="singleMovie.html?id=' + cart[i]["id"] + '">' + cart[i]["title"] + "</a></th>";
			rowHTML += "<th>" + cart[i]["year"] + "</th>";
			rowHTML += "<th>" + cart[i]["director"] + "</th>";
			rowHTML += '<th>' + cart[i]["quantity"] + '</th>';
		rowHTML += "</tr>";
		movieTableBodyElement.append(rowHTML);
	}
}

function emptyCart(){
	sessionStorage.cart = JSON.stringify([]);
}

function getParameterByName(target){
	let url = window.location.href;
	
	target = target.replace(/[\[\]]/g, "\\$&");
	
	let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
		results = regex.exec(url);
	if (!results) return null;
	if(!results[2]) return '';
	
	return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleCheckoutResult(resultDataString) {
	let movieTableBodyElement = jQuery("#movie_table_body");
    
    console.log("handle checkout response");
    for (let i = 0; i < resultDataString.length; i ++){
    	let rowHTML = "";
    	rowHTML += "<tr>";
    		rowHTML += "<th>" + resultDataString[i]["saleId"] + "</th>";
    		rowHTML += "<th>" + resultDataString[i]["movieId"] + "</th>";
    		rowHTML += "<th>" + resultDataString[i]["movieRating"] + "</th>";
    		rowHTML += "<th>" + resultDataString[i]["movieTitle"] + "</th>";
    		rowHTML += "<th>" + resultDataString[i]["movieYear"] + "</th>";
    		rowHTML += "<th>" + resultDataString[i]["director"] + "</th>";
    	rowHTML += "</tr>";
    	movieTableBodyElement.append(rowHTML);
    }

}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitCheckoutConfirmationForm() {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    $.ajax({
    	data: { id: getParameterByName('id'),
    		cart: sessionStorage.cart},
    	dataType: 'JSON',
    	method: 'post',
    	url: 'api/sale',
    	success: handleCheckoutResult
    })
}

// Bind the submit action of the form to a handler function
submitCheckoutConfirmationForm();

emptyCart();
updateCartValue();
getGenres(); //loads genre hyperlinks
insertAlphabet(); //loads alphabet hyper links