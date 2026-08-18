// Browser part of the chat. The page itself comes from the server (see
// chat.xqm); this script opens a WebSocket connection to the selected room
// and keeps the users list and the messages up-to-date.

// base WebSocket address, built from the page address (without query and
// fragment, which the pattern would not match; a context path is preserved):
// http(s)://HOST[/PATH]/chat  ->  ws(s)://HOST[/PATH]/ws/chat
// the room name is appended when a room is opened (see openRoom)
var base = (location.origin + location.pathname).
  replace(/^http(.*)\/chat\/?$/, "ws$1/ws/chat");
// the open connection and the current room
var ws = null;
var room = null;
// the user whose private conversation is selected (empty: the room), and the
// store key of the conversation on display. The server states the key with
// every message and every history, so it is never built here (see chat-util.xqm)
var to = "";
var view = null;

// the logged-in user (set by the page, see chat.xqm); used to keep you from
// starting a private conversation with yourself
var me = (document.body && document.body.dataset.user) || "";

// connections are kept alive by pings from the server (see chat-ws:heartbeat)

// (re)connects to a room. Switching rooms is a fresh handshake: this is what
// lets the server bind the {$room} part of the path (see chat-ws.xqm).
function openRoom(newRoom) {
  if(room === newRoom) return;
  room = newRoom;
  // mark the active room, clear the message list, and leave any private
  // conversation; the server sends the history of the room on connect
  document.querySelectorAll(".room").forEach(function(link) {
    link.classList.toggle("active", link.dataset.room === room);
  });
  clearMessages();
  to = "";
  view = null;
  updateControls();
  // drop the old connection without reporting it as a disconnect, and ignore
  // what is still in flight on it (a history that was requested before)
  if(ws) { ws.onclose = null; ws.onmessage = null; ws.close(); }
  // offer two sub-protocols, newest first; the server picks the first one
  // it supports (see %ws:subprotocol in chat-ws.xqm)
  ws = new WebSocket(base + "/" + room, ["chat.v2", "chat.v1"]);
  ws.onopen = function() {
    // clear any earlier disconnect notice; the server's welcome fills the area.
    // the negotiated sub-protocol is a technical detail, kept in the hover text
    // (see %ws:subprotocol in chat-ws.xqm)
    var info = document.getElementById("info");
    if(info) info.title = ws.protocol ? "Connected via " + ws.protocol + "." : "";
    showInfo("", "note");
  };
  ws.onclose = function() { showInfo("Connection to the server was lost.", "warning"); };
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
    // a chat message: show it if it belongs to the conversation on display,
    // and give a hint if a private one arrives while another one is shown
    if(json.key === view) {
      addMessage(json);
    } else if(json.private) {
      showInfo('Private message from "' + json.from + '".');
    }
  } else if(json.type === "history") {
    // the stored messages of a conversation (see chat-util:history): they are
    // what the message panel shows from now on, oldest message last
    view = json.key;
    clearMessages();
    json.messages.forEach(addMessage);
  } else if(json.type === "system") {
    // a server notice (welcome, join/leave, "Who's here?"): shown in the
    // header info area, not in the message list
    showInfo(json.text);
  } else if(json.type === "users") {
    // redraw the users list: a user with rooms is online and becomes a link
    // that opens the private conversation; the others are plain text, since
    // they cannot receive messages
    var list = document.getElementById("users");
    list.innerHTML = "";
    addUsers(list, "ONLINE USERS", json.users.filter(isOnline));
    addUsers(list, "OFFLINE USERS", json.users.filter(function(user) {
      return !isOnline(user);
    }));
    updateControls();
  } else {
    console.log("UNKNOWN COMMAND", event);
  }
}

// a user is online while at least one connection of theirs is open
function isOnline(user) {
  return user.rooms.length > 0;
}

// adds one section to the users list: a heading, and a line per user. Online
// users become links, with the rooms they are in appended
function addUsers(list, label, users) {
  if(!users.length) return;
  var note = list.appendChild(document.createElement("div"));
  note.className = "note";
  note.appendChild(document.createElement("b")).textContent = label;
  users.forEach(function(user) {
    var line = list.appendChild(document.createElement("div"));
    if(!isOnline(user)) {
      line.textContent = user.name;
      return;
    }
    if(user.name === me) {
      // yourself: shown in bold, not clickable (you cannot chat with yourself)
      line.appendChild(document.createElement("b")).textContent = user.name;
    } else {
      var link = line.appendChild(document.createElement("a"));
      link.href = "#";
      link.className = "user";
      link.dataset.user = user.name;
      link.textContent = user.name;
    }
    var where = line.appendChild(document.createElement("span"));
    where.className = "footnote";
    where.textContent = " (" + user.rooms.join(", ") + ")";
  });
}

// puts a message on top of the list (newest first). The text was serialized by
// the server, so it cannot smuggle HTML into the page (see chat-util:entry)
function addMessage(message) {
  var messages = document.getElementById("messages");
  messages.innerHTML = "<div>" + message.text + "<div class='footnote'>" +
    message.from + ", " + message.date + "</div></div>" + messages.innerHTML;
}

// empties the message list
function clearMessages() {
  document.getElementById("messages").innerHTML = "";
}

// helper functions

// opens the private conversation with the given user: the server answers with
// its history, and what is typed from now on goes to that user alone
function openConversation(user) {
  to = user;
  resetInput();
  updateControls();
  send("history", "", to);
  document.getElementById("input").focus();
}

// switches back from a private conversation to the room (escape key, or a
// click on the room that is already open)
function closeConversation() {
  if(!to) return;
  to = "";
  resetInput();
  updateControls();
  send("history", "", "");
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
  } else if(event.keyCode === 27) { // escape: back to the room
    closeConversation();
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

// keeps the page in step with the selected conversation: the input hint and the
// panel heading name it, the users list marks the selected user, and the Send
// button is disabled while the input field is empty
function updateControls() {
  var input = document.getElementById("input");
  input.placeholder = to ? "Private message to " + to + "…" : "Message to the room…";
  var button = document.getElementById("send");
  if(button) button.disabled = !input.value;
  document.querySelectorAll(".user").forEach(function(link) {
    link.classList.toggle("active", link.dataset.user === to);
  });
  var active = document.querySelector(".room.active");
  document.getElementById("conversation").textContent =
    " · " + (to ? "Private chat with " + to : active ? active.textContent : "");
}

// asks the server for statistics (see chat-ws:info)
function serverInfo() {
  send("info", "", "");
}

// sends a JSON object to the server, where chat-ws:message
// takes over (see chat-ws.xqm)
function send(type, message, user) {
  if(ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({ "type": type, "text": message, "to": user }));
  }
}

// wire up the room links and open the first room once the page is ready
// (this script is loaded from the <head>, before the links exist)
window.addEventListener("DOMContentLoaded", function() {
  // enable the Send button only while there is something to send
  document.getElementById("input").addEventListener("input", updateControls);

  document.querySelectorAll(".room").forEach(function(link) {
    link.addEventListener("click", function(event) {
      event.preventDefault();
      // the room that is already open: leave the private conversation, if any
      if(link.dataset.room === room) closeConversation();
      else openRoom(link.dataset.room);
    });
  });
  // the users list is redrawn whenever someone joins or leaves, so the click
  // is caught on the panel, which stays
  document.getElementById("users").addEventListener("click", function(event) {
    var link = event.target.closest("a.user");
    if(link) {
      event.preventDefault();
      openConversation(link.dataset.user);
    }
  });
  openRoom(document.querySelector(".room").dataset.room);
});
