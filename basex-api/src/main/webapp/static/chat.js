var href = window.location.href.replace(/^http(.*)\/chat\/?$/, "ws$1/ws/chat");
var ws = new WebSocket(href);
let privatemsg = false;
let receiver = "";

ws.onclose = function(event) {
  console.log("BYEBYE");
  send("users", "");
};

ws.onopen = function(event) {
  send("users", "");
};

ws.onmessage = function(event) {
  var json = JSON.parse(event.data);
  if(json.type === "message-global") {
    var message = "<div>" + json.text + "</div>" +
      "<div class='footnote'>" + json.user + ", " + json.date + "</div>";
    var msg = document.getElementById("messages");
    msg.innerHTML = message + msg.innerHTML;
  } else if(json.type === "message-private") {
     var message = "<div class='private'>" + json.text + "</div>" +
      "<div class='footnote'>" + json.user + ", " + json.date + "</div>";
    var msg = document.getElementById("messages");
    msg.innerHTML = message + msg.innerHTML;
  } else if(json.type === "users") {
    let users = "";
    json.users.forEach( 
      (user) => {
        users = users + "<p style='cursor: pointer;' onclick='privateMsg(\""+ user + "\")'>" + user + "</p>"; 
      }
    );
    document.getElementById("users").innerHTML = users;
  } else {
    console.log("UNKNOWN COMMAND", event);
  }
};

function privateMsg(pReceiver) {
  privatemsg = true;
  receiver = pReceiver;
  document.getElementById("input").placeholder = "Message to " + pReceiver;
};

function resetPrivateMsg() {
  receiver = "";
        privatemsg = false;
        document.getElementById("input").placeholder = "Write your Message...";
};

function keyDown(event) {
  if(event.keyCode === 13) {
    var input = document.getElementById("input");
    if(input.value) {
      if(privatemsg) {
        send("message-private", input.value, receiver);
        resetPrivateMsg()
      } else {
        send("message-global", input.value);
      }
      input.value = "";
    }
    event.preventDefault();
  } else if(event.keyCode === 27) {
    resetPrivateMsg();
  }
};

function send(type, message, receiver) {
  ws.send(JSON.stringify({ "type": type, "text": message, "receiver": receiver }));
};
