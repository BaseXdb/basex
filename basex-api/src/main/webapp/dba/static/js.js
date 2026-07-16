/** Link to the CodeMirror editor component. */
let _editor;
/** Link to the CodeMirror output component. */
let _output;

/** Type of (latest) running query. */
let _updating;
/** Promise of (latest) running query. */
let _running;

/** Most recent log entry search state. */
let _logInput;
/** Most recent log filter string. */
let _dbInput;

/**
 * Indicates whether the table row containing a checkbox is currently shown.
 * @param {checkbox} input checkbox
 * @returns {boolean} visibility
 */
function rowVisible(input) {
  return input.closest("tr")?.style.display !== "none";
}

/**
 * Toggles the selection of all checkboxes in a form.
 * @param {checkbox} source clicked header checkbox
 */
function toggle(source) {
  for(const input of getForm(source).querySelectorAll("input[type=checkbox]")) {
    input.checked = source.checked && rowVisible(input);
  }
  buttons(source);
}

/**
 * Refreshes all buttons and checkboxes of a form.
 * @param {checkbox} source clicked checkbox. if undefined, all forms will be refreshed
 */
function buttons(source) {
  for(const form of (source ? [ getForm(source) ] : document.querySelectorAll("form"))) {
    // count selected items and refresh header checkbox
    let count = 0, checked = 0, header;
    for(const input of form.querySelectorAll("input[type=checkbox]")) {
      if(rowVisible(input)) {
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
    for(const button of form.querySelectorAll("button")) {
      if(button.getAttribute("data-check")) button.disabled = !checked;
    }
  }
}

/**
 * Returns the enclosing form element.
 * @param {source} source element
 */
function getForm(source) {
  return source.closest("form");
}

/**
 * Toggles the expansion of truncated table cells.
 */
document.addEventListener("click", (event) => {
  const cell = event.target.closest("table.fixed td");
  // keep state if text is being selected (e.g. for copying it)
  if(cell?.matches(".truncated, .expanded") && window.getSelection().isCollapsed) {
    cell.classList.toggle("expanded");
  }
});

/**
 * Marks truncated table cells, indicating that they can be expanded.
 */
function markTruncated() {
  for(const cell of document.querySelectorAll("table.fixed td")) {
    if(!cell.classList.contains("expanded")) {
      cell.classList.toggle("truncated", cell.scrollWidth > cell.clientWidth);
    }
  }
}
window.addEventListener("resize", markTruncated);

/**
 * Asks for confirmation, naming the action and the selected entries.
 * @param {button} button clicked button
 * @param {string} action action label
 * @returns {boolean} true if the action was confirmed
 */
function confirmAction(button, action) {
  const values = [];
  for(const input of getForm(button).querySelectorAll("input[type=checkbox]")) {
    if(input.name && input.checked && rowVisible(input)) {
      values.push(input.value);
    }
  }
  const count = values.length;
  const message = count
    ? `${action} ${count} ${count === 1 ? "entry" : "entries"}: ` +
      `${values.slice(0, 8).join(", ")}${count > 8 ? ", …" : ""}?`
    : "Are you sure?";
  return confirm(message);
}

/**
 * Displays text with the specified type.
 * @param {string} message message to display
 * @param {type} type message type (info, warning, error)
 */
function setText(message, type) {
  const info = document.getElementById("info");
  info.className = type;
  info.textContent = message;
  info.title = message;
}

/**
 * Indicates that the files of a form are being uploaded.
 * @param {form} form submitted form
 */
function uploading(form) {
  setText("Files are being uploaded…", "warning");
  // disable buttons after dispatch, so the clicked button's 'formaction' is still evaluated
  setTimeout(() => {
    for(const button of form.querySelectorAll("button")) button.disabled = true;
  });
}

/**
 * Creates and sends an HTTP request.
 * @param {url} url URL to be called
 * @param {data} data data to be sent
 * @returns {promise} promise
 */
async function request(url, data) {
  let response;
  try {
    response = await fetch(url, {
      method: "post",
      headers: { "Content-Type": "text/plain" },
      body: data
    });
  } catch {
    // network failure: mirror the XHR shape that consumers (showError) read
    throw { status: 0, statusText: "", responseText: "" };
  }
  const text = await response.text();
  if(response.status >= 200 && response.status < 400) return text;
  throw { status: response.status, statusText: response.statusText, responseText: text };
}

/**
 * Runs a query and shows the result.
 * @param {string} path URL path
 * @param {string} query query to be evaluated
 * @param {boolean} reset reset query
 */
function query(path, query, reset) {
  let url = path;
  for(const name of [ "name", "date", "resource", "sort", "time", "page" ]) {
    const value = document.getElementById(name)?.value;
    if(value && (name !== "page" || value !== 1 && !reset)) {
      url += `${url === path ? "?" : "&"}${name}=${encodeURIComponent(value)}`;
    }
  }
  const filters = document.querySelectorAll("input.filter");
  if(filters.length) {
    for(const input of filters) {
      const value = input.value.trim();
      if(value) url += `${url === path ? "?" : "&"}${input.name}=${encodeURIComponent(value)}`;
    }
  } else {
    // initial rendering: filter fields do not exist yet, take values from page URL
    for(const [name, value] of new URL(window.location.href).searchParams) {
      if(name.startsWith("f-") && value) {
        url += `${url === path ? "?" : "&"}${name}=${encodeURIComponent(value)}`;
      }
    }
  }
  return request(url, query);
}

/**
 * Evaluates a query in the editor panel.
 */
async function runQuery() {
  if(document.getElementById("run").disabled) return;
  if(_editor) _editor.focus();

  const stop = document.getElementById("stop");
  stop.disabled = true;
  setText("", "");

  const queryString = document.getElementById("editor").value;
  const self = query("parse", queryString);
  try {
    const up = (await self) === "true";
    // stop a running query of the other kind before switching update/query mode
    const stopping = _running && up !== _updating ? stopQuery() : Promise.resolve();
    _updating = up;
    await stopping;

    register(self);
    const file = document.getElementById("file");
    let path = _updating ? "update" : "query";
    if(file && file.value) path += `?file=${encodeURIComponent(file.value)}`;
    showResult(await query(path, queryString));
  } catch(response) {
    showError(response);
  } finally {
    if(self === _running) {
      stop.disabled = true;
      _running = undefined;
    }
  }
}

/**
 * Stops the query that is currently evaluated in the editor panel.
 * @param {boolean} show show info if query was successfully stopped
 * @returns {promise} promise
 */
async function stopQuery(show) {
  if(_editor) _editor.focus();

  await query(_updating ? "update" : "query", "()");
  _running = undefined;
  if(show) {
    setText("Query was stopped.", "warning");
    document.getElementById("stop").disabled = true;
  }
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
      const stop = document.getElementById("stop");
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
  let msg = response.statusText.match(/\[\w+\]/g) ? response.statusText : response.responseText;
  const lc = !info && msg.match(/\d+\/\d+:/);
  const s = msg.indexOf("["), e1 = msg.indexOf("\n", s);
  if(s > -1) msg = msg.substring(s, e1 > s ? e1 : msg.length);
  msg = msg.replace(/^\[.*?\] /, "").replace(/Stack Trace:.*/, "").replace(/\s+/g, " ");
  if(info) msg = `${info}: ${msg}`;
  if(lc) msg = `${lc} ${msg}`;

  // decode HTML entities via an inert parse (no scripts run, no resources load)
  const decoded = new DOMParser().parseFromString(msg, "text/html").documentElement.textContent;
  setText(decoded, "error");
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
async function logEntries(key) {
  const reset = key && key !== "Enter";
  const input = document.getElementById("input").value.trim();
  const filters = document.querySelectorAll("input.filter");
  const state = [ input, ...[...filters].map(f => f.value.trim()) ].join("\u0000");
  if(reset && _logInput === state) return false;
  _logInput = state;
  try {
    const text = await query("logs", input, reset);
    setText("", "");
    // preserve focus and caret of a filter field across the table refresh
    const active = document.activeElement;
    const focused = active?.matches("input.filter") && active;
    document.getElementById("output").innerHTML = text;
    markTruncated();
    const e = document.getElementById(window.location.hash.replace(/^#/, ""));
    if(e) e.scrollIntoView();
    if(focused) {
      const filter = document.querySelector(`input.filter[name="${focused.name}"]`);
      if(filter) {
        filter.value = focused.value;
        filter.focus();
        filter.setSelectionRange(focused.selectionStart, focused.selectionEnd);
      }
    }
    if(reset) window.history.replaceState(null, "", replaceParam(window.location.href, "page", 1));
  } catch(response) {
    showError(response);
  } finally {
    // refresh browser history
    let href = replaceParam(window.location.href, "input", input);
    for(const filter of filters) href = replaceParam(href, filter.name, filter.value.trim());
    window.history.replaceState(null, "", href);
  }
}

/**
 * Filters log files.
 */
function logFilter() {
  const value = document.getElementById("log-filter").value;
  let count = 0, checked = 0;
  for(const input of document.getElementById("dates").querySelectorAll("input[type=checkbox]")) {
    if(input.name === "name") {
      const visible = !value || input.value.startsWith(value);
      input.closest("tr").style.display = visible ? null : "none";
      if(visible) {
        count++;
        if(input.checked) checked++;
      } else {
        input.checked = false;
      }
    }
  }
  for(const id of ["log-download", "log-delete"]) {
    document.getElementById(id).disabled = !checked;
  }
  document.querySelector("h3").innerHTML = `${count} Entries`;
  buttons();
}

/**
 * Queries a database resource.
 * @param {boolean} enforce enforce query execution
 */
async function queryResource(enforce) {
  const input = document.getElementById("input").value.trim();
  if(!enforce && _dbInput === input) return false;
  _dbInput = input;

  const self = query("db-query", input);
  register(self);
  try {
    showResult(await self);
  } catch(response) {
    showError(response);
  } finally {
    if(self === _running) _running = undefined;
  }
}

/**
 * Loads the CodeMirror editor extension.
 * @param {string}  language of main editor (for syntax highlighting)
 * @param {boolean} edit edit flag (edit vs. read-only)
 * @param {boolean} resize resize text areas to maximum height
 */
function loadCodeMirror(language, edit, resize) {
  if(!CodeMirror || !dispatchEvent) return;

  const useCM = !/android/i.test(navigator.userAgent);
  if(edit) {
    const editorArea = document.getElementById("editor");
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
      _editor.display.wrapper.classList.add("codemirror");
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

  const outputArea = document.getElementById("output");
  if(outputArea != null) {
    if (useCM) {
      _output = CodeMirror.fromTextArea(outputArea, {
        mode: "xml",
        lineWrapping: true,
        readOnly: true
      });
      _output.display.wrapper.classList.add("codemirror");
    } else {
      _output = {
        setValue(v) { outputArea.value = v; }
      }
    }
  }

  if(resize) {
    const refresh = () => {
      // size each pane from its own top to the viewport bottom, so a tall
      // sibling column (e.g. a long resource list) can't shrink it
      const height = elem => Math.max(192, window.innerHeight - elem.getBoundingClientRect().top - 32);
      if (useCM) {
        for(const elem of document.querySelectorAll(".CodeMirror")) {
          elem.CodeMirror.setSize("100%", height(elem));
        }
      } else {
        for(const elem of document.querySelectorAll("textarea")) {
          elem.style.height = `${height(elem)}px`;
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
  let href = replaceParam(source.href, "input", document.getElementById("input").value.trim());
  for(const input of document.querySelectorAll("input.filter")) {
    href = replaceParam(href, input.name, input.value.trim());
  }
  source.href = href;
}

/**
 * Removes query parameters from the address bar, so a page refresh
 * will not repeat outdated info and error messages.
 * @param {...string} names parameter names
 */
function hideParams(...names) {
  const url = new URL(window.location.href);
  for(const name of names) url.searchParams.delete(name);
  window.history.replaceState(null, "", url);
}

/**
 * Replaces a query parameter; empty values remove the parameter.
 * @param {string} url URL
 * @param {string} name name
 * @param {string} value value
 * @returns {string} new url
 */
function replaceParam(url, name, value) {
  const u = new URL(url);
  if(`${value}`) u.searchParams.set(name, value);
  else u.searchParams.delete(name);
  return u.href;
}
