/** Stores view: the stores of the server, their entries and their values. */

/** Path of the endpoint that serves the panels of this view. */
const STORES_WS = "/stores";

/** Selected store; the default store is named by the empty string, and is what the view opens
    with. It is part of the address: a link reproduces what the panels show, and the browser
    history steps through the selections that were made. */
let _store = "";

/** Path of the shown level. Its first step is the key of the entry, which is part of the
    address as well; the steps into the value are client-side state, as a level of a value has
    no address of its own. The server states the path it rendered, and the client adopts it. */
let _path = [];

/** Step of the child that is looked at within the level, or null. The level is what the entries
    panel lists; the selected child is what the value panel shows. */
let _selected = null;

/** Whether the entries panel opens its dialog as soon as it arrives. */
let _add = false;

/** Whether the first child of the level is chosen as soon as the panel arrives. */
let _first = false;

/**
 * Shows another store. Its entries replace the ones that were listed before, and the value of
 * the previous store is closed with it.
 * @param {string} name store; the default store is named by the empty string
 */
function selectStore(name) {
  if(name === _store) return;
  _store = name;
  _path = [];
  _selected = null;
  pushSelection();
  mark("stores-panel", _store);
  // a store that holds nothing yet is not listed: the list states which one is being filled
  refreshStores();
  // a store is opened on its first entry, as it is when the view is loaded
  _first = true;
  showLevel();
}

/**
 * Returns the key of the entry that is shown.
 * @returns {string} key; empty if the store itself is all that is chosen
 */
function entryKey() {
  return selectionPath()[0] ?? "";
}

/**
 * Requests what the path leads to: the children of the level, and the value itself.
 */
function showLevel() {
  refreshEntries();
  refreshValue();
}

/**
 * Shows the entries panel that was pushed by the server.
 * @param {string} html panel contents
 */
function showEntries(html) {
  fillPanel("entries-panel", html);
  syncPath();
  // the dialog is part of the replaced markup, and brings a new text area with it
  loadCodeMirror("xquery", [ "add-value" ]);
  if(_add) {
    _add = false;
    _first = false;
    showDialog("add");
  } else if(_first) {
    _first = false;
    // the marker that opens a child is no choice of one: it brings a title, the label none
    const link = document.querySelector("#entries-panel a[data-step]:not([title])");
    if(link) selectChild(link.dataset.step);
  }
}

/**
 * Asks for the name of a store and fills it. A store is its entries: an empty one does not
 * exist and would be gone with the next visit, so the first entry is asked for right away.
 * @returns {Promise} promise
 */
async function newStore() {
  const name = await promptDialog("Name of the store:");
  if(!name) return;
  if(name === _store) {
    showDialog("add");
  } else {
    // the dialog belongs to the level, and is opened once the panel that brings it has arrived
    _add = true;
    selectStore(name);
  }
}

/**
 * Writes the selection to the address bar, as a step of its own: the back button returns to
 * what was shown before.
 */
function pushSelection() {
  let url = replaceParam(window.location.href, "name", _store);
  // the level is stated as a whole, so that an update returns to it; within the store, the
  // entry that is shown is what names the selection
  url = replaceParam(url, "path", pathToString(_path));
  url = replaceParam(url, "key", _path.length ? "" : entryKey());
  window.history.pushState({}, "", url);
}

/**
 * Adopts the selection of the address bar, after a step in the browser history.
 */
function popSelection() {
  adoptSelection();
  mark("stores-panel", _store);
  refreshStores();
  showLevel();
}

/**
 * Adopts the selection of the address bar: the store that was chosen, and the entry it shows.
 */
function adoptSelection() {
  const params = new URLSearchParams(window.location.search);
  _store = params.get("name") ?? "";
  const path = params.get("path");
  _path = parsePath(path);
  // within the store, the address names the entry that is shown; deeper, the level is all
  // that is reproduced, and the server states what it rendered
  _selected = _path.length ? null : (params.get("key") || null);
}

/**
 * Requests the stores panel.
 * @param {string} sort sort key; if omitted, the shown order is kept
 * @param {number} page page; if omitted, the first one
 */
function refreshStores(sort, page) {
  requestPanel(STORES_WS, "stores-panel", { type: "stores", name: _store }, sort, page);
}

/**
 * Requests the children of the level the path leads to.
 * @param {string} sort sort key; if omitted, the shown order is kept
 * @param {number} page page; if omitted, the first one
 */
function refreshEntries(sort, page) {
  requestPanel(STORES_WS, "entries-panel",
    { type: "entries", name: _store, path: pathToString(_path),
      selected: stepToString(_selected) }, sort, page);
}

/**
 * Requests the value the path leads to, and with it the text the editor shows.
 */
function refreshValue() {
  sendMessage(STORES_WS, { type: "value", name: _store,
    path: pathToString(selectionPath()) });
}

/**
 * Shows the value panel and the value it refers to.
 * @param {object} json panel contents, value text and edit state
 */
function showValue(json) {
  fillPanel("value-panel", json.html);
  _editor.setValue(json.text);
  setDisabled("save-value", !json.editable);
  editorReadOnly(!json.editable);
}

/**
 * Replaces the value the path leads to with the result of the edited expression.
 * @returns {Promise} promise
 */
async function saveValue() {
  const path = encodeURIComponent(pathToString(selectionPath()));
  try {
    await request(`store-save?name=${encodeURIComponent(_store)}&path=${path}`, editorValue());
    setText("Value was stored.", "info");
    // the level lists the value that has just changed
    refreshEntries();
  } catch(response) {
    showError(response);
  }
}

/**
 * Shows a child of the level: the level it belongs to stays, so that the values around it
 * remain in reach.
 * @param {string} step step that leads to the child
 */
function selectChild(step) {
  _selected = parseStep(step);
  // the panel is not asked for again: what is marked is all that changes there
  markSelected();
  pushSelection();
  refreshValue();
}

/**
 * Points out the selected child of the shown level.
 */
function markSelected() {
  const step = stepToString(_selected);
  for(const link of document.querySelectorAll("#entries-panel a[data-step]:not([title])")) {
    link.classList.toggle("selected", link.dataset.step === step);
  }
}

/**
 * Opens a child of the level: what it holds becomes the level that is listed.
 * @param {string} step step that leads to the child
 */
function descend(step) {
  _path.push(parseStep(step));
  // the level that is opened shows its first child, as a store does
  _selected = null;
  _first = true;
  pushSelection();
  showLevel();
}

/**
 * Returns to a level the path led through; the child that was opened stays selected.
 * @param {number} depth number of steps that are kept
 */
function truncatePath(depth) {
  const kept = Number(depth);
  if(kept === _path.length && _selected === null) return;
  _selected = _path[kept] ?? null;
  _path.length = kept;
  pushSelection();
  showLevel();
}

/**
 * Returns the path of the value that is shown: the selected child of the level, or the level
 * itself if no child is chosen.
 * @returns {Array} path
 */
function selectionPath() {
  return _selected === null ? _path : [ ..._path, _selected ];
}

/**
 * Returns the text of a step: a name that needs no quotes, or JSON with its dots escaped.
 * @param {*} step step; null if none is selected
 * @returns {string} text; empty if there is no step
 */
function stepToString(step) {
  if(step === null) return "";
  if(typeof step === "string" && /^\p{L}[\p{L}\p{N}_-]*$/u.test(step)) return step;
  // the text is compared with the one the server wrote: a slash is escaped as it escapes it
  return JSON.stringify(step).replaceAll("/", "\\/").replaceAll(".", "\\u002E");
}

/**
 * Returns the text of a path: its steps, separated by dots.
 * @param {Array} path path
 * @returns {string} text
 */
function pathToString(path) {
  // a dot survives a query string as itself, which a slash does not
  return path.map(stepToString).join(".");
}

/**
 * Returns the step that a text denotes.
 * @param {string} text text
 * @returns {*} step
 */
function parseStep(text) {
  return /^["{]|^-?\d+$/.test(text) ? JSON.parse(text) : text;
}

/**
 * Returns the steps that the text of a path denotes.
 * @param {string} text text
 * @returns {Array} path
 */
function parsePath(text) {
  // a step escapes the dots of its own, so every dot that is left separates two of them
  return text ? text.split(".").map(parseStep) : [];
}

/**
 * Adopts the path that was rendered: a path that does not resolve any more is given up by the
 * server, and the client must not descend from a level that is not shown.
 */
function syncPath() {
  const form = document.querySelector("#entries-panel [data-path]");
  _path = parsePath(form?.dataset.path);
  _selected = form?.dataset.selected ? parseStep(form.dataset.selected) : null;
}

/** The sort and page links of the list panels are followed in place. */
followPanelLinks({ "stores-panel": refreshStores, "entries-panel": refreshEntries });

/** The endpoint of the view serves its three panels. */
_handlers[STORES_WS] = json => {
  switch(json.type) {
    case "stores": fillPanel("stores-panel", json.html); break;
    case "entries": showEntries(json.html); break;
    case "value": showValue(json); break;
  }
};

/**
 * Prepares the view. The panels are rendered by the server, which knows the selection from the
 * address; only what is selected later is requested over the connection.
 * @param {boolean} editable whether the shown value can be replaced
 */
function initStores(editable) {
  // the value is held by an editor that outlives its panel; the dialog brings a second one
  loadCodeMirror("xquery", true, "fill");
  loadCodeMirror("xquery", [ "add-value" ]);
  adoptSelection();
  // the server opens a store on its first entry: what was rendered is what is shown
  syncPath();
  setDisabled("save-value", !editable);
  editorReadOnly(!editable);

  window.addEventListener("popstate", popSelection);
}
