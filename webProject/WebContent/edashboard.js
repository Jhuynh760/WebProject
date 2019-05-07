function handleMetaDataResult(metaData){
	console.log("Inserting MetaData")
	let tableMetaDataElement = document.getElementById("table_metadata");
	let tableMetaDataHtml = "<h3 style = 'padding-left: 10px;'>Table MetaData</h3>";
		tableMetaDataHtml += "<hr>";
	
	for (let i = 0; i < metaData.length; i++){
		let tableHTML = "";
			tableHTML += "<h4 style = 'padding-left: 10px;'>" + metaData[i]["name"] + "</h4>";
			for (let col = 0; col < metaData[i]["metadata"].length; col++){
				tableHTML += "<li style='font-size:15px; padding-left: 10px;'>" +
				 			 metaData[i]["metadata"][col]["columnName"] + " - "  + metaData[i]["metadata"][col]["datatype"] + " (" + metaData[i]["metadata"][col]["datasize"] + ")" +
							 "</li>";
			}
		tableMetaDataHtml += tableHTML;
	}
	tableMetaDataHtml += "</hr>";
	
	tableMetaDataElement.innerHTML = tableMetaDataHtml;
}

function getTableMetaData(){
	jQuery.ajax({
		dataType: "json",
		method: "GET",
		url: "api/edashboard?querytype=allmetadata",
		success: (metaData) => handleMetaDataResult(metaData)
	});
}

getTableMetaData();

function handleAddStarForm(resultDataString){
	console.log("Handle Add Star Result")
	if (resultDataString[0]["status"] == "success"){
		$("#add_star_success_message").text("Successfully added star.");
		$("#add_star_error_message").text("");
	} else{
		$("#add_star_success_message").text("");
		$("#add_star_error_message").text(resultDataString[0]["errorMessage"]);
	}
}

function addStarForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();
	var star_name = document.getElementById("star_name").value;
	
	if (star_name == ""){
		console.log("Add star form submitted but star_name empty.")
		$("#add_star_success_message").text("");
		$("#add_star_error_message").text("Star Name required.");
	} else{
		console.log("submit Add Star Form");
		
	    $.post(
	        "api/edashboard",
	        // Serialize the login form to the data sent by POST request
	        $("#add_star_form").serialize() + "&querytype=addstar",
	        (resultDataString) => handleAddStarForm(resultDataString)
	    );
	}
}

function handleAddMovieForm(resultDataString, star_name, genre){
	console.log(star_name);
	console.log(genre);
	console.log("Handle Add Star Result")
	if (resultDataString[0]["status"] == "success"){
		//if status is success
		let movieCreated = resultDataString[0]["movieCreated"];
		let linked_star_to_movie = resultDataString[0]["linked_star_to_movie"];
		let linked_genre_to_movie = resultDataString[0]["linked_genre_to_movie"];
		
		if (movieCreated == 0){
			$("#add_movie_success_message").text("Movie already exists.");
		} else{
			$("#add_movie_success_message").text("Successfully added movie.");
		}
		
		if (star_name != ""){ 
			//If star is specified show message
			if (linked_star_to_movie == 0){
				$("#linked_star_to_movie_message").text("Star already linked to movie.");
			} else{
				$("#linked_star_to_movie_message").text("Successfully linked star to movie.");
			}
		} else{
			//If star is not specified don't show message
			$("#linked_star_to_movie_message").text("");
		}
		
		if (genre != ""){
			//If genre specified show message
			if (linked_genre_to_movie == 0){
				$("#linked_genre_to_movie_message").text("Genre already linked to movie.");
			} else{
				$("#linked_genre_to_movie_message").text("Successfully linked genre to movie.");
			}
		} else{
			//If genre specified don't show message
			$("#linked_genre_to_movie_message").text("");
		}
		
		$("#add_movie_error_message").text("");
	} else{
		//if status is fail
		$("#add_movie_success_message").text("");
		$("#add_movie_error_message").text(resultDataString[0]["errorMessage"]);
	}
}

function addMovieForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();
	let movie_title = document.getElementById("movie_title").value;
	let movie_year = document.getElementById("movie_year").value;
	let movie_director = document.getElementById("movie_director").value;
	let star_name = document.getElementById("link_star_to_movie").value;
	let genre = document.getElementById("link_genre_to_movie").value;
	
	if (movie_title == "" || movie_year == "" || movie_director == ""){
		console.log("Add movie form submitted but missing required field.")
		$("#add_movie_success_message").text("");
		$("#add_movie_error_message").text("Movie title, year, and director are required.");
	} else{
		console.log("submit Add Movie Form");
		
	    $.post(
	        "api/edashboard",
	        // Serialize the login form to the data sent by POST request
	        $("#add_movie_form").serialize() + "&querytype=addmovie",
	        (resultDataString) => handleAddMovieForm(resultDataString, star_name, genre)
	    );
	}
}

$("#add_star_form").submit(function(event){
		addStarForm(event)
});

$("#add_movie_form").submit(function(event){
	addMovieForm(event)
});