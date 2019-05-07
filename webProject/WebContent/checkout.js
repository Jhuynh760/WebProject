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
    resultDataJson = JSON.parse(resultDataString);

    console.log("handle checkout response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
    	window.location.replace("checkoutConfirmation.html?id=" + resultDataJson["id"]);
    } else {
        // If login fails, the web page will display 
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#checkoutErrorMessage").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitCheckoutForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.post(
        "api/checkout",
        // Serialize the login form to the data sent by POST request
        $("#checkout_form").serialize(),
        (resultDataString) => handleCheckoutResult(resultDataString)
    );
}

// Bind the submit action of the form to a handler function
$("#checkout_form").submit((event) => submitCheckoutForm(event));

updateCartValue();
getGenres(); //loads genre hyperlinks
insertAlphabet(); //loads alphabet hyper links