// Browser part of the chat. The page itself comes from the server (see
// chat.xqm); this script opens a WebSocket connection to the selected room
// and keeps the users list and the messages up-to-date.

// base WebSocket address, built from the page address:
// http(s)://HOST/chat  ->  ws(s)://HOST/ws/chat
// the room name is appended when a room is opened (see openRoom)
var base = window.location.href.replace(/^http(.*)\/chat\/?$/, "ws$1/ws/chat");
// the open connection, the current room, and the private-message receiver
// (empty: the message goes to the whole room)
var ws = null;
var room = null;
var to = "";

// the logged-in user (set by the page, see chat.xqm); used to keep you from
// starting a private message to yourself
var me = (document.body && document.body.dataset.user) || "";

// connections are kept alive by pings from the server (see chat-ws:heartbeat)

// (re)connects to a room. Switching rooms is a fresh handshake: this is what
// lets the server bind the {$room} part of the path (see chat-ws.xqm).
function openRoom(newRoom) {
  if(room === newRoom) return;
  room = newRoom;
  // reflect the active room in the buttons, clear the message list, leave
  // any private-message mode
  document.querySelectorAll(".room").forEach(function(button) {
    button.classList.toggle("active", button.dataset.room === room);
  });
  document.getElementById("messages").innerHTML = "";
  resetPrivateMsg();
  // drop the old connection without reporting it as a disconnect
  if(ws) { ws.onclose = null; ws.close(); }
  // offer two sub-protocols, newest first; the server picks the first one
  // it supports (see %ws:subprotocol in chat-ws.xqm)
  ws = new WebSocket(base + "/" + room, ["chat.v2", "chat.v1"]);
  ws.onopen = function() {
    // clear any earlier "offline" notice; the server's welcome fills the area.
    // the negotiated sub-protocol is a technical detail, kept in the hover text
    // (see %ws:subprotocol in chat-ws.xqm)
    var info = document.getElementById("info");
    if(info) info.title = ws.protocol ? "connected via " + ws.protocol : "";
    showInfo("", "note");
  };
  ws.onclose = function() { showInfo("offline", "warning"); };
  ws.onmessage = onMessage;
}

// shows a server notice in the info area next to the room names (like the
// status info in the DBA). Server notices are transient status, not chat
// content, so they live here rather than in the message list.
function showInfo(text, cls) {
  var info = document.getElementById("info");
  if(!info) return;
  info.textContent = text;
  info.className = cls || "note";
}

// runs whenever the server sends something; the data is a JSON object,
// and its "type" field tells us what to do
function onMessage(event) {
  var json = JSON.parse(event.data);
  if(json.type === "message") {
    // a chat message: show sender and time (all messages in view belong to
    // the current room), and mark private ones
    var info = json.from + ", " + json.date + (json.private ? " (private)" : "");
    addMessage(json.text, info);
  } else if(json.type === "system") {
    // a server notice (welcome, join/leave, "Who's here?"): shown in the
    // header info area, not in the message list
    showInfo(json.text);
  } else if(json.type === "users") {
    // redraw the users list: online users (with their rooms) are links you can
    // click to start a private message; offline users are plain text, since
    // they cannot receive messages
    var rooms = {};
    json.active.forEach(function(user) { rooms[user.name] = user.rooms; });
    var online = "", offline = "";
    json.users.forEach(function(user) {
      if(Object.prototype.hasOwnProperty.call(rooms, user)) {
        var where = rooms[user].length ?
          " <span class='footnote'>(" + rooms[user].join(", ") + ")</span>" : "";
        if(user === me) {
          // yourself: shown in bold, not clickable (you can't message yourself)
          online += "<b>" + user + "</b>" + where + "<br>";
        } else {
          online += "<a href='#' onclick=\"privateMsg('" +
            user.replace("'", "\\'") + "', event);\">" + user + "</a>" + where + "<br>";
        }
      } else {
        offline += user + "<br>";
      }
    });
    var html = "";
    if(online) html += "<div class='note'><b>ONLINE USERS</b></div>" + online;
    if(offline) html += (html ? "<div class='small'></div>" : "") +
      "<div class='note'><b>OFFLINE USERS</b></div>" + offline;
    document.getElementById("users").innerHTML = html;
  } else {
    console.log("UNKNOWN COMMAND", event);
  }
}

// puts a message on top of the list (newest first)
function addMessage(html, info) {
  var div = "<div>" + html + "<div class='footnote'>" + info + "</div></div>";
  var messages = document.getElementById("messages");
  messages.innerHTML = div + messages.innerHTML;
}

// helper functions

// gets ready to send a private message to the clicked user
function privateMsg(user, event) {
  to = user;
  var placeholder = "Private message to " + user + "…";
  var input = document.getElementById("input");
  input.placeholder = placeholder;
  input.focus();
  resetInput();
  updateControls();
  // do not follow the clicked link (it points nowhere)
  event.preventDefault();
}

// switches back from private to public messages
function resetPrivateMsg() {
  to = "";
  document.getElementById("input").placeholder = "Message to the room…";
  updateControls();
}

// empties the input field
function resetInput() {
  document.getElementById("input").value = "";
}

// runs when a key is pressed in the input field (see chat.xqm)
function keyDown(event) {
  if(event.keyCode === 13) { // enter: send the typed message
    event.preventDefault();
    sendInput();
  } else if(event.keyCode === 27) { // escape: cancel private message
    resetPrivateMsg();
  }
}

// sends whatever is currently typed (from the Send button or the Enter key)
function sendInput() {
  var input = document.getElementById("input");
  if(input.value) {
    send("message", input.value, to);
    resetInput();
    updateControls();
  }
  input.focus();
}

// keeps the toolbar controls in step with the current state: the Send button
// label switches between "Send" (room) and "Send private" (recipient selected)
// and is disabled while the field is empty; the cancel button appears only in
// private mode, so leaving it needs no keyboard
function updateControls() {
  var button = document.getElementById("send");
  if(button) {
    button.textContent = to ? "Send private" : "Send";
    button.disabled = !document.getElementById("input").value;
  }
  var cancel = document.getElementById("cancel");
  if(cancel) cancel.hidden = !to;
}

// asks the server for statistics (see chat-ws:info)
function serverInfo() {
  ws.send(JSON.stringify({ "type": "info" }));
}

// sends a JSON object to the server, where chat-ws:message
// takes over (see chat-ws.xqm)
function send(type, message, to) {
  ws.send(JSON.stringify({ "type": type, "text": message, "to": to }));
}

// wire up the room buttons and open the first room once the page is ready
// (this script is loaded from the <head>, before the buttons exist)
window.addEventListener("DOMContentLoaded", function() {
  // enable the Send button only while there is something to send
  document.getElementById("input").addEventListener("input", updateControls);

  document.querySelectorAll(".room").forEach(function(link) {
    link.addEventListener("click", function(event) {
      event.preventDefault();
      openRoom(link.dataset.room);
    });
  });
  openRoom(document.querySelector(".room").dataset.room);
});
