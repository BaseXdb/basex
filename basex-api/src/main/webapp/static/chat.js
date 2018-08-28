var href = window.location.href.replace(/^http(.*)\/chat\/?$/, "ws$1/ws/chat");
var ws = new WebSocket(href);

ws.onclose = function(event) {
  console.log("BYEBYE");
  send("users", "");
};

ws.onopen = function(event) {
  send("users", "");
};

ws.onmessage = function(event) {
  var json = JSON.parse(event.data);
  if(json.type === "message") {
    var message = "<div>" + json.text + "</div>" +
      "<div class='footnote'>" + json.user + ", " + json.date + "</div>";
    var msg = document.getElementById("messages");
    msg.innerHTML = message + msg.innerHTML;
  } else if(json.type === "users") {
    document.getElementById("users").innerHTML = json.users.join("<br>");
  } else {
    console.log("UNKNOWN COMMAND", event);
  }
};

function keyDown(event) {
  if(event.keyCode === 13) {
    var input = document.getElementById("input");
    if(input.value) {
      send("message", input.value);
      input.value = "";
    }
    event.preventDefault();
  }
};

function send(type, message) {
  ws.send(JSON.stringify({ "type": type, "text": message }));
};
