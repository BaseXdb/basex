
/**
 * Toggles the selection of all check boxes in the corresponding form.
 * @param {checkbox} clicked header checkbox
 */
function toggle(source) {
  var form = getForm(source);
  var inputs = form.getElementsByTagName("INPUT");
  var checked = false;
  for(var i = 0; i < inputs.length; i++) {
    if(inputs[i].type == "checkbox") inputs[i].checked = source.checked;
  }
  buttons();
};

/**
 * Refreshes the disabled property of form buttons after a checkbox has been clicked.
 * @param {checkbox} clicked checkbox. if undefined, the buttons of all forms will be refreshed
 */
function buttons(source) {
  var forms = source ? [ getForm(source) ] : document.getElementsByTagName("FORM");
  for(var f = 0; f < forms.length; f++) {
    var form = forms[f];
    if(form.className != "update") continue;

    var inputs = form.getElementsByTagName("INPUT");
    var checked = false;
    for(var i = 0; i < inputs.length; i++) {
      checked |= inputs[i].type == "checkbox" && inputs[i].checked;
    }
    var buttons = form.getElementsByTagName("BUTTON");
    for(var b = 0; b < buttons.length; b++) {
      var button = buttons[b];
      if(button.className == "global") continue;

      var values = [
        "backup", "backup-drop", "backup-restore",
        "db-drop", "db-optimize", "db-optimize-all", "delete",
        "file-delete", "job-stop", "log-delete", "session-kill",
        "pattern-drop", "user-drop"
      ];
      for(var v = 0; v < values.length; v++) {
        if(button.value == values[v]) button.disabled = !checked;
      }
    }
  }
};

/**
 * Returns the ancestor form element
 * @param {checkbox} clicked checkbox. if undefined, the buttons of all forms will be refreshed
 * @return {element} form element
 */
function getForm(source) {
  while(source.tagName.toUpperCase() != "FORM") source = source.parentElement;
  return source;
};

/**
 * Displays an info message.
 * @param {string} message  info message
 */
function setInfo(message) {
  setText(message, "info");
};

/**
 * Displays a warning message.
 * @param {string} message  warning message
 */
function setWarning(message) {
  setText(message, "warning");
};

/**
 * Displays an error message. Stack trace info will be replaced.
 * @param {string} message  error message
 */
function setError(message) {
  setText(message.replace(/Stack Trace:.*/, ""), "error");
};

/**
 * Displays text with the specified type.
 * @param {string} message  message
 * @param {type}   type     message type (info, warning, error)
 */
function setText(message, type) {
  var info = document.getElementById("info");
  info.className = type;
  info.textContent = message;
};

/** Indicates how many queries are being evaluated. */
var _running = 0;

/**
 * Runs a query and shows the result.
 * @param {string} path   path to query service
 * @param {query}  query  query to be evaluated
 * @param {func}   func   function that processes the result
 */
function query(path, query, func) {
  _running++;
  setInfo("");
  var stop = document.getElementById("stop");
  if(stop) stop.disabled = true;

  setTimeout(function() {
    if(_running) {
      setWarning("Please wait…");
      if(stop) stop.disabled = false;
    }
  }, 500);

  var name = document.getElementById("name");
  var resource = document.getElementById("resource");
  var sort = document.getElementById("sort");
  var page = document.getElementById("page");
  var url = path +
    "?name=" + encodeURIComponent(name ? name.value : "") +
    "&resource=" + encodeURIComponent(resource ? resource.value : "") +
    "&sort=" + encodeURIComponent(sort ? sort.value : "") +
    "&page=" + encodeURIComponent(page ? page.value : "");
  request("POST", url, query,
    function(request) {
      _running--;
      if(!_running) {
        setInfo("Query was successful.");
        if(stop) stop.disabled = true;
      }
      func(request.responseText);
    },
    function(request) {
      _running--;
      if(request.status != 460 || !_running) setErrorFromResponse(request);
    }
  )
};

/**
 * Displays the error that is embedded in the HTTP response.
 * @param {object} request  HTTP request
 */
function setErrorFromResponse(request) {
  // normalize error message
  var msg = request.statusText.match(/\[\w+\]/g) ? request.statusText : request.responseText;
  var s = msg.indexOf("["), e1 = msg.indexOf("\n", s);
  if(s > -1) msg = msg.substring(s, e1 > s ? e1 : msg.length);
  msg = msg.replace(/\s+/g, " ");
  if(msg.length > 100) msg = msg.substring(0, 100) + "…";
  // display correctly escaped feedback
  var html = document.createElement("div");
  html.innerHTML = msg;
  setError(html.innerText || html.textContent);
};

/** Most recent log entry search string. */
var _logInput;

/**
 * Queries all log entries of a log file.
 * @param {boolean} enforce  enforce query execution
 */
function logEntries(enforce) {
  var input = document.getElementById("input").value.trim();
  if(!enforce && _logInput == input) return false;
  _logInput = input;
  query("log", input, function(text) {
    document.getElementById("output").innerHTML = text;
  });

  // refresh browser history
  window.history.replaceState(null, "", replaceParam(window.location.href, "input", input));
};

/** Most recent query search string. */
var _dbInput;

/**
 * Queries a database resource.
 * @param {boolean} enforce  enforce query execution
 */
function queryResource(enforce) {
  var input = document.getElementById("input").value.trim();
  if(!enforce && _dbInput == input) return false;
  _dbInput = input;
  query("db-query", input, function(text) {
    _outputMirror.setValue(text);
  });
};

/** Indicates if current query is updating. */
var _updating;

/**
 * Evaluates a query.
 * @param {boolean} reverse  reverse query execution mode (eval, update)
 */
function runQuery(reverse) {
  // decide if query is read-only or updating
  var updating = (document.getElementById("mode").selectedIndex == 1) ^ reverse;
  var path = updating ? "update-query" : "eval-query";

  // stop old query if mode was changed (each has its own %rest:single function)
  if(_running && _updating != updating) stopQuery();
  _updating = updating;

  // run query
  query(path, document.getElementById("editor").value, function(text) {
    _outputMirror.setValue(text);
  });
};

/**
 * Stops a query.
 */
function stopQuery() {
  // stop query by sending empty sequence
  query(_updating ? "update-query" : "eval-query", "()", function(text) {
    setInfo("Query was stopped.");
  });
};

/**
 * Creates and sends an HTTP request.
 * @param {string}  method   HTTP method
 * @param {url}     url      URL to be called
 * @param {data}    data     data to be sent
 * @param {success} success  success function
 * @param {failure} failure  failure function
 */
function request(method, url, data, success, failure) {
  var request = window.XMLHttpRequest ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP");
  request.onreadystatechange = function() {
    if(request.readyState == 4) {
      if(request.status == 200) {
        success(request);
      } else {
        failure(request);
      }
    }
  };
  // synchronous querying: wait for server feedback
  request.open(method, url, true);
  request.setRequestHeader("Content-Type", "text/plain");
  request.send(data);
};

/** Link to the mirrored editor component. */
var _editorMirror;
/** Link to the mirrored output component. */
var _outputMirror;

/** Editor. */
var _edit;

/**
 * Loads the code mirror editor extension.
 * @param {boolean}  edit  editor (or read-only view)
 */
function loadCodeMirror(edit) {
  _edit = edit;
  if (CodeMirror && dispatchEvent) {
    if(edit) {
      var editorArea = document.getElementById("editor");
      _editorMirror = CodeMirror.fromTextArea(editorArea, {
        mode: "xquery",
        lineNumbers: true,
        extraKeys: {
          "Ctrl-Enter"      : function(cm) { runQuery(); },
          "Cmd-Enter"       : function(cm) { runQuery(); },
          "Shift-Ctrl-Enter": function(cm) { runQuery(true); },
          "Shift-Cmd-Enter" : function(cm) { runQuery(true); }
        }
      });
      _editorMirror.on("change", function(cm, cmo) { cm.save(); });
      _editorMirror.display.wrapper.style.border = "solid 1px grey";
    }

    var outputArea = document.getElementById("output");
    _outputMirror = CodeMirror.fromTextArea(outputArea, {
      mode: "xml",
      readOnly: true,
    });
    _outputMirror.display.wrapper.style.border = "solid 1px grey";

    window.addEventListener("load", setDisplayHeight);
    window.addEventListener("resize", setDisplayHeight);
  }
}

/**
 * Sets the display height of the editor and result views.
 */
function setDisplayHeight() {
  // get current height
  var dummy = document.createElement("div");
  document.body.appendChild(dummy);
  var p = dummy.offsetTop;
  document.body.removeChild(dummy);
  var s = window.innerHeight;

  // adjust height of all editors
  var elems = document.getElementsByClassName("CodeMirror");
  for(var e = 0; e < elems.length; e++) {
    var v = _edit ? p - elems[e].offsetHeight : elems[e].offsetTop;
    elems[e].CodeMirror.setSize("100%", Math.max(50, s - 20 - v));
  };
}

/**
 * Adds the input string to the link target.
 * @param {link} source  clicked link
 */
function addInput(source) {
  source.href = replaceParam(source.href, "input", document.getElementById("input").value.trim());
};

/**
 * Replace a query parameter.
 * @param {string} url    url
 * @param {string} name   name
 * @param {string} value  value
 * @returns {string} new url
 */
function replaceParam(url, name, value) {
  var key = name + "=";
  var qm = url.indexOf("?");
  var href = (qm < 0 ? url : url.substr(0, qm)) + "?" + key + encodeURIComponent(value);
  if(qm >= 0) {
    var params = url.substr(qm + 1).split('&');
    for(var p = 0; p < params.length; p++) {
      if(params[p].indexOf(key) < 0) href += "&" + params[p];
    }
  }
  return href;
}
