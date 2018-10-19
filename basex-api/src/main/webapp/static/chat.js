var href = window.location.href.replace(/^http(.*)\/chat\/?$/, "ws$1/ws/chat");
var ws = new WebSocket(href);
var to = "";

ws.onopen = function(event) {
  // send regular ping to keep connection alive
  setInterval(function ping() {
    send("ping", "");
  }, 250000);
};

ws.onmessage = function(event) {
  var json = JSON.parse(event.data);
  if(json.type === "message") {
    // show incoming message
    var info = json.from + ", " + json.date;
    if(json.private) info += " (private message)";
    var message = "<div>" + json.text + "</div><div class='footnote'>" + info + "</div>";
    var msg = document.getElementById("messages");
    msg.innerHTML = message + msg.innerHTML;
  } else if(json.type === "users") {
    // refresh users list
    var users = "";
    json.users.forEach(function(user) {
      href = json.active.indexOf(user) != -1 ? "<b>" + user + "</b>" : user;
      users += "<a href='#' onclick=\"privateMsg('"+
        user.replace("'", "\\'") + "', event);\">" + href + "</a><br>";
    });
    document.getElementById("users").innerHTML = users;
  } else {
    console.log("UNKNOWN COMMAND", event);
  }
};

// helper functions

function privateMsg(user, event) {
  to = user;
  var placeholder = "Private message to " + user + " (press ESC to cancel)…";
  var input = document.getElementById("input");
  input.placeholder = placeholder;
  input.focus();
  resetInput()
  event.preventDefault();
};

function resetPrivateMsg() {
  to = "";
  var placeholder = "Message to all users…";
  document.getElementById("input").placeholder = placeholder;
};

function resetInput() {
  document.getElementById("input").value = "";
};

function keyDown(event) {
  if(event.keyCode === 13) { // enter
    event.preventDefault();
    var message = document.getElementById("input").value;
    if(message) {
      send("message", message, to);
      resetInput()
    }
  } else if(event.keyCode === 27) { // escape
    resetPrivateMsg();
  }
};

function send(type, message, to) {
  ws.send(JSON.stringify({ "type": type, "text": message, "to": to }));
};
