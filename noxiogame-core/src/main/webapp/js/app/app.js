"use strict";

var element = document.getElementById("info");

var showInfo = function(data) {
  element.innerHTML = "<b>SERVER NAME : " + data.name + "</b></br>";
  element.innerHTML += "LOCATION : " + data.location + "</br>";
  element.innerHTML += "ADDRESS : " + data.address + "</br></br>";
  element.innerHTML += "<b>USERS ONLINE : " + data.users.length + "</b></br>";
  for(var i=0, j=0;i<data.users.length;i++) { element.innerHTML += data.users[i] + "</br>"; }
  element.innerHTML += "</br></br><b>LOBBEIS : " + data.lobbies.length + "</br>";
  for(var i=0;i<data.lobbies.length;i++) {
    element.innerHTML += data.lobbies[i] + "</br>";
    element.innerHTML += "</br>";
  }
};

var error = function() {
  element.innerHTML = "ERROR TRYING TO RETRIEVE INFO FROM SERVER!";
};

var getInfo = function() {
  $.ajax({
    url: "/nxg/advinfo",
    type: 'GET',
    timeout: 3000,
    success: function(data) { showInfo(data); },
    error: function() { error(); }
  });
};

getInfo();