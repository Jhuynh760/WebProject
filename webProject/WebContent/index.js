function addToCart(rating, title, year, director, movie_id){
	var cart = JSON.parse(sessionStorage.cart);
	var found = false;
	
	for (let i = 0; i < cart.length; i++){
		if (cart[i]['title'] == title){
			console.log("Incremented \'" + title + "\' quantity");
			found = true;
			console.log("\tQuantity before: " + cart[i]['quantity']);
			cart[i]['quantity'] = cart[i]['quantity'] + 1;
			console.log("\tQuantity After: " + cart[i]['quantity']);
			break;
		}
	}
	if (found == false){
		console.log("Added \'" + title + "\' to cart");
		var movie = {
				'id' : movie_id,
				'title' : title,
				'rating' : rating,
				'year' : year,
				'director' : director,
				'quantity' : 1
		};
		cart.push(movie);
	}
	sessionStorage.cart = JSON.stringify(cart);
	updateCartValue();
}

function updateCartValue(){
	var cart = JSON.parse(sessionStorage.cart);
	var sum = 0;
	for (let i = 0; i < cart.length; i++){
		sum += cart[i]['quantity'];
	}
	document.getElementById("cartButton").value = "Cart " + sum;
}


function reinitializePageInfo(){
	sessionStorage.page = 0;
	
	var npp  = document.getElementById("numPerPage");
	sessionStorage.numPerPage = npp.value;
	
	sessionStorage.search_type = "search";
	sessionStorage.hidden_search_query = "none";
	
	return true;
}

function updatePageInfo(){
	document.getElementById("pageNum").innerHTML = Number(sessionStorage.page) + 1;
	
	var npp  = document.getElementById("numPerPage");
	npp.value = sessionStorage.numPerPage;
	
	let full_text_search_query = getParameterByName("fullTextSearchBar");
	let search_query = getParameterByName("search_text");
	let search_title = getParameterByName("search_title");
	let search_year = getParameterByName("search_year");
	let search_director = getParameterByName("search_director");
	let search_star = getParameterByName("search_star");
	let sortby = getParameterByName("sortby");
	if (full_text_search_query != null){
		document.getElementById("fullTextSearchBar").value = full_text_search_query;
	}if(search_query != null){
		document.getElementById("search_text").value = search_query;
	}if(search_title != null){
		document.getElementById("search_title").checked = true;
	}if(search_year != null){
		document.getElementById("search_year").checked = true;
	}if(search_director != null){
		document.getElementById("search_director").checked = true;
	}if(search_star != null){
		document.getElementById("search_star").checked = true;
	}if (sortby != null){
		document.getElementById(sortby.toLowerCase()).checked = true;
	}
	
	updateCartValue();
	
	let search_type = document.getElementById("search_type");
	let hidden_search_query = document.getElementById("hidden_search_query");
	
	search_type.value = sessionStorage.search_type;
	hidden_search_query.value = sessionStorage.hidden_search_query;
}

function updateValues(){
	console.log("Updating tables");
	var npp  = document.getElementById("numPerPage");
	sessionStorage.numPerPage = npp.value;
	if (!sessionStorage.page){
		sessionStorage.page = 0;
	}
	$("#movie_table_body").empty();
	
	redirect();
	return true;
}

function nextPage(){
		var npp  = document.getElementById("numPerPage");
		sessionStorage.numPerPage = npp.options[npp.selectedIndex].value;
		if (sessionStorage.page){
			sessionStorage.page = Number(sessionStorage.page) + 1;
		} else{
			sessionStorage.page = 0;
		}
		console.log("Page: " + sessionStorage.page);
		document.getElementById("pageNum").innerHTML = Number(sessionStorage.page) + 1;
		$("#movie_table_body").empty();

		redirect();
		return true;
	}
function prevPage(){
	var npp  = document.getElementById("numPerPage");
	sessionStorage.numPerPage = npp.options[npp.selectedIndex].value;
	if (sessionStorage.page){
		if (Number(sessionStorage.page) > 0){
			sessionStorage.page = Number(sessionStorage.page) - 1;
		} else{
			sessionStorage.page = 0;
		}
	} else{
		sessionStorage.page = 0;
	}
	console.log("Page: " + sessionStorage.page);
	document.getElementById("pageNum").innerHTML = Number(sessionStorage.page) + 1;
	$("#movie_table_body").empty();
	
	redirect();
	return true;
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


function handleMovieResult(movieData){
	if (movieData.length == 0){
		alert("Not enough records to show on this page or nothing was found.");
	}
	console.log("handleMovieResult: Populating movie table from movieData");
	
	let movieTableBodyElement = jQuery("#movie_table_body");
	
	for (let i = 0; i < Math.min(Number(sessionStorage.numPerPage), movieData.length); i++){
		let rowHTML = "";
		rowHTML += "<tr>";
			rowHTML += "<th>" + movieData[i]["movie_rating"] + "</th>";
			rowHTML += "<th>" + '<a href="singleMovie.html?id=' + movieData[i]["movie_id"] + '">' + movieData[i]["movie_title"] + "</a></th>";
			rowHTML += "<th>" + movieData[i]["movie_year"] + "</th>";
			rowHTML += "<th>" + movieData[i]["movie_director"] + "</th>";
			rowHTML +="<th>";
			for (let g = 0; g < movieData[i]["movie_genres"].length; g++){
				rowHTML += "<li>" + 
								"<a href='index.html?search_text=&search_type=genre&hidden_search_query=" + movieData[i]["movie_genres"][g]["genre_id"] + "&pagenum=" + Number(sessionStorage.page) + "&offset=" + sessionStorage.numPerPage + "\'>" + 
									movieData[i]["movie_genres"][g]["genre"] + 
								"</a>" +
							"</li>";
			}
			rowHTML +="</th>";
			
			rowHTML +="<th>";
			for (let g = 0; g < movieData[i]["movie_stars"].length; g++){
				rowHTML += "<li>" + 
							'<a href="singleStar.html?id=' + movieData[i]["movie_stars"][g]["star_id"] + '">' +
							movieData[i]["movie_stars"][g]["star_name"] + 
							"</a>" +
							"</li>";
			}
			rowHTML +="</th>";
			
			rowHTML += '<th><input type="button" onclick="addToCart('+ 
				movieData[i]["movie_rating"] + ',' + 
				"\'" + movieData[i]["movie_title"].replace(/[^\w\s]/gi, '') + '\',' + 
				movieData[i]["movie_year"] + ',' +
				"\'" + movieData[i]["movie_director"].replace(/[^\w\s]/gi, '') + "\'," +
				"\'" + movieData[i]["movie_id"].replace(/[^\w\s]/gi, '') + "\'" +
				');"' + 'value=\'Add\'>';
			rowHTML += '</th>';
			
		rowHTML += "</tr>";
		
		movieTableBodyElement.append(rowHTML);
	}
}

function getSearchList(){
	//for search query from search bar
	let search_query = getParameterByName("search_text");
	let search_title = getParameterByName("search_title");
	let search_year = getParameterByName("search_year");
	let search_director = getParameterByName("search_director");
	let search_star = getParameterByName("search_star");
	let sortby = getParameterByName("sortby");
	let search_type = getParameterByName("search_type");
	let hidden_search_query = getParameterByName("hidden_search_query");
	
	sessionStorage.search_type = search_type;
	sessionStorage.hidden_search_query = hidden_search_query;
	
	console.log("Submitted Search request."  +
			"\n\tQuery: " + search_query +
			"\n\tPage: " + sessionStorage.page + "\tOffset: " + sessionStorage.numPerPage)
	
	jQuery.ajax({
		dataType: "json",
		method: "GET",
		url: "api/movies?searchfield=true" +
			"&search_query=" + search_query +
			"&search_title=" + search_title +
			"&search_year=" + search_year+
			"&search_director=" + search_director +
			"&search_star=" +search_star +
			"&sortby=" + sortby +
			"&pagenum=" + Number(sessionStorage.page) + "&offset=" + sessionStorage.numPerPage +
			"&search_type=" + search_type + "&hidden_search_query=" + hidden_search_query,
		success: (movieData) => handleMovieResult(movieData)
	});
}

function generalLandingRequestList(){
	console.log("Submitted general request. \n\tPage: " + sessionStorage.page + "\tOffset: " + sessionStorage.numPerPage)
	jQuery.ajax({
		dataType: "json",
		method: "GET",
		url: "api/movies?pagenum=" + Number(sessionStorage.page) + "&offset=" + Number(sessionStorage.numPerPage),
		success: (movieData) => handleMovieResult(movieData)
	});
}

function getFullTextSearchList(){
	//for search query from search bar
	let search_query = getParameterByName("fullTextSearchBar");
	let sortby = getParameterByName("sortby");
	
	sessionStorage.search_type = search_type;
	sessionStorage.hidden_search_query = hidden_search_query;
	
	console.log("Submitted Full Text Search request."  +
			"\n\tQuery: " + search_query +
			"\n\tPage: " + sessionStorage.page + "\tOffset: " + sessionStorage.numPerPage)
	
	jQuery.ajax({
		dataType: "json",
		method: "GET",
		url: "api/FullTextSearchServlet?" +
			"search_query=" + search_query +
			"&sortby=" + sortby +
			"&pagenum=" + Number(sessionStorage.page) + "&offset=" + sessionStorage.numPerPage,
		success: (movieData) => handleMovieResult(movieData)
	});
}

function redirect(){
	if (!sessionStorage.storedSuggestions){
		var storedSuggestions = [];
		sessionStorage.storedSuggestions = JSON.stringify(storedSuggestions);
	}
	if (!sessionStorage.page){
		sessionStorage.page = 0;
	} if (!sessionStorage.numPerPage){
		var npp  = document.getElementById("numPerPage");
		sessionStorage.numPerPage = npp.options[npp.selectedIndex].value;
	} if (!sessionStorage.cart){
		var newcart = [];
		sessionStorage.cart = JSON.stringify(newcart)
	} if (!sessionStorage.search_type){
		sessionStorage.search_type = "search";
	} if (!sessionStorage.hidden_search_query){
		sessionStorage.hidden_search_query = "none";
	}
	
	let sortfield = getParameterByName("sortfield");
	let search_text = getParameterByName("search_text");
	let search_type = getParameterByName("search_type");
	let fullTextSearchBar = getParameterByName("fullTextSearchBar");
	let genrequery = getParameterByName("genrequery");
	let alphabetquery = getParameterByName("alphabet");
	
	if (search_type != null && search_text != ""){
		getSearchList();
	} else if(fullTextSearchBar != null && fullTextSearchBar != ""){
		getFullTextSearchList();
	} else{
		generalLandingRequestList();
	}
}

function handleLookup(query, doneCallback) {
	console.log("autocomplete initiated")
	var found = false
	var foundData;
	var storedSuggestions = JSON.parse(sessionStorage.storedSuggestions);
	for (let i = 0; i < storedSuggestions.length; i++){
		if (storedSuggestions[i]["query"] == query){
			found = true;
			foundData = storedSuggestions[i]["data"];
		}
	}
	
	if (found == true){
		console.log("Suggestion query already search. Sending cached data.")
		doneCallback( { suggestions: foundData } );
		console.log("Suggestion data from cache: ");
		console.log(foundData);
	} else{
		console.log("sending AJAX request to backend Java Servlet for suggestions.")
		
		// TODO: if you want to check past query results first, you can do it here
		
		// sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
		// with the query data
		jQuery.ajax({
			"method": "GET",
			// generate the request url from the query.
			// escape the query string to avoid errors caused by special characters 
			"url": "api/movie-suggestion?query=" + escape(query),
			"success": function(data) {
				// pass the data, query, and doneCallback function into the success handler
				handleLookupAjaxSuccess(data, query, doneCallback) 
			},
			"error": function(errorData) {
				console.log("lookup ajax error")
				console.log(errorData)
			}
		})
	}
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 * 
 * data is the JSON data string you get from your Java Servlet
 * 
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
	console.log("lookup ajax for suggestions successful.")
	
	// parse the string into JSON
	var jsonData = JSON.parse(data);
	var storedSuggestions = JSON.parse(sessionStorage.storedSuggestions);
	var query = {
			"query" : query,
			"data" : jsonData
	};
	storedSuggestions.push(query);
	sessionStorage.storedSuggestions = JSON.stringify(storedSuggestions);
	console.log("Cached server data for suggestions.");
	console.log("Suggestion data from server: ");
	console.log(jsonData);
	
	// TODO: if you want to cache the result into a global variable you can do it here

	// call the callback function provided by the autocomplete library
	// add "{suggestions: jsonData}" to satisfy the library response format according to
	//   the "Response Format" section in documentation
	doneCallback( { suggestions: jsonData } );
}


/*
 * This function is the select suggestion handler function. 
 * When a suggestion is selected, this function is called by the library.
 * 
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
	// TODO: jump to the specific result page based on the selected suggestion
	
	window.location.href = "singleMovie.html?id=" + suggestion["data"]["movie_id"];
	//singleMovie.html?id=' + movieData[i]["movie_id"] + '">'
	console.log("Selected: " + suggestion["value"] + 
							"\n\tID: " + suggestion["data"]["movie_id"] +
							"\n\tYear: " + suggestion["data"]["movie_year"] +
							"\n\tDirector: " + suggestion["data"]["movie_director"] +
							"\n\tRating: " + suggestion["data"]["movie_rating"])
}

if (sessionStorage.isEmployee == "true"){
	document.getElementById("edashboard_link").style.visibility = "visible";
}

redirect(); //redirects to servlet with parameters

updateCartValue();

updatePageInfo(); //regardless of what page it updates page number and page offset dropdown with information in sessionStorage
getGenres(); //loads genre hyperlinks
insertAlphabet(); //loads alphabet hyper links

$('#fullTextSearchBar').autocomplete({
	// documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
    		handleLookup(query.trim(), doneCallback)
    },
    onSelect: function(suggestion) {
    		handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
    minChars: 3,
});

function handleNormalSearch(query) {
	console.log("doing normal search with query: " + query);
	// TODO: you should do normal search here
	getFullTextSearchList()
}

// bind pressing enter key to a handler function
$('#fullTextSearchBar').keypress(function(event) {
	// keyCode 13 is the enter key
	if (event.keyCode == 13) {
		// pass the value of the input box to the handler function
		handleNormalSearch($('#autocomplete').val())
	}
})