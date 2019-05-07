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
function handleLoginResult(resultDataString, edashboard) {
    resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);
    console.log("Edashboard = " + edashboard);
    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
    	if (edashboard != null){
    		window.location.replace("edashboard.html");
    	} else{
    		window.location.replace("index.html");
    	}
    	
    	if (resultDataJson["isemployee"] == "true"){
    		sessionStorage.isEmployee = "true";
    	} else{
    		sessionStorage.isEmployee = "false";
    	}
    } else {
        // If login fails, the web page will display 
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#login_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent, edashboard) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.post(
        "api/login",
        // Serialize the login form to the data sent by POST request
        $("#login_form").serialize() + "&edashboard=" + edashboard,
        (resultDataString) => handleLoginResult(resultDataString, edashboard)
    );
}
var edashboard = getParameterByName("edashboard");
if (edashboard != null){
	let login_title = document.getElementById("login_title");
	login_title.innerText = "Fabflix Employee Dashboard Login"
}
// Bind the submit action of the form to a handler function
$("#login_form").submit(function(event){
	//From https://stackoverflow.com/questions/31017261/require-user-to-click-googles-new-recaptcha-before-form-submission/31019293#31019293
	var recaptcha = $("#g-recaptcha-response").val();
	if (recaptcha === ""){
		event.preventDefault();
		$("#login_error_message").text("Please check the recaptcha");
	} else{
		submitLoginForm(event, edashboard)
	}
});