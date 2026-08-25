/** Shared code of all DBA pages. */

/*
 * The DBA keeps its state in three places. Which one is right follows from a single question:
 * who must see the value?
 *
 * - URL parameters: everything a link must be able to reproduce. The sort key and the page of a
 *   table, the job a detail view shows, the info and error message of a redirect. If a colleague
 *   opens the address and sees something else, the value belongs here.
 * - localStorage: what the user set up and expects to find again, and what no one else should
 *   see. Editor drafts, the directory and the open documents of the Workspace view, panel
 *   splits and collapsed panels, the 'Live' and 'Indent' preferences, the log filter. Keys
 *   that describe one page are scoped with pageKey; the rest are shared by every page of the DBA.
 * - Server session: only what the server itself must know, which is the login. Anything else
 *   would make two browsers fight over one value and the server answer differently to the
 *   same request.
 *
 * Global options that outlive the browser (timeout, memory, permission, table rows) are not
 * state: they are configuration, and live in the .dba.xml of the database directory.
 */

/** Root of the web application; empty if it is deployed in the root context. HTTP requests are
    relative and need no prefix, but a WebSocket URL is absolute. */
const CONTEXT_PATH = document.documentElement.dataset.context ?? "";

/** Connections to the WebSocket endpoints, by path (promises, resolved with open WebSockets). */
const _sockets = {};
/** Handlers for the messages of a WebSocket endpoint, by path; a page registers its own. */
const _handlers = {};
/** Actions that a 'Live' checkbox starts, by the name in its 'data-live'; a page registers its own. */
const _live_actions = {};

/** Number of the last query that was started in the editor panel. */
let _run = 0;

/** Number of the query whose outcome is awaited (0: none). */
let _pending = 0;

/** Whether the status line reports that a request is still running. */
let _waiting = false;

/** Handle of the timer that repeats what the 'Live' checkbox controls. */
let _live;

/** Moves the editor to a line and column that an error message names; registered by editor.js
    where a page has an editor, and absent on the pages that do not load it. */
let _locate;

/** Delay before a query is reported as running. */
const RESULT_DELAY = 500;

/** localStorage key prefix for the 'Live' checkbox of a page. */
const LIVE_KEY = "dba-live-";

/** Pause between a result and the next refresh, in milliseconds; the server supplies it in
    seconds, as the global option it keeps. Refreshes are chained: the pause starts when a
    result arrives, so a slow one cannot queue up others. */
const REFRESH_INTERVAL = Number(document.documentElement.dataset.interval) * 1000;

/** Content panels of the current page, in grid track order. */
let _panels = [];

/** Whether at least one of the panels can be collapsed. */
let _collapsible = false;

/** Longest message that is accepted by the WebSocket endpoints.
    Kept well below the 'maxTextMessageSize' of the WebSocket servlet (see web.xml), which also
    needs to accommodate the UTF-8 encoding of the message. */
const MAX_MESSAGE_LENGTH = 1000000;

/** Link to the resized panel grid. */
let _content;

/** Resized grid tracks, as weights ({ column: [], row: [] }); the first row is not resized. */
let _split;

/** localStorage key prefix for the panel splits of a page. */
const SPLIT_KEY = "dba-split-";

/** Smallest a panel gets by dragging, in pixels. */
const MIN_PANEL_SIZE = 60;

/** localStorage key prefix for the collapsed content panels of a page. Only the panels that
    were folded by hand are stored; the others follow the page, which assigns their state.
    Versioned: panel ids used to be row-based, the grid layout numbers them flat, and the
    collapsed ones used to be listed instead of stating the state of both. */
const PANELS_KEY = "dba-panels-v3-";

/**
 * Returns a localStorage key that describes the current page, not the DBA as a whole.
 * @param {string} prefix key prefix
 * @returns {string} key
 */
function pageKey(prefix) {
  return prefix + window.location.pathname;
}

/**
 * Indicates whether the layout has stacked the panels into a single column.
 * The breakpoint belongs to style.css; this only reads the flag it sets.
 * @returns {boolean} stacked state
 */
function stacked() {
  return getComputedStyle(document.documentElement).getPropertyValue("--stacked").trim() === "1";
}

/**
 * Indicates whether the table row containing a checkbox is currently shown.
 * @param {HTMLInputElement} input checkbox
 * @returns {boolean} visibility
 */
function rowVisible(input) {
  return input.closest("tr")?.style.display !== "none";
}

/**
 * Returns the checkboxes that select the entries of a form: the ones in its table. A checkbox
 * elsewhere in the same form is a setting of an action (compress, binary), not a selection.
 * @param {HTMLFormElement} form form
 * @returns {NodeList} checkboxes
 */
function selection(form) {
  return form.querySelectorAll("table input[type=checkbox]");
}

/**
 * Toggles the selection of all checkboxes in a form.
 * @param {HTMLInputElement} source clicked header checkbox
 */
function toggle(source) {
  for(const input of selection(getForm(source))) {
    input.checked = source.checked && rowVisible(input);
  }
  buttons(source);
}

/**
 * Refreshes all buttons and checkboxes of a form.
 * @param {HTMLInputElement} source clicked checkbox. if undefined, all forms will be refreshed
 */
function buttons(source) {
  for(const form of (source ? [ getForm(source) ] : document.querySelectorAll("form"))) {
    // count selected items and refresh header checkbox
    let count = 0, checked = 0, header;
    for(const input of selection(form)) {
      if(rowVisible(input)) {
        if(input.name) {
          count++;
          if(input.checked) checked++;
        } else {
          // the select-all box of the table header; the named ones are the entries
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
 * @param {HTMLElement} source element
 * @returns {HTMLFormElement} enclosing form
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
 * @param {HTMLElement} root part of the page to measure; the whole of it by default
 */
function markTruncated(root = document) {
  // read all overflow states first, then write classes: interleaving the two forces
  // a full re-layout per cell, which is O(rows) for a fixed table and dominates on large logs
  const cells = root.querySelectorAll("table.fixed td");
  const truncated = [];
  for(const cell of cells) {
    truncated.push(cell.classList.contains("expanded") ? null : cell.scrollWidth > cell.clientWidth);
  }
  cells.forEach((cell, i) => {
    if(truncated[i] === null) return;
    // a cell that holds a link is not expanded: a click on it is the link's, and the checkbox
    // and the chevron of the cell would compete with it for the same spot
    cell.classList.toggle("truncated", truncated[i] && !cell.querySelector("a"));
    // the tooltip is the only access to the clipped text
    if(truncated[i]) cell.title = cell.textContent;
    else cell.removeAttribute("title");
  });
  // the content that was measured also decides whether the panel it fills scrolls
  markScrollbars();
}

/**
 * Places the collapse chevrons. A chevron sits just inside the scrollbar of the list below it,
 * and takes the room the scrollbar would have needed while the panel does not scroll.
 */
function markScrollbars() {
  for(const panel of _panels) {
    // an editor brings a scrollbar of its own; only a panel that scrolls as a whole can be
    // free of one. The difference between the two widths is the scrollbar itself
    const pane = panel.querySelector(".pane");
    if(pane) panel.classList.toggle("no-scrollbar", pane.offsetWidth === pane.clientWidth);
  }
}

// the event must not reach markTruncated, whose argument is the part of the page to measure
window.addEventListener("resize", () => markTruncated());

/**
 * Asks for confirmation, naming the action and the selected entries. The answer arrives after
 * the click has been dealt with, so the first click is always refused and the button clicks
 * itself again once the action is confirmed; the second click submits its form.
 * @param {HTMLButtonElement} button clicked button
 * @param {string} action action label
 * @returns {boolean} true if the form may be submitted
 */
function confirmAction(button, action) {
  if(button.dataset.confirmed) {
    delete button.dataset.confirmed;
    return true;
  }
  // the entries are named by the table the question is asked from; the number is what the
  // question adds, and a long list of paths would only bury it
  let count = 0;
  for(const input of selection(getForm(button))) {
    if(input.name && input.checked && rowVisible(input)) count++;
  }
  const message = count
    ? `${action} ${count} ${count === 1 ? "entry" : "entries"}?`
    : "Are you sure?";
  confirmDialog(message).then(ok => {
    if(ok) {
      button.dataset.confirmed = "true";
      button.click();
    }
  });
  return false;
}

/**
 * Displays text with the specified type.
 * @param {string} message message to display
 * @param {string} type message type (info, warning, error)
 */
function setText(message, type) {
  const info = document.getElementById("info");
  info.className = type;
  info.textContent = message;
  info.title = message;
}

/**
 * Indicates that the files of a form are being uploaded.
 * @param {HTMLFormElement} form submitted form
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
 * @param {string} url URL to be called
 * @param {string} data data to be sent
 * @returns {Promise} promise
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
 * Returns the connection to the specified endpoint, and opens it if required.
 * @param {string} path path, relative to the WebSocket root
 * @returns {Promise} promise, resolved with an open WebSocket
 */
function socket(path) {
  if(!_sockets[path]) {
    _sockets[path] = new Promise((resolve, reject) => {
      const scheme = location.protocol === "https:" ? "wss" : "ws";
      const ws = new WebSocket(`${scheme}://${location.host}${CONTEXT_PATH}/ws/dba${path}`);
      ws.onopen = () => resolve(ws);
      // the endpoint determines how a result is displayed; the server does not label it
      ws.onmessage = event => showMessage(path, event.data);
      // a refused handshake and a lost connection both invalidate the cached promise,
      // so that the next message opens a new one
      ws.onerror = () => {
        delete _sockets[path];
        reject(new Error("No connection to the server. " +
          "If you use a proxy server, check if WebSockets are enabled."));
      };
      ws.onclose = () => {
        delete _sockets[path];
        if(_pending) {
          endRequest();
          setText("Connection to the server was lost.", "error");
        }
      };
    });
  }
  return _sockets[path];
}

/**
 * Sends a message to a WebSocket endpoint.
 * @param {string} path path, relative to the WebSocket root
 * @param {object} message message to be sent
 * @returns {Promise} promise, resolved with true if the message was sent
 */
async function sendMessage(path, message) {
  // the server closes the connection without a response if a message exceeds its size limit
  const string = JSON.stringify(message);
  if(string.length > MAX_MESSAGE_LENGTH) {
    endRequest();
    showError(`Input is too long (maximum: ${MAX_MESSAGE_LENGTH} characters).`);
    return false;
  }
  try {
    (await socket(path)).send(string);
    return true;
  } catch(ex) {
    endRequest();
    showError(ex.message);
    return false;
  }
}

/**
 * Shows a message that was pushed by the server. A message that carries a run number reports
 * the outcome of a request; one without is a notification. What it means is known by the page
 * that opened the connection, which registers its handler in _handlers.
 * @param {string} path path of the endpoint that pushed the message
 * @param {string} data JSON message
 */
function showMessage(path, data) {
  const json = JSON.parse(data);
  // drop the outcome of a request that was stopped or superseded by a newer one
  if(json.run !== undefined && json.run !== _pending) return;
  // the outcome ends the request; so does an error, which is raised before it becomes a job.
  // The wait message is revoked before a handler writes its own, as an outcome that reports
  // nothing (a rendered document) would leave the page waiting forever
  if(json.run !== undefined || json.type === "error") {
    if(endRequest()) setText("", "");
  }
  if(json.type === "error") showError(json.message, undefined, json);
  _handlers[path]?.(json);
}

/**
 * Registers a request whose outcome is awaited.
 * @returns {number} number of the run
 */
function startRequest() {
  return _pending = ++_run;
}

/**
 * Reports a request that takes longer, and offers to stop it.
 * @param {number} run number of the run
 */
function awaitResult(run) {
  setTimeout(() => {
    if(_pending === run) {
      setText("Please wait…", "warning");
      _waiting = true;
      setDisabled("stop", false);
    }
  }, RESULT_DELAY);
}

/**
 * Gives up the request whose outcome was awaited.
 * @returns {boolean} whether the status line still reports that it is running
 */
function endRequest() {
  _pending = 0;
  setDisabled("stop", true);
  const waiting = _waiting;
  _waiting = false;
  return waiting;
}

/**
 * Asks the server for a panel. A folded panel is requested as well: opening it must show what
 * is there now, not what was there when it was folded away. The order it shows is kept unless
 * another one is requested; a new order starts at the first page.
 * @param {string} path path of the endpoint that serves the panel
 * @param {string} id id of the panel
 * @param {object} message message identifying the panel
 * @param {string} sort sort key; if omitted, the shown order is kept
 * @param {number} page page; if omitted, the first one
 */
function requestPanel(path, id, message, sort, page) {
  const shown = document.querySelector(`#${id} [data-sort]`);
  sendMessage(path, Object.assign(message, {
    sort: sort ?? shown?.dataset.sort ?? "",
    page: page ?? 1
  }));
}

/**
 * Shows a panel that was pushed by the server.
 * @param {string} id id of the panel
 * @param {string} html panel contents
 */
function fillPanel(id, html) {
  const pane = document.getElementById(id);
  pane.innerHTML = html;
  // a panel with nothing to show is not shown at all, and gives up its grid track
  const panel = pane.closest(".panel"), empty = !html;
  if(panel.classList.contains("hidden") !== empty) {
    panel.classList.toggle("hidden", empty);
    applyColumns();
    // lets the editor and the truncated cells adjust to the new widths
    window.dispatchEvent(new Event("resize"));
  }
  // the panel arrives after the shared setup ran, so its buttons are checked here
  buttons();
  markTruncated(pane);
}

/**
 * Points out the selected entry of a panel.
 * @param {string} id id of the panel
 * @param {string} value selected value
 */
function mark(id, value) {
  for(const link of document.querySelectorAll(`#${id} a[data-select]`)) {
    link.classList.toggle("selected", link.dataset.select === value);
  }
}

/**
 * Follows the sort and page links of the list panels in place: they name what a panel shows,
 * so the panel is asked for it again instead of the page being reloaded.
 * @param {object} panels function that requests the panel again, per panel id
 */
function followPanelLinks(panels) {
  document.addEventListener("click", event => {
    const link = event.target.closest("a[href]");
    // a link that selects an entry brings its own handler
    if(!link || link.dataset.select) return;
    const panel = link.closest(Object.keys(panels).map(id => `#${id}`).join(", "));
    if(!panel) return;
    const params = new URL(link.href, window.location.href).searchParams;
    if(!params.has("sort") && !params.has("page")) return;
    event.preventDefault();
    panels[panel.id](params.get("sort") ?? "", Number(params.get("page")) || 1);
  });
}

/**
 * Displays an error message.
 * @param {response|string} response HTTP response, or an error message
 * @param {string} info optional info
 * @param {object} position optional error position ({ line, column })
 */
function showError(response, info, position) {
  if(response.status === 460) return;

  // normalize error message
  let msg = typeof response === "string" ? response :
    response.statusText.match(/\[\w+\]/g) ? response.statusText : response.responseText;
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
  el.classList.toggle("locatable", Boolean(line && _locate));
  if(line && _locate) {
    el.dataset.line = line;
    el.dataset.column = column;
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
  if(info.classList.contains("locatable")) {
    _locate(Number(info.dataset.line), Number(info.dataset.column));
  }
}

/**
 * Indicates whether the 'Live' checkbox of the current page is ticked.
 * @returns {boolean} live state
 */
function liveOn() {
  return document.getElementById("live")?.checked === true;
}

/**
 * Restores the stored state of the 'Live' checkbox. What it controls is started by the page,
 * which knows if the first request is already on its way.
 */
function initLive() {
  const live = document.getElementById("live");
  if(!live) return;
  const stored = localStorage.getItem(pageKey(LIVE_KEY));
  if(stored !== null) live.checked = stored === "true";
}

/**
 * Persists the state of the 'Live' checkbox and starts what it controls.
 */
function liveChanged() {
  const live = document.getElementById("live");
  localStorage.setItem(pageKey(LIVE_KEY), live.checked);
  // a repeat that was already scheduled must not slip through after unticking
  clearTimeout(_live);
  if(live.checked) _live_actions[live.dataset.live]?.();
}

/**
 * Keeps a dialog open when Escape is pressed in one of its editors: there, Escape is how the
 * focus leaves the editor (Escape, then Tab), not how the dialog is dismissed.
 */
document.addEventListener("cancel", (event) => {
  if(document.activeElement?.closest(".cm-editor")) event.preventDefault();
}, true);

/**
 * Opens a modal dialog: a form that needs more room than a question can be asked in.
 * @param {string} id id of the dialog, without its suffix
 */
function showDialog(id) {
  document.getElementById(`${id}-dialog`)?.showModal();
}

/**
 * Indicates whether a dialog is open. A dialog is answered by clicking the button it was
 * opened from, so a view that refreshes itself must leave that button in the document.
 * @returns {boolean} dialog state
 */
function dialogOpen() {
  return document.querySelector("dialog[open]") !== null;
}

/**
 * Opens one of the dialogs that answer a question, and waits for the answer. Their forms are
 * submitted with 'method=dialog', so the clicked button's value is the answer; Escape closes
 * the dialog with an empty one.
 * @param {string} id id of the dialog, without its suffix
 * @returns {Promise} promise, resolved with the value of the clicked button
 */
function askDialog(id) {
  const dialog = document.getElementById(`${id}-dialog`);
  dialog.returnValue = "";
  dialog.showModal();
  return new Promise(resolve => dialog.addEventListener("close",
    () => resolve(dialog.returnValue), { once: true }));
}

/**
 * Asks a question and waits for one of the answers it offers. The last answer is the way out:
 * it is the one that Escape and a dismissed dialog give, so it carries an empty value.
 * @param {string} message question to be answered
 * @param {Array} answers answers, as [ value, label ] pairs
 * @returns {Promise} promise, resolved with the value of the chosen answer
 */
function askQuestion(message, answers) {
  document.getElementById("confirm-text").textContent = message;
  const dialog = document.getElementById("confirm-dialog");
  dialog.querySelector(".buttons").replaceChildren(...answers.map(([ value, label ]) => {
    const button = document.createElement("button");
    button.value = value;
    button.textContent = label;
    return button;
  }));
  return askDialog("confirm");
}

/**
 * Asks for confirmation.
 * @param {string} message question to be answered
 * @returns {Promise} promise, resolved with true if the action was confirmed
 */
async function confirmDialog(message) {
  return await askQuestion(message, [ [ "ok", "OK" ], [ "", "Cancel" ] ]) === "ok";
}

/**
 * Asks for a text value.
 * @param {string} message question to be answered
 * @param {string} value value to start from
 * @returns {Promise} promise, resolved with the entered text, or null if it was cancelled
 */
async function promptDialog(message, value) {
  const input = document.getElementById("prompt-input");
  document.getElementById("prompt-text").textContent = message;
  input.value = value ?? "";
  const answer = askDialog("prompt");
  // the dialog is open now: the text is offered for replacement, as a prompt does
  input.select();
  return await answer === "ok" ? input.value : null;
}

/**
 * Publishes the width of a scrollbar, so that the collapse button of a panel can sit beside
 * one instead of on top of it. It is the same for every panel, and is measured once.
 */
function measureScrollbar() {
  const probe = document.createElement("div");
  probe.style.cssText = "position:absolute; visibility:hidden; overflow:scroll; width:100px";
  document.body.append(probe);
  document.documentElement.style.setProperty("--scrollbar",
    `${probe.offsetWidth - probe.clientWidth}px`);
  probe.remove();
}

/**
 * Opens a file chooser; choosing files submits the form it belongs to.
 * @param {string} id id of the file input
 */
function chooseUpload(id) {
  document.getElementById(id).click();
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
 * Handles global keyboard shortcuts.
 * @param {Event} event keydown event
 */
function shortcuts(event) {
  if(event.key === "Escape") {
    setText("", "");
    return;
  }
  // ignore key presses while typing or combined with modifier keys
  const target = event.target;
  if(event.ctrlKey || event.metaKey || event.altKey ||
     target.matches("input, textarea, select") || target.closest(".cm-editor")) return;
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
 * Returns the key the folded panels of the current page are stored under. A panel is addressed
 * by its position, so a page that shows different panels in its subviews keeps a state for
 * each of them: the subview is what the page calls itself in 'data-panels'.
 * @returns {string} key
 */
function panelsKey() {
  const subview = document.querySelector(".content")?.dataset.panels;
  return pageKey(PANELS_KEY) + (subview ? `/${subview}` : "");
}

/**
 * Returns the panels that were folded by hand in the current subview.
 * @returns {object} collapsed state, by panel id
 */
function storedPanels() {
  return JSON.parse(localStorage.getItem(panelsKey()) ?? "{}");
}

/**
 * Makes the side-by-side content panels of a page collapsible.
 */
function initPanels() {
  const content = document.querySelector(".content");
  if(!content) return;
  // panels spanning all columns have no track of their own and never collapse
  const panels = [ ...content.children ].filter(p => p.matches(".panel"));
  if(panels.length < 2) return;

  // every panel owns a grid track, whether or not it can be collapsed
  _panels = panels;

  // the markup supplies the state of every panel that was not folded by hand
  const stored = storedPanels();
  panels.forEach((panel, p) => {
    // the label of the collapsed strip is the panel's own, or the first word of its heading:
    // what follows a separator names the entry that is shown, which the panel outlives.
    // A panel with neither (the editor panes) stays as it is
    const heading = panel.querySelector("h2, h3");
    const label = panel.dataset.label ?? heading?.textContent.split(/[»:]/)[0].trim();
    if(!label) return;
    const id = `${p}`;
    const button = document.createElement("button");
    button.type = "button";
    button.className = "collapse";
    // the last panel folds to the right, all others to the left; a panel at the right edge
    // of a page that ends with more than one of them says so itself
    button.dataset.right = panel.dataset.fold === "right" || p === panels.length - 1;
    button.dataset.title = label;
    button.addEventListener("click", () => togglePanel(panel, id));
    panel.prepend(button);
    _collapsible = true;
    showPanel(panel, stored[id] ?? panel.classList.contains("collapsed"));
  });
  applyColumns();
}

/**
 * Collapses or expands a content panel and persists the new state.
 * @param {HTMLElement} panel panel to be toggled
 * @param {string} id panel id
 */
function togglePanel(panel, id) {
  const collapse = !panel.classList.contains("collapsed");
  showPanel(panel, collapse);
  applyColumns();

  const stored = storedPanels();
  stored[id] = collapse;
  localStorage.setItem(panelsKey(), JSON.stringify(stored));

  // lets CodeMirror panes and truncated cells adjust to the new widths
  window.dispatchEvent(new Event("resize"));
}

/**
 * Applies the collapsed state of a content panel.
 * @param {HTMLElement} panel panel
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
    // the strip is drawn tight around its text: the trailing space keeps the rotated label
    // off the edge, as the one after a heading keeps it off the chevron
    label.textContent = `${title} `;
    button.append(label);
  }
  button.title = `${collapse ? "Expand" : "Collapse"} ${title}`;
  button.setAttribute("aria-expanded", !collapse);
}

/**
 * Resizes the grid tracks: a panel with nothing to show gets none, a collapsed one a strip,
 * and the rest share the freed space.
 */
function applyColumns() {
  // pages without collapsible panels keep the track widths they declared;
  // the editor resizer owns the inline value there
  const content = document.querySelector(".content");
  if(!content || !_collapsible) return;

  // on narrow screens the panels are stacked; the media query supplies the single column
  const state = p => p.classList.contains("hidden") ? null :
    p.classList.contains("collapsed") ? "min-content" : "";
  const tracks = _panels.map(state);
  if(!stacked() && tracks.some(t => t !== "")) {
    // the panels that stay open keep the track widths the page declared, and
    // 'min-content' sizes a folded strip from its rotated label, whatever the font
    const widths = getComputedStyle(content).getPropertyValue("--columns").trim().split(/\s+/);
    content.style.gridTemplateColumns = tracks
      .map((t, i) => t === "" ? widths[i] || "1fr" : t)
      .filter(t => t !== null).join(" ");
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
  measureScrollbar();
  initPanels();
  // statically rendered tables are not marked by their own code
  markTruncated();
  document.addEventListener("keydown", shortcuts);
  document.getElementById("info")?.addEventListener("click", jumpToError);
  initLive();
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
 * Writes a selection to the address bar, as a step of its own: the back button returns to what
 * was shown before. A parameter with an empty value is dropped.
 * @param {object} params selected values, by parameter name
 */
function pushParams(params) {
  let url = window.location.href;
  for(const [ name, value ] of Object.entries(params)) url = replaceParam(url, name, value);
  window.history.pushState({}, "", url);
}

/**
 * Makes the panel grid resizable: a '.resizer' drags a split between two columns, a
 * '.resizer-row' one between two rows. 'data-split' numbers the split of its axis, so
 * handles that sit on the same split drag it together.
 */
function initResizers() {
  _content = document.querySelector(".content");
  if(!_content) return;

  // the page declares the initial tracks; a stored split wins, but only if it still fits the
  // grid: a page that changed its layout must not be sized by what an older one stored
  const stored = JSON.parse(localStorage.getItem(pageKey(SPLIT_KEY)) ?? "{}");
  const style = getComputedStyle(_content);
  const fitting = (tracks, count) =>
    Array.isArray(tracks) && tracks.length === count ? tracks : undefined;
  _split = {
    column: fitting(stored.column, style.gridTemplateColumns.split(" ").length),
    // the first row holds the toolbar, which is not resized
    row: fitting(stored.row, style.gridTemplateRows.split(" ").length - 1)
  };
  applySplit();
  window.addEventListener("resize", applySplit);

  for(const handle of _content.querySelectorAll(".resizer, .resizer-row")) {
    handle.addEventListener("pointerdown", event => {
      const drag = e => resize(e, handle);
      const stop = e => {
        document.removeEventListener("pointermove", drag);
        document.removeEventListener("pointerup", stop);
        handle.releasePointerCapture(e.pointerId);
        localStorage.setItem(pageKey(SPLIT_KEY), JSON.stringify(_split));
      };
      document.addEventListener("pointermove", drag);
      document.addEventListener("pointerup", stop);
      handle.setPointerCapture(event.pointerId);
    });
  }
}

/**
 * Applies the dragged tracks. On narrow screens the panels are stacked, and the
 * tracks declared in style.css take over.
 */
function applySplit() {
  const apply = (name, tracks, prefix) => {
    // on narrow screens the media query stacks the panels; the tracks that the page declared
    // are left alone, as removing them would leave the grid without any at all
    if(tracks && !stacked()) {
      _content.style.setProperty(name, prefix + tracks.map(t => `${t}fr`).join(" "));
    }
  };
  apply("--columns", _split.column, "");
  // the first row holds the toolbar, which keeps the height it needs
  apply("--rows", _split.row, "auto ");
}

/**
 * Moves a split to the pointer. What the dragged panel gains is taken from every panel behind
 * it, in proportion to the space each of them has: dragging the first split must not squeeze
 * its neighbour while the panels beyond it keep their width.
 * @param {Event} e pointer event
 * @param {HTMLElement} handle dragged handle
 */
function resize(e, handle) {
  const row = handle.matches(".resizer-row");
  const style = getComputedStyle(_content);
  // used track sizes, in pixels; written back as weights, which keeps the layout
  const tracks = (row ? style.gridTemplateRows : style.gridTemplateColumns).split(" ").map(parseFloat);
  const gap = parseFloat(row ? style.rowGap : style.columnGap) || 0;
  // the first row is the toolbar and keeps its height; all columns are resized
  const first = (row ? 1 : 0) + Number(handle.dataset.split ?? 0);

  const rect = _content.getBoundingClientRect();
  let start = row ? rect.top : rect.left;
  for(let t = 0; t < first; t++) start += tracks[t] + gap;

  // the tracks behind the split give way together and shrink in proportion, so the smallest
  // of them is the one that reaches the minimum first and sets the limit
  const after = tracks.slice(first + 1);
  const rest = after.reduce((a, b) => a + b, 0);
  const room = rest * (1 - MIN_PANEL_SIZE / Math.min(...after));
  const size = Math.min(tracks[first] + room, Math.max(MIN_PANEL_SIZE,
    (row ? e.clientY : e.clientX) - start));

  const delta = size - tracks[first];
  tracks[first] = size;
  after.forEach((track, t) => { tracks[first + 1 + t] = track - delta * track / rest; });

  _split[row ? "row" : "column"] = tracks.slice(row ? 1 : 0);
  applySplit();
}
