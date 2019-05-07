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

function updateMovieEntry(id, quantity){
	console.log("Updating movie entry: " + id);
	var cart = JSON.parse(sessionStorage.cart);
	for (let i = 0; i < cart.length; i++){
		if (id == cart[i]["id"]){
			if (quantity == 0){
				console.log("\tQuantity is 0. Deleting Entry.");
				cart.splice(i, 1);
			} else{
				console.log("\tQuantity is: " + quantity);
				cart[i]["quantity"] = quantity;
			}
		}
	}
	sessionStorage.cart = JSON.stringify(cart);
}

function updateCart(){
	console.log("Updating Cart");
	var errorOccurred = false;
	
	var movie_table_body = document.getElementById('movie_table_body');
	for (let i = 0, row; row = movie_table_body.rows[i]; i++){
		var movie_id = row.cells[0].innerHTML;
		var quantity = parseInt(row.cells[5].children[0].value)
		if (quantity < 0){
			errorOccurred = true;
		} else{
			updateMovieEntry(movie_id, quantity);
		}
	}
	if (errorOccurred == true){
		$("#cartErrorMessage").text("Invalid quantity input selected. Must be greater than or equal to 0.");
	} else{
		$("#cartErrorMessage").text("");
	}
	
	updateCartValue();
	
	$("#movie_table_body").empty();

	loadCart();
}

function deleteFromCart(id){
	console.log("Deleting from Cart: " + id);
	var cart = JSON.parse(sessionStorage.cart);
	for (let i = 0; i < cart.length; i++){
		if (id == cart[i]["id"]){
			cart.splice(i, 1);
		}
	}
	sessionStorage.cart = JSON.stringify(cart);
	updateCartValue();
	
	$("#movie_table_body").empty();

	loadCart();
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
			rowHTML += "<th><input type = \'number\' value = " + cart[i]["quantity"] + " name = \'quantity\' size = \'1\' min=\'0\'></th>";
			rowHTML += '<th>' +
							'<input type="button" onclick= "deleteFromCart(\'' +
							cart[i]['id'] + '\');"' + 'value="Delete">' +  
					   "</th>";
		rowHTML += "</tr>";
		movieTableBodyElement.append(rowHTML);
	}
}

loadCart();

updateCartValue();
getGenres(); //loads genre hyperlinks
insertAlphabet(); //loads alphabet hyper links