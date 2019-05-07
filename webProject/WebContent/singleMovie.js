/**
 * 
 * @param movieData jsonObject
 */

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

function reinitializePageInfo(){
	sessionStorage.page = 0;
	
	var npp  = document.getElementById("numPerPage");
	sessionStorage.numPerPage = npp.value;
	
	sessionStorage.search_type = "search";
	sessionStorage.hidden_search_query = "none";
	
	return true;
}

function reinitializePagenumber(){
	sessionStorage.page = 0;
	
	var npp  = document.getElementById("numPerPage");
	sessionStorage.numPerPage = npp.value;
	
	return true;
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
	console.log("handleMovieResult: Populating movie table from movieData");
	
	let movieTableBodyElement = jQuery("#single_movie_table_body");
	
	for (let i = 0; i < movieData.length; i++){
		let rowHTML = "";
		rowHTML += "<tr>";
			rowHTML += "<th>" + movieData[i]["movie_id"] + "</th>";
			rowHTML += "<th>" + movieData[i]["movie_rating"] + "</th>";
			rowHTML += "<th>" + movieData[i]["movie_title"] + "</th>";
			rowHTML += "<th>" + movieData[i]["movie_year"] + "</th>";
			rowHTML += "<th>" + movieData[i]["movie_director"] + "</th>";
			rowHTML +="<th>";
			for (let g = 0; g < movieData[i]["movie_genres"].length; g++){
				rowHTML += 
					"<li>" + 
						"<a href='index.html?search_text=&search_type=genre&hidden_search_query=" + movieData[i]["movie_genres"][g]["genre_id"] + "&pagenum=0" + "&offset=" + sessionStorage.numPerPage + "\'onclick=\'return reinitializePagenumber();\'>" + 
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
			"\'" + movieData[i]["movie_title"] + '\',' + 
			movieData[i]["movie_year"] + ',' +
			"\'" + movieData[i]["movie_director"] + "\'," +
			"\'" + movieData[i]["movie_id"] + "\'" +
			');"' + 'value=\'Add\'>';
		rowHTML += '</th>';
		
		rowHTML += "</tr>";
		
		movieTableBodyElement.append(rowHTML);
	}
}

let movieId = getParameterByName('id');

jQuery.ajax({
	dataType: "json",
	method: "GET",
	url: "api/movies/single?id=" + movieId,
	success: (movieData) => handleMovieResult(movieData)
});

if (sessionStorage.isEmployee == "true"){
	document.getElementById("edashboard_link").style.visibility = "visible";
}

updateCartValue();
getGenres(); //loads genre hyperlinks
insertAlphabet(); //loads alphabet hyper links