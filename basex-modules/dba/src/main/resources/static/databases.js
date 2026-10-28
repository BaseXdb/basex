/** Databases: the database chooser, the resource browser and the document editor. */

/** Path of the endpoint that serves the panels and the queries of this view. */
const DB_WS = "/databases";

/** Selected database and resource, and the directory of the database that is listed. All three
    are part of the address: a link reproduces what the panels show, and the browser history
    steps through the selections that were made. */
let _db = "";
let _resource = "";
let _dir = "";

/** Whether the shown document can be edited. */
let _editable = false;

/** Server-rendered read-only reason ([ message, class ]). */
let _note;

/** Cached raw document; undefined if it must be requested again. */
let _saved;
/** Text the editor showed the document with; Save is enabled once it differs. */
let _shown;

/** Query and indent preference of the pending document request. */
let _request;

/**
 * Shows another database. Its resources replace the ones that were listed before, and the
 * document of the previous database is closed with it.
 * @param {string} name database
 */
function selectDatabase(name) {
  if(name === _db) return;
  _db = name;
  _resource = "";
  _dir = "";
  pushSelection();
  showDatabase();
}

/**
 * Selects a database and shows its backups.
 * @param {string} name database
 */
function showBackups(name) {
  selectDatabase(name);
  openDialog("properties", "backups-panel");
}

/**
 * Shows another resource of the selected database.
 * @param {string} resource resource
 */
function selectResource(resource) {
  if(resource === _resource) return;
  _resource = resource;
  pushSelection();
  mark("database-panel", _resource);
  refreshResource();
}

/**
 * Writes the selection to the address bar, as a step of its own: the back button returns to
 * what was shown before.
 */
function pushSelection() {
  pushParams({ name: _db, resource: _resource, dir: _dir });
}

/**
 * Shows another directory of the selected database. The document that is open is left alone:
 * it belongs to the database, not to the level it was chosen from.
 * @param {string} dir directory; '..' steps up to the parent directory
 */
function enterDbDir(dir) {
  // a level and a filter are two ways of looking at the database: entering one gives up the other
  clearField("resource-filter", _db);
  _dir = dir === ".." ? _dir.replace(/[^/]+\/$/, "") : dir;
  pushSelection();
  refreshDatabase();
}

/**
 * Adopts the selection of the address bar. A link that names a resource alone opens the level
 * that holds it, as the server does when it renders the page.
 */
function adoptSelection() {
  const params = new URLSearchParams(window.location.search);
  _db = params.get("name") ?? "";
  _resource = params.get("resource") ?? "";
  _dir = params.get("dir") ?? _resource.replace(/[^/]+$/, "");
}

/**
 * Requests everything that the selected database supplies. The list it was chosen from is not
 * among it: the entry it points out is the only thing that changes there.
 */
function showDatabase() {
  mark("databases-panel", _db);
  refreshDatabase();
  refreshResource();
  // the properties of another database are dropped; they are requested once looked at
  const properties = document.getElementById("properties-dialog");
  if(properties.querySelector("input[name=name]")?.value !== _db) properties.replaceChildren();
}

/** Dialog that is opened once its contents arrive, and the block it shows. */
let _opening;

/**
 * Opens a dialog whose contents are rendered by the server, requesting them if it is empty.
 * @param {string} dialog name of the dialog ('properties', 'storage')
 * @param {string} id id of the block that is shown; if omitted, the remembered one
 */
function openDialog(dialog, id) {
  if(document.getElementById(`${dialog}-dialog`).childElementCount) {
    showTab(dialog, id);
    showDialog(dialog);
  } else if(_opening?.dialog !== dialog) {
    _opening = { dialog, id };
    // the properties list the index that was browsed in the database again
    requestPanel(DB_WS, `${dialog}-dialog`, dialog === "properties" ?
      { type: dialog, name: _db, ...indexState() } : { type: dialog });
  }
}
for(const dialog of [ "properties", "storage" ]) {
  _panel_filled[`${dialog}-dialog`] = () => {
    if(_opening?.dialog !== dialog) return;
    const { id } = _opening;
    _opening = undefined;
    // nothing is opened if there is nothing to show
    if(document.getElementById(`${dialog}-dialog`).childElementCount) openDialog(dialog, id);
  };
}

/**
 * Shows a tab of a dialog, which is remembered.
 * @param {string} dialog name of the dialog
 * @param {string} id id of the block that the tab shows; if omitted, the remembered one
 */
function showTab(dialog, id) {
  const key = `dba-${dialog}-tab`;
  const tabs = [ ...document.querySelectorAll(`#${dialog}-dialog .tab`) ];
  const block = t => t.getAttribute("aria-controls");
  const tab = tabs.find(t => block(t) === (id ?? stored(key))) ?? tabs[0];
  for(const t of tabs) {
    t.classList.toggle("active", t === tab);
    t.setAttribute("aria-selected", t === tab);
    t.tabIndex = t === tab ? 0 : -1;
    document.getElementById(block(t)).hidden = t !== tab;
  }
  store(key, block(tab));
}

/**
 * Switches the tabs of a dialog with the cursor keys, as a tab list does.
 * @param {string} dialog name of the dialog
 * @param {KeyboardEvent} event key event
 */
function tabKey(dialog, event) {
  const tabs = [ ...event.currentTarget.children ];
  const p = tabs.indexOf(event.target);
  const next = { ArrowLeft: p - 1, ArrowRight: p + 1, Home: 0, End: tabs.length - 1 }[event.key];
  if(p === -1 || next === undefined) return;
  event.preventDefault();
  const tab = tabs.at(next % tabs.length);
  showTab(dialog, tab.getAttribute("aria-controls"));
  tab.focus();
}

/**
 * Requests the backups block of the properties dialog: the backups of the selected database.
 * @param {string} sort sort key; if omitted, the shown order is kept
 * @param {number} page page; if omitted, the first one
 */
function refreshBackups(sort, page) {
  requestPanel(DB_WS, "backups-panel", { type: "backups", name: _db }, sort, page);
}

/**
 * Requests the list of the general backups dialog.
 * @param {string} sort sort key; if omitted, the shown order is kept
 * @param {number} page page; if omitted, the first one
 */
function refreshGeneralBackups(sort, page) {
  requestPanel(DB_WS, "general-backups-panel", { type: "general-backups" }, sort, page);
}

/**
 * Requests the panel that browses an index of the selected database.
 * @param {string} sort sort key; if omitted, the shown order is kept
 * @param {number} page page; if omitted, the first one
 */
function refreshIndex(sort, page) {
  requestPanel(DB_WS, "index-panel", { type: "index", name: _db, ...indexState() }, sort, page);
}

/**
 * Returns the index that is browsed in the selected database, and the prefix of its entries.
 * @returns {object} index and prefix
 */
function indexState() {
  // the index that was chosen for the database, as the prefix, outlives the page
  return {
    index: storedField("index-select", _db) || fieldValue("index-select") || "element-name",
    prefix: storedField("index-prefix", _db)
  };
}

/**
 * Shows another index of the selected database, which is remembered for it.
 */
function selectIndex() {
  storeField("index-select", _db);
  clearPrefix();
  refreshIndex();
}

/**
 * Empties the prefix of the index list: it belongs to the index it was typed for.
 */
function clearPrefix() {
  clearField("index-prefix", _db);
}

/**
 * Requests the index entries that start with the supplied prefix.
 */
function filterIndex() {
  storeField("index-prefix", _db);
  refreshIndex();
}

/**
 * Requests the databases panel.
 * @param {string} sort sort key; if omitted, the shown order is kept
 * @param {number} page page; if omitted, the first one
 */
function refreshDatabases(sort, page) {
  requestPanel(DB_WS, "databases-panel", { type: "databases", name: _db }, sort, page);
}

/**
 * Requests the panel of the selected database.
 * @param {string} sort sort key; if omitted, the shown order is kept
 * @param {number} page page; if omitted, the first one
 */
function refreshDatabase(sort, page) {
  requestPanel(DB_WS, "database-panel",
    { type: "database", name: _db, resource: _resource, dir: _dir,
      filter: storedField("resource-filter", _db) }, sort, page);
}

/**
 * Requests the resources that match the filter.
 */
function filterResources() {
  storeField("resource-filter", _db);
  // the filtered list keeps the number of entries that are shown, and starts at the top
  _toTop.add("database-panel");
  refreshDatabase(undefined, shownPages("database-panel"));
}

/**
 * Requests the panel of the selected resource, and with it the document itself. It is asked
 * for even while the panel is folded away: the editor holds the document, not the panel.
 */
function refreshResource() {
  sendMessage(DB_WS, { type: "resource", name: _db, resource: _resource });
}

/**
 * Shows the resource panel and the document it refers to.
 * @param {object} json panel contents, document text and edit state
 */
function showResource(json) {
  fillPanel("resource-panel", json.html);
  // the 'Indent' preference belongs to the editor, and outlives the panel that shows it
  restoreIndent();
  initDocument(json.editable, json.text);
}

/**
 * Adopts the document that is shown in the editor.
 * @param {boolean} editable whether the document can be edited in place
 * @param {string} text document; if omitted, the editor already holds it
 */
function initDocument(editable, text) {
  _editable = editable;
  _request = undefined;
  if(text !== undefined) _editor.setValue(text);
  // the editor normalizes line endings: what it holds is the baseline, not what was sent
  _saved = editorValue();
  const note = document.getElementById("note");
  _note = note ? [ note.textContent, note.className ] : undefined;

  // the query field is rendered empty: what was typed for the database is run again
  if(restoreField("input", _db) || document.getElementById("input") && indentOn()) {
    // XML resource with a query or indentation: request the result
    queryResource(true, true);
  } else {
    setEditable("save-resource", editable);
    markShown();
  }
}

/**
 * Shows the document, raw or indented, or the result of a query on it.
 * @param {boolean} enforce enforce execution
 * @param {boolean} keep keep the shown message: it was rendered with the page and reports
 *   the action that led here, which the first rendering of the document must not discard
 */
function queryResource(enforce, keep) {
  const input = fieldValue("input");
  storeField("input", _db);
  const indent = indentOn();
  // re-run whenever the query or the indent preference changes
  if(!enforce && _request?.input === input && _request?.indent === indent) return;
  // remember what was requested: the reply is evaluated when it arrives
  _request = { input, indent };
  // what the last rendering reported is outdated as soon as a new one is asked for
  if(!keep) setText("", "");

  // no query: show the document, raw or indented. only the raw one is cached
  if(!input && !indent && _saved !== undefined) {
    showDocument(_saved);
    return;
  }
  // block edits until the result has been received; a query result is read-only
  setEditable("save-resource", false);
  if(input && _editable) {
    showResourceNote("Read-only: query result; clear the query to edit.");
  }

  sendRequest(DB_WS, {
    type: "query",
    name: _db,
    resource: _resource,
    query: input || ".",
    indent
  });
}

/**
 * Shows the document or query result that was pushed by the server.
 * @param {string} text result
 */
function showResourceResult(text) {
  if(_request.input) {
    _editor.setValue(text);
    setText("Query was successful.", "info");
  } else {
    showDocument(text);
    if(!_request.indent) _saved = text;
  }
}

/**
 * Shows a document in the editor.
 * @param {string} text document
 */
function showDocument(text) {
  _editor.setValue(text);
  setEditable("save-resource", _editable);
  markShown();
  showResourceNote(_editable && indentOn() ?
    "Whitespace may be stripped when the document is saved." : undefined);
}

/**
 * Shows a note below the resource toolbar.
 * @param {string} message message; if omitted, the server-rendered reason is restored
 */
function showResourceNote(message) {
  showNote("note", message, _note);
}

/**
 * Saves the edited document.
 * @returns {Promise} promise
 */
async function saveResource() {
  const content = editorValue();
  const indent = indentOn();
  const params = { name: _db, resource: _resource };
  if(indent) params.indent = true;
  if(await saveEditor("db-save", params, "Resource was saved.", refreshDatabase)) {
    // the raw document has changed: request it again
    _saved = indent ? undefined : content;
    markShown();
  }
}

/**
 * Takes the text of the editor as the one the document was shown with, which is nothing to save.
 */
function markShown() {
  _shown = editorValue();
  setDisabled("save-resource", true);
}

/** A document can be saved once it was edited; a query result cannot be saved at all. */
_editor_changed = () => {
  setDisabled("save-resource", !_editable || Boolean(_request?.input) || editorValue() === _shown);
};

/**
 * Asks for a new name for the selected database and renames it.
 * @returns {Promise} promise
 */
function renameDatabase() {
  return promptSubmit("database-newname", "New name of the database:", "Rename", _db,
    "databases/rename");
}

/**
 * Asks for a name for the copy of the selected database and creates it.
 * @returns {Promise} promise
 */
function copyDatabase() {
  return promptSubmit("database-newname", "Name of the copy:", "Copy", _db, "databases/copy");
}

/**
 * Asks for a new path for the selected resource and renames it.
 * @returns {Promise} promise
 */
function renameResource() {
  return promptSubmit("rename-target", "New path of the resource:", "Rename", _resource);
}

/**
 * Derives the target path of the Add dialog from the input that was entered: an input is
 * stored under its own name, as it is in the GUI.
 * @param {HTMLInputElement} input input field
 */
function deriveTarget(input) {
  const segments = input.value.split(/[/\\]+/).filter(segment => segment);
  // the input is stored where the panel is: the level that is listed is the target
  document.getElementById("add-target").value = _dir + (segments.pop() || "");
}

/** The queries of the view run on the endpoint that also serves its panels. */
_query_path = DB_WS;

/** Ctrl-Enter and the 'Indent' preference re-render what the editor shows. */
_editor_run = () => queryResource(true);
_editor_shortcuts.push([ "Ctrl+Enter", "Run the query of the resource" ]);
_indent_changed = () => queryResource(true);

/** A database that is dropped takes the fields that were remembered for it along: one that is
    created again under its name starts afresh. */
document.addEventListener("submit", event => {
  if(!event.submitter?.formAction.endsWith("/databases/drop")) return;
  forgetFields([ ...event.target.querySelectorAll("input[name=name]:checked") ].
    map(input => input.value));
});

/** The sort and page links of the list panels are followed in place. */
followPanelLinks({ "databases-panel": refreshDatabases, "database-panel": refreshDatabase,
  "backups-panel": refreshBackups, "general-backups-panel": refreshGeneralBackups,
  "index-panel": refreshIndex });

/** The controls of the list panels keep the focus and the caret while their panel is replaced. */
_panel_focus["database-panel"] = [ "#resource-filter" ];
_panel_focus["index-panel"] = [ "#index-select", "#index-prefix" ];

/** The panels of the view are filled by showMessage; what is left is the shown document. */
_handlers[DB_WS] = json => {
  switch(json.type) {
    case "editor": showResource(json); break;
    case "result": showResourceResult(json.result); break;
  }
};

/**
 * Prepares the view. The panels are rendered by the server, which knows the selection from the
 * address; only what is selected later is requested over the connection.
 * @param {boolean} editable whether the shown document can be edited in place
 */
function initDatabases(editable) {
  // the panels are folded away, not dragged: one mechanism is enough to divide the page
  loadCodeMirror("xml", true, "fill");

  initSelection(adoptSelection, showDatabase);
  // the server opens the first database if the address names none: the address is completed
  _db ||= adoptOpened("database-panel");
  initDocument(editable);
  // the dialog of an action is opened again, at the named or remembered tab; a reload does not repeat it
  const params = new URLSearchParams(window.location.search);
  const show = params.get("show");
  if(show) {
    hideParams("show", "tab");
    openDialog(show, params.get("tab") ?? undefined);
  }
  // the lists are rendered unfiltered: what was typed for the database is asked for again
  if(restoreField("resource-filter", _db)) refreshDatabase();
  const index = storedField("index-select", _db);
  if(document.getElementById("index-select") &&
      (restoreField("index-prefix", _db) || index && index !== fieldValue("index-select"))) {
    refreshIndex();
  }
}
