function sendTrialData(coreUrl, datasetName) {
	var http = new XMLHttpRequest();
	var url = "http://localhost:8000"; //URL of interface between core and solver. 
	
	var params = "url=" + coreUrl + "&datasetName=" + datasetName; 
	
	http.open("POST", url, true);

	//Setting up headers for HTTP POST.
	http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	//http.setRequestHeader("Content-length", params.length);
	//http.setRequestHeader("Connection", "close");

	http.onreadystatechange = function() {//Call a function when the state changes.
		if(http.readyState == 4 && http.status == 200) {
			alert(http.responseText);
		}
	}
	http.send(params);
}