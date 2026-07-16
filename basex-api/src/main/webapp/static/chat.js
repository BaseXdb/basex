// Browser part of the chat. The page itself comes from the server (see
// chat.xqm); this script opens a WebSocket connection and keeps the users
// list and the messages up-to-date.

// build the WebSocket address from the page address:
// http(s)://HOST/chat  ->  ws(s)://HOST/ws/chat
var href = window.location.href.replace(/^http(.*)\/chat\/?$/, "ws$1/ws/chat");
// open the connection; the protocol name must be the same
// as on the server (see %ws:subprotocol in chat-ws.xqm)
var ws = new WebSocket(href, "chat.v1");
// receiver of a private message (empty: message goes to all users)
var to = "";

// connections are kept alive by pings from the server (see chat-ws:heartbeat)

// runs whenever the server sends something; the data is a JSON
// object, and its "type" field tells us what to do
ws.onmessage = function(event) {
  var json = JSON.parse(event.data);
  if(json.type === "message") {
    // show incoming message: put it on top (newest first)
    var info = json.from + ", " + json.date;
    if(json.private) info += " (private message)";
    var message = "<div>" + json.text + "</div><div class='footnote'>" + info + "</div>";
    var msg = document.getElementById("messages");
    msg.innerHTML = message + msg.innerHTML;
  } else if(json.type === "users") {
    // redraw the users list: bold names are online; clicking a
    // name starts a private message to that user
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

// gets ready to send a private message to the clicked user
function privateMsg(user, event) {
  to = user;
  var placeholder = "Private message to " + user + " (press ESC to cancel)…";
  var input = document.getElementById("input");
  input.placeholder = placeholder;
  input.focus();
  resetInput()
  // do not follow the clicked link (it points nowhere)
  event.preventDefault();
};

// switches back from private to public messages
function resetPrivateMsg() {
  to = "";
  var placeholder = "Message to all users…";
  document.getElementById("input").placeholder = placeholder;
};

// empties the input field
function resetInput() {
  document.getElementById("input").value = "";
};

// runs when a key is pressed in the input field (see chat.xqm)
function keyDown(event) {
  if(event.keyCode === 13) { // enter: send the typed message
    event.preventDefault();
    var message = document.getElementById("input").value;
    if(message) {
      send("message", message, to);
      resetInput()
    }
  } else if(event.keyCode === 27) { // escape: cancel private message
    resetPrivateMsg();
  }
};

// sends a JSON object to the server, where chat-ws:message
// takes over (see chat-ws.xqm)
function send(type, message, to) {
  ws.send(JSON.stringify({ "type": type, "text": message, "to": to }));
};
