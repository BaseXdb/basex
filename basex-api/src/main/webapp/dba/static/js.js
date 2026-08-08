/** Link to the CodeMirror editor component. */
let _editor;
/** Link to the CodeMirror output component. */
let _output;

/** Promise of (latest) running query. */
let _running;

/** Connection to the editor endpoint (promise, resolved with an open WebSocket). */
let _socket;
/** Number of the last query that was started in the editor panel. */
let _run = 0;
/** Number of the query whose outcome is awaited (0: none). */
let _pending = 0;

/** Most recent log entry search state. */
let _logInput;
/** Most recent log filter string. */
let _dbInput;

/** Content panels of the current page, in grid track order. */
let _panels = [];
/** Whether at least one of the panels can be collapsed. */
let _collapsible = false;

/** Whether the current database resource can be edited. */
let _resourceEditable;
/** Server-rendered read-only reason ([ message, class ]). */
let _resourceNote;
/** Cached raw document; undefined if it must be requested again. */
let _resourceSaved;

/** Longest query that is accepted by the editor endpoint.
    Kept well below the 'maxTextMessageSize' of the WebSocket servlet (see web.xml), which also
    needs to accommodate the UTF-8 encoding and the JSON escaping of the query. */
const MAX_QUERY_LENGTH = 1000000;

/** Link to the resizer area. */
let _resizer;
/** Link to the panel grid whose first track is resized. */
let _content;
/** Width of the left panel, in percent. */
let _width;

/** Width of the left panel before the resizer is first dragged, in percent. */
const DEFAULT_PANEL_WIDTH = 50;
/** Narrowest the resizer lets the left panel get, in percent of the grid. */
const MIN_PANEL_WIDTH = 10;
/** Widest the resizer lets the left panel get, in percent of the grid. */
const MAX_PANEL_WIDTH = 85;

/** localStorage key prefix for unsaved editor drafts (per file name). */
const DRAFT = "dba-draft:";
/** On-disk content of the current file (empty for an untitled buffer). */
let _saved = "";

/** Shortest an auto-resized editor gets, and its height on stacked layouts. */
const EDITOR_MIN_HEIGHT = 200;
/** Height of an editor that is not auto-resized. */
const EDITOR_FIXED_HEIGHT = "300px";

/** localStorage key for the logs 'ignore entries' filter. */
const IGNORE_KEY = "dba-ignore-logs";
/** localStorage key for the 'Indent' output preference. */
const INDENT_KEY = "dba-indent";
/** localStorage key prefix for the collapsed content panels of a page.
    Versioned: panel ids used to be row-based, the grid layout numbers them flat. */
const PANELS_KEY = "dba-panels-v2-";

/**
 * Indicates whether the layout has stacked the panels into a single column.
 * The breakpoint belongs to style.css; this only reads the flag it sets.
 * @returns {boolean} stacked state
 */
function stacked() {
  return getComputedStyle(document.documentElement).getPropertyValue("--stacked").trim() === "1";
}

/**
 * Returns the height of the page chrome below <main>: the rule, the footer and
 * the body margin. None of it depends on the editor, so it can be measured.
 * @returns {number} height in pixels
 */
function chromeBelowMain() {
  let height = parseFloat(getComputedStyle(document.body).marginBottom);
  for(let el = document.querySelector("main").nextElementSibling; el; el = el.nextElementSibling) {
    height += el.getBoundingClientRect().height;
  }
  return height;
}

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
  // read all overflow states first, then write classes: interleaving the two forces
  // a full re-layout per cell, which is O(rows) for a fixed table and dominates on large logs
  const cells = document.querySelectorAll("table.fixed td");
  const truncated = [];
  for(const cell of cells) {
    truncated.push(cell.classList.contains("expanded") ? null : cell.scrollWidth > cell.clientWidth);
  }
  cells.forEach((cell, i) => {
    if(truncated[i] === null) return;
    cell.classList.toggle("truncated", truncated[i]);
    // the tooltip is the only access to the clipped text
    if(truncated[i]) cell.title = cell.textContent;
    else cell.removeAttribute("title");
  });
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
  for(const name of [ "name", "date", "resource", "sort", "time", "page", "ignore" ]) {
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
  // output indentation is a client-side preference (see the 'Indent' checkbox)
  if(indentOn()) url += `${url === path ? "?" : "&"}indent=true`;
  return request(url, query);
}

/**
 * Returns the connection to the editor endpoint, and opens it if required.
 * @returns {promise} promise, resolved with an open WebSocket
 */
function editorSocket() {
  if(!_socket) {
    _socket = new Promise((resolve, reject) => {
      const scheme = location.protocol === "https:" ? "wss" : "ws";
      const socket = new WebSocket(`${scheme}://${location.host}/ws/dba`);
      socket.onopen = () => resolve(socket);
      socket.onmessage = event => showMessage(event.data);
      // a refused handshake and a lost connection both invalidate the cached promise,
      // so that the next run opens a new one
      socket.onerror = () => {
        _socket = undefined;
        reject(new Error("No connection to the server. " +
          "If you use a proxy server, check if WebSockets are enabled."));
      };
      socket.onclose = () => {
        _socket = undefined;
        if(_pending) {
          _pending = 0;
          setDisabled("stop", true);
          setText("Connection to the server was lost.", "error");
        }
      };
    });
  }
  return _socket;
}

/**
 * Sends a message to the editor endpoint.
 * @param {object} message message to be sent
 * @returns {promise} promise, resolved with true if the message was sent
 */
async function sendMessage(message) {
  try {
    (await editorSocket()).send(JSON.stringify(message));
    return true;
  } catch(ex) {
    showError({ status: 0, statusText: "", responseText: ex.message });
    return false;
  }
}

/**
 * Evaluates a query in the editor panel. The query is sent to the server, which pushes back the
 * result or the error; see showMessage.
 */
async function runQuery() {
  if(document.getElementById("run").disabled) return;
  if(_editor) _editor.focus();

  setDisabled("stop", true);
  setText("", "");

  // the server closes the connection without a response if a message exceeds its size limit
  const queryString = document.getElementById("editor").value;
  if(queryString.length > MAX_QUERY_LENGTH) {
    setText(`Query is too long (maximum: ${MAX_QUERY_LENGTH} characters).`, "error");
    return;
  }

  const run = ++_run;
  _pending = run;
  if(!await sendMessage({ type: "run", run: run, query: queryString, indent: indentOn() })) {
    _pending = 0;
    return;
  }
  // report and offer to stop a query that takes longer
  setTimeout(() => {
    if(_pending === run) {
      setText("Please wait…", "warning");
      setDisabled("stop", false);
    }
  }, 500);
}

/**
 * Stops the query that is currently evaluated in the editor panel. The server confirms the
 * request with a 'stopped' message; see showMessage.
 */
async function stopQuery() {
  if(_editor) _editor.focus();

  // drop the number of the run: the result of the stopped query will be ignored
  _pending = 0;
  setDisabled("stop", true);
  await sendMessage({ type: "stop" });
}

/**
 * Shows the outcome of a query that was pushed by the server.
 * @param {string} data JSON message
 */
function showMessage(data) {
  const json = JSON.parse(data);
  // messages without a run are not bound to a query: they must not end the one that is running
  if(json.type === "stopped") {
    setText("Query was stopped.", "warning");
  } else if(json.run === undefined) {
    showError({ status: 0, statusText: "", responseText: json.message });
  } else if(json.run === _pending) {
    // drop the outcome of a query that was stopped or superseded by a newer one
    _pending = 0;
    setDisabled("stop", true);
    if(json.type === "result") {
      showResult(json.result);
    } else {
      showError({ status: 0, statusText: "", responseText: json.message }, undefined, json);
    }
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
 * @param {object} position optional error position ({ line, column })
 */
function showError(response, info, position) {
  if(response.status === 460) return;

  // normalize error message
  let msg = response.statusText.match(/\[\w+\]/g) ? response.statusText : response.responseText;
  const match = info ? null : msg.match(/(\d+)\/(\d+):/);
  const line = position?.line ?? match?.[1], column = position?.column ?? match?.[2];
  // isolate the error-code line ([XPST0003] …, [db:get] …); match a real code, not any '[' in the text
  const s = msg.search(/\[([A-Z]\w*|[a-z][\w-]*:[\w-]+)\]/), e1 = msg.indexOf("\n", s);
  if(s > -1) msg = msg.substring(s, e1 > s ? e1 : msg.length);
  msg = msg.replace(/^\[.*?\] /, "").replace(/\s*Stack Trace:[\s\S]*/, "").replace(/\s+/g, " ");
  if(info) msg = `${info}: ${msg}`;

  // decode HTML entities via an inert parse (no scripts run, no resources load)
  const decoded = new DOMParser().parseFromString(msg, "text/html").documentElement.textContent;
  setText(decoded, "error");

  // with a line/column and an open editor, make a click on the message jump there
  const el = document.getElementById("info");
  if(line && _editor && _editor.setCursor) {
    el.dataset.line = line;
    el.dataset.column = column;
    el.classList.add("locatable");
  } else {
    delete el.dataset.line;
    delete el.dataset.column;
  }
}

/**
 * Moves the editor cursor to the line/column of a clickable error message.
 */
function jumpToError() {
  const info = document.getElementById("info");
  if(!info.classList.contains("locatable") || !_editor || !_editor.setCursor) return;
  _editor.setCursor({ line: Number(info.dataset.line) - 1, ch: Number(info.dataset.column) - 1 });
  _editor.focus();
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
  const ignore = document.getElementById("ignore")?.value.trim() ?? "";
  const filters = document.querySelectorAll("input.filter");
  const state = [ input, ignore, ...[...filters].map(f => f.value.trim()) ].join("\u0000");
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
 * Persists the log ignore filter and refreshes the entries.
 * @param {string} key typed key
 */
function ignoreLogs(key) {
  localStorage.setItem(IGNORE_KEY, document.getElementById("ignore").value);
  return logEntries(key);
}

/**
 * Restores the persisted ignore filter, then loads the log entries.
 */
function initLogs() {
  const ignore = document.getElementById("ignore");
  if(ignore) ignore.value = localStorage.getItem(IGNORE_KEY) ?? "";
  return logEntries();
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
 * Shows a database resource in the editor: the document, raw or indented, or a query result.
 * @param {boolean} enforce enforce execution
 */
async function queryResource(enforce) {
  const input = document.getElementById("input").value.trim();
  const indent = indentOn();
  // re-run whenever the query or the indent preference changes
  const state = input + " " + indent;
  if(!enforce && _dbInput === state) return false;
  _dbInput = state;

  // no query: show the document, raw or indented. only the raw one is cached
  if(!input) {
    if(!indent && _resourceSaved !== undefined) {
      showResource(_resourceSaved);
      return;
    }
    // block edits until the document has been received
    editResource(false);

    const self = query("db-query", ".");
    register(self);
    try {
      const text = await self;
      showResource(text);
      if(!indent) _resourceSaved = text;
    } catch(response) {
      showError(response);
    } finally {
      if(self === _running) _running = undefined;
    }
    return;
  }

  // a query result is read-only
  editResource(false);
  if(_resourceEditable) {
    showNote("Read-only: query result. Clear the query to edit the document again.");
  }

  const self = query("db-query", input);
  register(self);
  try {
    _editor.setValue(await self);
    setText("Query was successful.", "info");
  } catch(response) {
    showError(response);
  } finally {
    if(self === _running) _running = undefined;
  }
}

/**
 * Shows an editable document in the resource editor.
 * @param {string} text document
 */
function showResource(text) {
  _editor.setValue(text);
  editResource(_resourceEditable);
  showNote(_resourceEditable && indentOn() ?
    "Whitespace may be stripped when the document is saved." : undefined, true);
  setText("", "");
}

/**
 * Shows a note below the resource toolbar.
 * @param {string} message message; if omitted, the server-rendered reason is restored
 * @param {boolean} warn emphasize the message
 */
function showNote(message, warn) {
  const note = document.getElementById("note");
  if(note) [ note.textContent, note.className ] =
    message ? [ message, warn ? "note strong" : "note" ] : _resourceNote;
}

/**
 * Initializes the resource editor and its button states, honoring the stored
 * 'Indent' preference.
 * @param {boolean} editable whether the resource can be edited in place
 */
function initResource(editable) {
  _resourceEditable = editable;
  _resourceSaved = document.getElementById("editor").value;
  const note = document.getElementById("note");
  _resourceNote = [ note.textContent, note.className ];

  if(document.getElementById("input") && indentOn()) {
    // XML resource with indentation enabled: request the indented document
    queryResource(true);
  } else {
    editResource(editable);
  }
}

/**
 * Enables or disables editing of the shown resource.
 * @param {boolean} enabled edit state
 */
function editResource(enabled) {
  editorReadOnly(!enabled);
  setDisabled("save-resource", !enabled);
}

/**
 * Reads the stored 'Indent' output preference.
 * @returns {boolean} whether output should be indented
 */
function indentOn() {
  return localStorage.getItem(INDENT_KEY) === "yes";
}

/**
 * Persists the 'Indent' preference and, in the resource view, re-renders with it.
 */
function indentChanged() {
  localStorage.setItem(INDENT_KEY, document.getElementById("indent").checked ? "yes" : "no");
  // resource view re-renders immediately; the editor applies it on the next run
  if(document.getElementById("resource")) queryResource(true);
}

/**
 * Sets the read-only state of the resource editor (CodeMirror or plain textarea).
 * @param {boolean} readOnly read-only state
 */
function editorReadOnly(readOnly) {
  if(_editor.setOption) _editor.setOption("readOnly", readOnly);
  else document.getElementById("editor").readOnly = readOnly;
}

/**
 * Enables or disables an element by id.
 * @param {string} id element id
 * @param {boolean} disabled disabled state
 */
function setDisabled(id, disabled) {
  const el = document.getElementById(id);
  if(el) el.disabled = disabled;
}

/**
 * Copies the current resource editor content to the clipboard.
 */
function copyResource() {
  copy(document.getElementById("editor").value);
}

/**
 * Saves the edited content of a database resource.
 */
async function saveResource() {
  const name = document.getElementById("name").value;
  const resource = document.getElementById("resource").value;
  const content = document.getElementById("editor").value;
  const indent = indentOn();
  let url = `db-save?name=${encodeURIComponent(name)}&resource=${encodeURIComponent(resource)}`;
  if(indent) url += "&indent=true";
  try {
    await request(url, content);
    // the raw document has changed: request it again
    _resourceSaved = indent ? undefined : content;
    setText("Resource was saved.", "info");
  } catch(response) {
    showError(response);
  }
}

/**
 * Loads the CodeMirror editor extension.
 * @param {string}  language of main editor (for syntax highlighting)
 * @param {boolean} edit edit flag (edit vs. read-only)
 * @param {boolean} resize resize text areas to maximum height
 */
function loadCodeMirror(language, edit, resize) {
  // CodeMirror 6 is delivered as the self-contained window.CM6 bundle
  // Without it, or on Android, fall back to plain textareas
  const useCM = !!window.CM6 && !/android/i.test(navigator.userAgent);
  if(edit) {
    const editorArea = document.getElementById("editor");
    if (useCM) {
      _editor = CM6.fromTextArea(editorArea, {
        language,
        // Lezer-driven syntax-error gutter, only for the XQuery editor
        parseErrors: language === "xquery",
        extraKeys: [
          { key: "Ctrl-Enter", run: () => (runQuery(), true) },
          { key: "Cmd-Enter",  run: () => (runQuery(), true) }
        ],
        onChange: () => {
          if(checkButtons) checkButtons();
          saveDraft();
        }
      });
    } else {
      _editor = {
        setValue(v) { editorArea.value = v; },
        getValue() { return editorArea.value; },
        historySize() { return {}; },
        clearHistory() {},
        focus() { editorArea.focus(); }
      }
      editorArea.onchange = () => {
        if(checkButtons) checkButtons();
        saveDraft();
      };
    }
  }

  const outputArea = document.getElementById("output");
  if(outputArea != null) {
    if (useCM) {
      _output = CM6.fromTextArea(outputArea, { language: "xml", readOnly: true });
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
      // stacked layouts keep the minimum, so the editor does not fill the
      // viewport and push the output and buttons off-screen
      // measured once: reading it per element would interleave layout and style writes
      const reserve = chromeBelowMain();
      const height = elem => stacked() ? EDITOR_MIN_HEIGHT : Math.max(EDITOR_MIN_HEIGHT,
        window.innerHeight - elem.getBoundingClientRect().top - reserve);
      if (useCM) {
        for(const elem of document.querySelectorAll(".cm-editor")) {
          elem.style.height = `${height(elem)}px`;
        }
      } else {
        for(const elem of document.querySelectorAll("textarea")) {
          elem.style.height = `${height(elem)}px`;
        }
      }
    };
    window.addEventListener("load", refresh);
    window.addEventListener("resize", refresh);
  } else if (useCM) {
    // no auto-resize (e.g. the users pages): without a height, the editor
    // collapses to a single line. The textarea fallback is sized by style.css
    for(const elem of document.querySelectorAll(".cm-editor")) {
      elem.style.height = EDITOR_FIXED_HEIGHT;
    }
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
 * Copies text to the clipboard and confirms via the message area.
 * @param {string} text text to copy
 */
async function copy(text) {
  try {
    await navigator.clipboard.writeText(text);
    setText("Copied to clipboard.", "info");
  } catch {
    setText("Copy failed.", "error");
  }
}

/**
 * Copies the current query result to the clipboard.
 */
function copyOutput() {
  copy(_output ? _output.getValue() : "");
}

/**
 * Handles global keyboard shortcuts.
 * @param {event} event keydown event
 */
function shortcuts(event) {
  if(event.key === "Escape") {
    setText("", "");
    return;
  }
  // ignore key presses while typing or combined with modifier keys
  const target = event.target;
  if(event.ctrlKey || event.metaKey || event.altKey ||
     target.matches("input, textarea, select") || target.closest(".CodeMirror")) return;
  if(event.key === "/") {
    // prefer the main search field (right panel) over column and log-file filters
    for(const selector of [ "#input", "input.filter", "#log-filter" ]) {
      const field = document.querySelector(selector);
      if(field) {
        event.preventDefault();
        field.focus();
        break;
      }
    }
  }
}

/**
 * Makes the side-by-side content panels of a page collapsible.
 */
function initPanels() {
  const content = document.querySelector(".content");
  if(!content) return;
  // panels spanning all columns have no track of their own and never collapse
  const panels = [ ...content.children ].filter(p => p.matches(".panel:not(.full)"));
  if(panels.length < 2) return;

  // every panel owns a grid track, whether or not it can be collapsed
  _panels = panels;

  // if no state was stored yet, the markup supplies the default
  const stored = localStorage.getItem(PANELS_KEY + window.location.pathname);
  const collapsed = stored?.split(",");
  panels.forEach((panel, p) => {
    // the heading supplies the label of the collapsed strip; a panel without one
    // (an empty side panel, the editor panes) stays as it is
    const heading = panel.querySelector("h2, h3");
    if(!heading) return;
    const id = `${p}`;
    const button = document.createElement("button");
    button.type = "button";
    button.className = "collapse";
    // the last panel folds to the right, all others to the left
    button.dataset.right = p === panels.length - 1;
    button.dataset.title = heading.textContent.split("»")[0].trim();
    button.addEventListener("click", () => togglePanel(panel, id));
    panel.prepend(button);
    _collapsible = true;
    showPanel(panel, collapsed ? collapsed.includes(id) : panel.classList.contains("collapsed"));
  });
  applyColumns();
}

/**
 * Collapses or expands a content panel and persists the new state.
 * @param {div} panel panel to be toggled
 * @param {string} id panel id
 */
function togglePanel(panel, id) {
  const collapse = !panel.classList.contains("collapsed");
  showPanel(panel, collapse);
  applyColumns();

  const key = PANELS_KEY + window.location.pathname;
  const ids = new Set((localStorage.getItem(key) ?? "").split(",").filter(Boolean));
  if(collapse) ids.add(id);
  else ids.delete(id);
  localStorage.setItem(key, [ ...ids ].join(","));

  // lets CodeMirror panes and truncated cells adjust to the new widths
  window.dispatchEvent(new Event("resize"));
}

/**
 * Applies the collapsed state of a content panel.
 * @param {div} panel panel
 * @param {boolean} collapse collapsed state
 */
function showPanel(panel, collapse) {
  panel.classList.toggle("collapsed", collapse);

  const button = panel.firstElementChild;
  const right = button.dataset.right === "true";
  const title = button.dataset.title;
  // the arrow points the way the panel will move
  button.textContent = right !== collapse ? "›" : "‹";
  if(collapse) {
    const label = document.createElement("span");
    label.className = "label";
    label.textContent = title;
    button.append(label);
  }
  button.title = `${collapse ? "Expand" : "Collapse"} ${title}`;
  button.setAttribute("aria-expanded", !collapse);
}

/**
 * Resizes the grid tracks: a collapsed panel shrinks to a strip,
 * its siblings share the freed space.
 */
function applyColumns() {
  // pages without collapsible panels keep the track widths they declared;
  // the editor resizer owns the inline value there
  const content = document.querySelector(".content");
  if(!content || !_collapsible) return;

  // on narrow screens the panels are stacked; the media query supplies the single column
  const shrunk = !stacked() && _panels.some(p => p.classList.contains("collapsed"));
  if(shrunk) {
    // 'min-content' sizes the strip from the rotated label, whatever the font
    content.style.gridTemplateColumns = _panels.map(
      p => p.classList.contains("collapsed") ? "min-content" : "1fr").join(" ");
  } else {
    // restores the track widths declared by the page
    content.style.removeProperty("grid-template-columns");
  }
}
window.addEventListener("resize", applyColumns);

/**
 * Initializes page-wide interactive behavior.
 */
function ready() {
  initPanels();
  // statically rendered tables are not marked by their own code
  markTruncated();
  document.addEventListener("keydown", shortcuts);
  document.getElementById("info")?.addEventListener("click", jumpToError);
  // reflect the stored 'Indent' preference in the checkbox (editor and resource views)
  const indent = document.getElementById("indent");
  if(indent) indent.checked = indentOn();
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

/**
 * Opens a file.
 * @param {string} file optional file name
 */
async function openFile(file) {
  if(_editor.historySize().undo > 0 && !confirm("Replace editor contents?")) return;

  const name = file || fileName();
  try {
    const disk = await request(`editor-open?name=${encodeURIComponent(name)}`);
    const draft = localStorage.getItem(DRAFT + name);
    // set the baseline before setValue, whose synchronous change event runs saveDraft
    _saved = disk;
    _editor.setValue(disk);
    finishFile(name, "File was opened.");
    // apply a newer unsaved draft on top of the saved file (undo reverts to disk)
    if(draft !== null && draft !== disk) _editor.setValue(draft);
  } catch(response) {
    showError(response, name);
  }
}

/**
 * Saves a file.
 */
async function saveFile() {
  // append file suffix
  const raw = fileName();
  let name = raw;
  if(!name.includes(".")) name += ".xq";

  const fileString = document.getElementById("editor").value;
  try {
    const text = await request(`editor-save?name=${encodeURIComponent(name)}`, fileString);
    // drop the draft: the buffer now matches the saved file (also the pre-suffix key)
    localStorage.removeItem(DRAFT + raw);
    localStorage.removeItem(DRAFT + name);
    finishFile(name, "File was saved.");
    refreshDataList(text.split("/"));
  } catch(response) {
    showError(response, name);
  }
}

/**
 * Closes a file.
 */
async function closeFile() {
  const name = fileName();
  // no file open: still clear the (possibly unsaved) untitled buffer
  if(!name) {
    _saved = "";
    _editor.setValue("");
    finishFile("", "Editor was cleared.");
    return;
  }
  try {
    const text = await request(`editor-close?name=${encodeURIComponent(name)}`);
    // baseline before setValue's synchronous change event (see openFile)
    _saved = "";
    localStorage.removeItem(DRAFT + name);
    _editor.setValue("");
    finishFile("", "File was closed.");
    refreshDataList(text.split("/"));
  } catch(response) {
    showError(response);
  }
}

/**
 * Finishes a file operation.
 * @param {string} name new filename
 * @param {string} info info message
 */
function finishFile(name, info) {
  document.getElementById("file").value = name;
  const disabled = name && !name.match(/\.xq(m|l|uery)?$/i);
  document.getElementById("run").disabled = disabled;
  _editor.clearHistory();
  _saved = document.getElementById("editor").value;
  checkButtons();
  setText(info, "info");
  _editor.focus();
}

/**
 * Persists the editor buffer as a local draft, or drops it once it matches the saved file.
 */
function saveDraft() {
  const name = fileName();
  // drafts are an editor-page feature; skip on other CodeMirror pages (no file field)
  if(name === undefined) return;
  const content = document.getElementById("editor").value;
  const key = DRAFT + name;
  try {
    if(content === _saved) localStorage.removeItem(key);
    else localStorage.setItem(key, content);
  } catch { /* storage disabled or full: drafts are best-effort */ }
}

/**
 * Restores the unsaved draft of the untitled buffer on page load, if one exists.
 */
function restoreDraft() {
  const draft = localStorage.getItem(DRAFT + (fileName() ?? ""));
  if(draft) _editor.setValue(draft);
}

/**
 * Refreshes the list of editable files.
 * @param {array} names editable files
 */
function refreshDataList(names) {
  const files = document.getElementById("files");
  files.replaceChildren();
  for(const name of names) {
    const opt = document.createElement("option");
    opt.value = name;
    files.appendChild(opt);
  }
}

/**
 * Refreshes the editor buttons.
 */
function checkButtons() {
  const name = fileName();
  (document.getElementById("open") || {}).disabled = !fileExists(name);
  (document.getElementById("save") || {}).disabled = !name;
  // Close also clears an untitled buffer, so enable it whenever there is content
  (document.getElementById("close") || {}).disabled = !name && !document.getElementById("editor")?.value;
}

/**
 * Checks if the specified file exists.
 * @param {string} name filename
 * @returns {boolean} result of check
 */
function fileExists(name) {
  const files = document.getElementById("files");
  return files && Array.from(files.children).some(file => file.value === name);
}

/**
 * Returns the current file name without file suffix
 * @returns {string} file name
 */
function fileName() {
  const file = document.getElementById("file");
  if(file) return file.value.trim();
}

/**
 * Initializes the panel resizer.
 */
function initResizer() {
  _content = document.querySelector(".content");
  _resizer = document.querySelector(".resizer");

  _width = Number(localStorage.getItem("editorWidth")) || DEFAULT_PANEL_WIDTH;
  applyWidth();
  window.addEventListener("resize", applyWidth);
  _resizer.addEventListener("pointerdown", e => {
    document.addEventListener("pointermove", resize);
    document.addEventListener("pointerup", stopResize);
    _resizer.setPointerCapture(e.pointerId);
  });
}

/**
 * Applies the current panel width. On narrow screens the panels are stacked,
 * and the width declared in style.css takes over.
 */
function applyWidth() {
  if(!stacked()) {
    _content.style.gridTemplateColumns = `${_width}% 1fr`;
  } else {
    _content.style.removeProperty("grid-template-columns");
  }
}

/**
 * Resizes the left panel.
 * @param {e} event
 */
function resize(e) {
  const rect = _content.getBoundingClientRect();
  const width = (e.clientX - rect.left) / rect.width * 100;
  _width = Math.min(MAX_PANEL_WIDTH, Math.max(MIN_PANEL_WIDTH, width));
  applyWidth();
}

/**
 * Stops resizing.
 * @param {e} event
 */
function stopResize(e) {
  document.removeEventListener("pointermove", resize);
  document.removeEventListener("pointerup", stopResize);
  _resizer.releasePointerCapture(e.pointerId);
  localStorage.setItem("editorWidth", _width);
}
