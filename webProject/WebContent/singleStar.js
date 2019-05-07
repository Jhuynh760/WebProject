/**
 * 
 * @param movieData jsonObject
 */

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
	
	sessionStorage.search_type = "search";
	sessionStorage.hidden_search_query = "none";
	
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

function handleMovieResult(starData){
	console.log("handleSingleStarResult: Populating movie table from movieData");
	
	let movieTableBodyElement = jQuery("#single_star_table_body");
	
	for (let i = 0; i < starData.length; i++){
		let rowHTML = "";
		rowHTML += "<tr>";
			rowHTML += "<th>" + starData[i]["star_id"] + "</th>";
			rowHTML += "<th>" + starData[i]["star_name"] + "</th>";
			console.log(starData[i]["star_birth_year"] == null);
			if (starData[i]["star_birth_year"] == null){
				rowHTML += "<th>N/A</th>";
			}else{
				rowHTML += "<th>" + starData[i]["star_birth_year"] + "</th>";
			}
			rowHTML +="<th>";
			for (let g = 0; g < starData[i]["movies"].length; g++){
				rowHTML += "<li>" + 
						'<a href="singleMovie.html?id=' + starData[i]["movies"][g]["movie_id"] + '">' +
						starData[i]["movies"][g]["movie_title"] + 
						"</a>" +
						"</li>";
			}
			rowHTML +="</th>";
		rowHTML += "</tr>";
		
		movieTableBodyElement.append(rowHTML);
	}
}

let movieId = getParameterByName('id');

jQuery.ajax({
	dataType: "json",
	method: "GET",
	url: "api/stars/single?id=" + movieId,
	success: (starData) => handleMovieResult(starData)
});

if (sessionStorage.isEmployee == "true"){
	document.getElementById("edashboard_link").style.visibility = "visible";
}

updateCartValue();
getGenres(); //loads genre hyperlinks
insertAlphabet(); //loads alphabet hyper links