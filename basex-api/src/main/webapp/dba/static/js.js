/** Link to the CodeMirror editor component. */
var _editor;
/** Link to the CodeMirror output component. */
var _output;

/** Type of (latest) running query. */
var _updating;
/** Promise of (latest) running query. */
var _running;

/** Most recent log entry search string. */
var _logInput;
/** Most recent log filter string. */
var _dbInput;

/**
 * Toggles the selection of all checkboxes in a form.
 * @param {checkbox} source clicked header checkbox
 */
function toggle(source) {
  for(var input of getForm(source).getElementsByTagName("input")) {
    if(input.type === "checkbox") {
      input.checked = source.checked && input.parentElement.parentElement.style.display !== "none";
    }
  }
  buttons(source);
}

/**
 * Refreshes all buttons and checkboxes of a form.
 * @param {checkbox} source clicked checkbox. if undefined, all forms will be refreshed
 */
function buttons(source) {
  for(var form of (source ? [ getForm(source) ] : document.getElementsByTagName("form"))) {
    // count selected items and refresh header checkbox
    var count = 0, checked = 0, header = undefined;
    for(var input of form.getElementsByTagName("input")) {
      if(input.type === "checkbox" && input.parentElement.parentElement.style.display !== "none") {
        if(input.name) {
          count++;
          if(input.checked) checked++;
        } else {
          header = input;
        }
      }
    }
    if(header) header.checked = count && count === checked;

    // check button states
    for(var button of form.getElementsByTagName("button")) {
      if(button.getAttribute("data-check")) button.disabled = !checked;
    }
  }
}

/**
 * Returns an ancestor element
 * @param {source} source element
 */
function getForm(source) {
  while(source.tagName.toLowerCase() !== "form") source = source.parentElement;
  return source;
}

/**
 * Displays text with the specified type.
 * @param {string} message message to display
 * @param {type} type message type (info, warning, error)
 */
function setText(message, type) {
  var info = document.getElementById("info");
  info.className = type;
  info.textContent = message;
  info.title = message;
}

/**
 * Creates and sends an HTTP request.
 * @param {url} url URL to be called
 * @param {data} data data to be sent
 * @returns {promise} promise
 */
function request(url, data) {
  return new Promise((resolve, reject) => {
    var request = new XMLHttpRequest();
    request.open("post", url);
    request.setRequestHeader("Content-Type", "text/plain");
    request.onreadystatechange = () => {
      if(request.readyState === XMLHttpRequest.DONE) {
        var status = request.status;
        if(status >= 200 && status < 400) {
          resolve(request.responseText);
        } else {
          reject(request);
        }
      }
    };
    request.send(data);
  });
}

/**
 * Runs a query and shows the result.
 * @param {string} path URL path
 * @param {string} query query to be evaluated
 * @param {boolean} reset reset query
 */
function query(path, query, reset) {
  var url = path;
  for(var name of [ "name", "date", "resource", "sort", "time", "page" ]) {
    var element = document.getElementById(name), value = element && element.value;
    if(value && (name !== "page" || value !== 1 && !reset)) {
      url += (url === path ? "?" : "&") + name + "=" + encodeURIComponent(value);
    }
  }
  return request(url, query);
}

/**
 * Evaluates a query in the editor panel.
 */
function runQuery() {
  if(document.getElementById("run").disabled) return;
  if(_editor) _editor.focus();

  var stop = document.getElementById("stop");
  stop.disabled = true;
  setText("", "");

  var queryString = document.getElementById("editor").value;
  var self = query("parse", queryString);
  return self.then((updating) => {
    var up = updating === "true";
    var next = _running && up !== _updating ? stopQuery() : Promise.resolve();
    _updating = up;
    return next;
  }).then(() => {
    register(self);
    var file = document.getElementById("file");
    var path = _updating ? "update" : "query";
    if(file && file.value) path += "?file=" + encodeURIComponent(file.value);
    return query(path, queryString);
  }).then((text) => {
    showResult(text);
  }).catch((response) => {
    showError(response);
  }).finally(() => {
    if(self === _running) {
      stop.disabled = true;
      _running = undefined;
    }
  });
}

/**
 * Stops the query that is currently evaluated in the editor panel.
 * @param {boolean} show show info if query was successfully stopped
 * @returns {promise} promise
 */
function stopQuery(show) {
  if(_editor) _editor.focus();

  return query(_updating ? "update" : "query", "()").then(() => {
    _running = undefined;
    if(show) {
      setText("Query was stopped.", "warning");
      document.getElementById("stop").disabled = true;
    }
  });
}

/**
 * Registers the promise.
 * @param {promise} self reference to promise
 */
function register(self) {
  _running = self;
  setTimeout(() => {
    if(self === _running) {
      setText("Please wait…", "warning");
      var stop = document.getElementById("stop");
      if(stop) stop.disabled = false;
    }
  }, 500);
}

/**
 * Displays an error message.
 * @param {response} response HTTP response
 * @param {string} info optional info
 */
function showError(response, info) {
  if(response.status === 460) return;

  // normalize error message
  var msg = response.statusText.match(/\[\w+\]/g) ? response.statusText : response.responseText;
  var lc = !info && msg.match(/\d+\/\d+:/);
  var s = msg.indexOf("["), e1 = msg.indexOf("\n", s);
  if(s > -1) msg = msg.substring(s, e1 > s ? e1 : msg.length);
  msg = msg.replace(/^\[.*?\] /, "").replace(/Stack Trace:.*/, "").replace(/\s+/g, " ");
  if(info) msg = info + ": " + msg;
  if(lc) msg = lc + " " + msg;

  // display correctly escaped feedback
  var html = document.createElement("div");
  html.innerHTML = msg;
  setText(html.innerText || html.textContent, "error");
}

/**
 * Shows the result of a query.
 * @param {string} text result
 * @returns {promise} promise
 */
function showResult(text) {
  setText("Query was successful.", "info");
  _output.setValue(text);
}

/**
 * Queries the entries of the current log file.
 * @param {string} key typed key
 */
function logEntries(key) {
  var reset = key && key !== "Enter";
  var input = document.getElementById("input").value.trim();
  if(reset && _logInput === input) return false;
  _logInput = input;
  return query("log", input, reset).then((text) => {
    setText("", "");
    document.getElementById("output").innerHTML = text;
    var e = document.getElementById(window.location.hash.replace(/^#/, ""));
    if(e) e.scrollIntoView();
    if(reset) window.history.replaceState(null, "", replaceParam(window.location.href, "page", 1));
  }, (response) => {
    showError(response);
  }).finally(() => {
    // refresh browser history
    window.history.replaceState(null, "", replaceParam(window.location.href, "input", input));
  });
}

/**
 * Filters log files.
 */
function logFilter() {
  var value = document.getElementById("log-filter").value;
  var count = 0, checked = 0;
  for(var input of document.getElementById("dates").getElementsByTagName("input")) {
    if(input.type === "checkbox" && input.name === "name") {
      var visible = !value || input.value.startsWith(value);
      input.parentElement.parentElement.style.display = visible ? null : "none";
      if(visible) {
        count++;
        if(input.checked) checked++;
      } else {
        input.checked = false;
      }
    }
  }
  for(var id of ["log-download", "log-delete"]) {
    document.getElementById(id).disabled = !checked;
  }
  document.getElementsByTagName("h3")[0].innerHTML = count + " Entries";
  buttons();
}

/**
 * Queries a database resource.
 * @param {boolean} enforce enforce query execution
 */
function queryResource(enforce) {
  var input = document.getElementById("input").value.trim();
  if(!enforce && _dbInput === input) return false;
  _dbInput = input;

  var self = query("db-query", input);
  register(self);
  return self.then((text) => {
    showResult(text);
  }).catch((response) => {
    showError(response);
  }).finally(() => {
    if(self === _running) _running = undefined;
  });
}

/**
 * Loads the CodeMirror editor extension.
 * @param {string}  language of main editor (for syntax highlighting)
 * @param {boolean} edit edit flag (edit vs. read-only)
 * @param {boolean} resize resize text areas to maximum height
 */
function loadCodeMirror(language, edit, resize) {
  if(!CodeMirror || !dispatchEvent) return;

  var useCM = !/android/i.test(navigator.userAgent);
  if(edit) {
    var editorArea = document.getElementById("editor");
    if (useCM) {
      _editor = CodeMirror.fromTextArea(editorArea, {
        mode: language,
        lineNumbers: true,
        lineWrapping: true,
        extraKeys: {
          "Ctrl-Enter": runQuery,
          "Cmd-Enter" : runQuery
        }
      });
      _editor.display.wrapper.className += " codemirror";
      _editor.on("change", (cm) => {
        cm.save();
        if(checkButtons) checkButtons();
      });
    } else {
      _editor = {
        setValue(v) { editorArea.value = v; },
        historySize() { return {}; },
        clearHistory() {},
        focus() { editorArea.focus(); }
      }
      editorArea.onchange = () => {
        if(checkButtons) checkButtons();
      };
    }
  }

  var outputArea = document.getElementById("output");
  if(outputArea != null) {
    if (useCM) {
      _output = CodeMirror.fromTextArea(outputArea, {
        mode: "xml",
        lineWrapping: true,
        readOnly: true
      });
      _output.display.wrapper.className += " codemirror";
    } else {
      _output = {
        setValue(v) { outputArea.value = v; }
      }
    }
  }

  if(resize) {
    var refresh = () => {
      var size = window.innerHeight - document.getElementById("footer").offsetTop - 32;
      var height = elem => Math.max(192, size + elem.offsetHeight);
      if (useCM) {
        for(var elem of document.getElementsByClassName("CodeMirror")) {
          elem.CodeMirror.setSize("100%", height(elem));
        }
      } else {
        for(var elem of document.getElementsByTagName("textarea")) {
          elem.style.height = height(elem) + "px"; 
        }
      }
    };
    window.addEventListener("load", refresh);
    window.addEventListener("resize", refresh);
  }
}

/**
 * Adds the input string to the link target.
 * @param {link} source clicked link
 */
function addInput(source) {
  source.href = replaceParam(source.href, "input", document.getElementById("input").value.trim());
}

/**
 * Replace a query parameter.
 * @param {string} url URL
 * @param {string} name name
 * @param {string} value value
 * @returns {string} new url
 */
function replaceParam(url, name, value) {
  var key = name + "=";
  var qm = url.indexOf("?");
  var href = (qm < 0 ? url : url.substr(0, qm)) + "?" + key + encodeURIComponent(value);
  if(qm >= 0) {
    for(var param of url.substr(qm + 1).split("&")) {
      if(param.indexOf(key) < 0) href += "&" + param;
    }
  }
  return href;
}