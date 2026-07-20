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

/** Whether the current database resource can be edited. */
let _resourceEditable;
/** Whether the resource editor currently shows (read-only) query output. */
let _queryMode;
/** Saved baseline of the resource editor; edits are detected against it. */
let _resourceSaved;

/** localStorage key for the logs 'ignore entries' filter. */
const IGNORE_KEY = "dba-ignore-logs";
/** localStorage key for the 'Indent' output preference. */
const INDENT_KEY = "dba-indent";

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
    if(truncated[i] !== null) cell.classList.toggle("truncated", truncated[i]);
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
  const lc = !info && msg.match(/(\d+)\/(\d+):/);
  // isolate the error-code line ([XPST0003] …); match a real code, not any '[' in the text
  const s = msg.search(/\[[A-Z]\w*\]/), e1 = msg.indexOf("\n", s);
  if(s > -1) msg = msg.substring(s, e1 > s ? e1 : msg.length);
  msg = msg.replace(/^\[.*?\] /, "").replace(/Stack Trace:.*/, "").replace(/\s+/g, " ");
  if(info) msg = `${info}: ${msg}`;

  // decode HTML entities via an inert parse (no scripts run, no resources load)
  const decoded = new DOMParser().parseFromString(msg, "text/html").documentElement.textContent;
  setText(decoded, "error");

  // with a line/column and an open editor, make a click on the message jump there
  const el = document.getElementById("info");
  if(lc && _editor && _editor.setCursor) {
    el.dataset.line = lc[1];
    el.dataset.column = lc[2];
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
 * Shows a database resource in the editor: the raw editable document, a query
 * result, or the indented document. Serialized output (query result or indented
 * document) is read-only, so editing is only possible on the raw, non-indented
 * view — clearing the query then restores the exact saved content.
 * @param {boolean} enforce enforce execution
 */
async function queryResource(enforce) {
  const input = document.getElementById("input").value.trim();
  const indent = indentOn();
  // re-run whenever the query or the indent preference changes
  const state = input + " " + indent;
  if(!enforce && _dbInput === state) return false;
  _dbInput = state;

  // no query and no indentation: show the raw, editable baseline (never reformatted)
  if(!input && !indent) {
    _queryMode = false;
    if(_resourceEditable) {
      editorReadOnly(false);
      resourceButtons(false);
    }
    _editor.setValue(_resourceSaved);
    setText("", "");
    return;
  }

  // otherwise show read-only serialized output: a query result or the indented document
  _queryMode = true;
  editorReadOnly(true);
  if(_resourceEditable) setDisabled("save-resource", true);

  const self = query("db-query", input || ".");
  register(self);
  try {
    _editor.setValue(await self);
    setText(input ? "Query was successful." : "", input ? "info" : "");
  } catch(response) {
    showError(response);
  } finally {
    if(self === _running) _running = undefined;
  }
}

/**
 * Initializes the resource editor and its button states, honoring the stored
 * 'Indent' preference (an indented document is shown read-only).
 * @param {boolean} editable whether the resource can be edited in place
 */
function initResource(editable) {
  _resourceEditable = editable;
  _queryMode = false;
  _resourceSaved = document.getElementById("editor").value;
  if(editable) _editor.on("change", updateResource);

  if(document.getElementById("input") && indentOn()) {
    // XML resource with indentation enabled: show the indented document read-only
    queryResource(true);
  } else if(editable) {
    resourceButtons(false);
  } else {
    // binary, value, or oversized XML: shown read-only, content inlined server-side
    editorReadOnly(true);
  }
}

/**
 * Refreshes the resource controls after an edit: toggles the Save, Copy, query
 * and Indent controls to match the modification state. Ignored while serialized
 * output is shown.
 */
function updateResource() {
  if(_queryMode) return;
  resourceButtons(document.getElementById("editor").value !== _resourceSaved);
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
 * Applies the resource button states for the given modification state.
 * @param {boolean} dirty whether the document has unsaved changes
 */
function resourceButtons(dirty) {
  setDisabled("save-resource", !dirty);
  // querying, copying or indenting an unsaved document would ignore the pending edits
  setDisabled("copy-resource", dirty);
  setDisabled("input", dirty);
  setDisabled("indent", dirty);
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
 * Saves the edited content of a database resource, updating the saved baseline.
 */
async function saveResource() {
  if(_queryMode) return;
  const name = document.getElementById("name").value;
  const resource = document.getElementById("resource").value;
  const content = document.getElementById("editor").value;
  const url = `db-save?name=${encodeURIComponent(name)}&resource=${encodeURIComponent(resource)}`;
  try {
    await request(url, content);
    _resourceSaved = content;
    resourceButtons(false);
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
        saveDraft();
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
        saveDraft();
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
      // sibling column (e.g. a long resource list) can't shrink it. the reserve
      // leaves room for the chrome below <main>: hr + footer + small + body margin.
      // on narrow (stacked) layouts, cap the height so the editor stays compact
      // instead of filling the viewport and pushing the output/buttons off-screen
      const height = elem => window.innerWidth <= 800 ? 200 :
        Math.max(192, window.innerHeight - elem.getBoundingClientRect().top - 52);
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
 * Initializes page-wide interactive behavior.
 */
function ready() {
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
