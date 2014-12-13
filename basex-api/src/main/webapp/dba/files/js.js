function buttons() {
  var forms = document.getElementsByTagName("form");
  for(var f = 0; f < forms.length; f++) {
    var form = forms[f];
    if(form.className != 'update') continue;

    var inputs = form.getElementsByTagName("input");
    var c = 0;
    for(var i = 0; i < inputs.length; i++) {
      if(inputs[i].type == "checkbox" && inputs[i].checked) c++;
    }
    var buttons = form.getElementsByTagName("button");
    for(var i = 0; i < buttons.length; i++) {
      var button = buttons[i], n = button.className, s = button.value, e = !button.disabled;
      if(n == 'global') continue;

      if(s == "optimize" || s == "optimize-all" || s == "drop-backup" ||
         s == "drop-db" || s == "drop-pattern" || s == "drop-user" ||
         s == "kill-session" || s == "restore" || s == "backup" || s == "delete" ||
         s == "delete-log" || s == "kill") {
        e = c > 0;
      }
      button.disabled = !e;
    }
  }
};

function setInfo(message) {
  setText(message, 'info');
};

function setWarning(message) {
  setText(message, 'warning');
};

function setError(message) {
  setText(message.replace(/Stack Trace:.*/, ''), 'error');
};

function setText(message, type) {
  var i = document.getElementById("info");
  i.className = type;
  i.textContent = message;
};

var searchDelay = 200;
var _d;
function query(wait, success, key, query, enforce, target) {
  var d = new Date();
  _d = d;
  setTimeout(function() {
    if(_d != d) return;

    var name = document.getElementById("name");
    var resource = document.getElementById("resource");
    var sort = document.getElementById("sort");
    var loglist = document.getElementById("loglist");
  
    if(wait) setWarning(wait);
    var req = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
    req.onreadystatechange = function() {
      if(_d != d) return;
      if(req.readyState == 4) {
        if(req.status == 200) {
          target(req.responseText);
          if(success) setInfo(success);
        } else {
          setError(req.statusText.replace(/^.*?\] /, ''));
        }
      }
    };
    // synchronous querying: wait for server feedback
    req.open("POST", key +
      "?name=" + encodeURIComponent(name ? name.value : "") +
      "&resource=" + encodeURIComponent(resource ? resource.value : "") +
      "&sort=" + encodeURIComponent(sort ? sort.value : "") +
      "&loglist=" + encodeURIComponent(loglist ? loglist.value : "") +
      "&query=" + encodeURIComponent(query), true);
    req.send(null);
  }, enforce ? 0 : searchDelay);
};

var _list;
function logslist(wait, success) {
  var input = document.getElementById('loglist').value.trim();
  if(_list == input) return false;
  _list = input;
  query(wait, success, 'loglist', input, false, function(text) {
    document.getElementById("list").innerHTML = text;
  })
};

var _logs;
function logentries(wait, success) {
  var input = document.getElementById('logs').value.trim();
  if(_logs == input) return false;
  _logs = input;
  query(wait, success, 'logs', input, false, function(text) {
    document.getElementById("output").innerHTML = text;
  });
};

var _input;
function queryResource(wait, success) {
  var input = document.getElementById('input').value.trim();
  if(_input == input) return false;
  _input = input;
  var target = 'query-resource';
  var enforce = false;
  query(wait, success, target, input, enforce, function(text) {
    document.getElementById("output").value = text;
  });
};

var _editor;
function xquery(wait, success, enforce) {
  var mode = document.getElementById("mode").selectedIndex;
  var update = mode == 2;
  var realtime = mode == 1;
  document.getElementById("run").disabled = realtime;

  var editor = document.getElementById('editor').value;
  if(enforce || (realtime && _editor != editor)) {
    _editor = editor;
    var target = update ? 'update-query' : 'eval-query';
    query(wait, success, target, editor, enforce, function(text) {
      document.getElementById("output").value = text;
    });
  }
};
