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
 * @param {string} name database; the selected one, or an empty string, closes it
 */
function selectDatabase(name) {
  // without a database, the page shows the general backups again
  _db = name === _db ? "" : name;
  _resource = "";
  _dir = "";
  pushSelection();
  showDatabase();
}

/**
 * Shows another resource of the selected database; an empty name, or the open one, closes the
 * document.
 * @param {string} resource resource
 */
function selectResource(resource) {
  _resource = resource === _resource ? "" : resource;
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
  const filter = document.getElementById("resource-filter");
  if(filter) filter.value = "";
  storeField("resource-filter", _db);
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
  refreshBackups();
  requestPanel(DB_WS, "information-panel", { type: "information", name: _db });
  refreshIndex();
}

/**
 * Requests the backups panel: the backups of the selected database, or the general ones.
 * @param {string} sort sort key; if omitted, the shown order is kept
 * @param {number} page page; if omitted, the first one
 */
function refreshBackups(sort, page) {
  requestPanel(DB_WS, "backups-panel", { type: "backups", name: _db }, sort, page);
}

/**
 * Requests the panel that browses an index of the selected database.
 * @param {string} sort sort key; if omitted, the shown order is kept
 * @param {number} page page; if omitted, the first one
 */
function refreshIndex(sort, page) {
  // the index that was chosen for the database, as the prefix, outlives the page
  requestPanel(DB_WS, "index-panel", { type: "index", name: _db,
    index: storedField("index-select", _db) || fieldValue("index-select") || "element-name",
    prefix: storedField("index-prefix", _db) }, sort, page);
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
  const prefix = document.getElementById("index-prefix");
  if(prefix) prefix.value = "";
  storeField("index-prefix", _db);
}

/**
 * Requests the index entries that start with the supplied prefix.
 * @param {string} key typed key
 */
function filterIndex(key) {
  storeField("index-prefix", _db);
  filterKey(key, "index-prefix", refreshIndex);
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
 * @param {string} key typed key
 */
function filterResources(key) {
  storeField("resource-filter", _db);
  // the filtered list keeps the number of entries that are shown, and starts at the top
  filterKey(key, "resource-filter", () => {
    _toTop.add("database-panel");
    refreshDatabase(undefined, shownPages("database-panel"));
  });
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
  // every selection asks for the resource panel, so this is where the level is known
  foldResourcePanels();
  // the 'Indent' preference belongs to the editor, and outlives the panel that shows it
  restoreIndent();
  initDocument(json.editable, json.text);
}

/**
 * Assigns the collapsed state of the panels for the level that the view shows.
 */
function foldResourcePanels() {
  // the panel decides, not the selection: a resource that does not exist opens nothing
  const shown = panelShown("Resource");
  // every level keeps the panels that were folded by hand on it; see panelsKey
  const content = document.querySelector(".content");
  if(shown) content.dataset.panels = "resource";
  else if(_db) content.dataset.panels = "database";
  else delete content.dataset.panels;
  const folded = storedPanels();
  // a document folds what it was chosen from; the backups are shown on the top level only
  foldPanels([
    [ "Databases", shown ], [ "Database", false ], [ "Resource", false ],
    [ "Backups", shown || Boolean(_db) ], [ "Information", true ]
  ].map(([ label, collapse ]) =>
    [ label, folded[label] ?? collapse ]));
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
  _note = note ? [ note.textContent, note.className ] : [ "", "note" ];

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
  _request = { input: input, indent: indent };
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
    showResourceNote("Read-only: query result. Clear the query to edit the document again.");
  }

  const run = startRequest();
  sendMessage(DB_WS, {
    type: "query",
    run: run,
    name: _db,
    resource: _resource,
    query: input || ".",
    indent: indent
  });
  awaitResult(run);
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
  return promptSubmit("database-newname", "New name of the database:", _db, "databases/rename");
}

/**
 * Asks for a name for the copy of the selected database and creates it.
 * @returns {Promise} promise
 */
function copyDatabase() {
  return promptSubmit("database-newname", "Name of the copy:", _db, "databases/copy");
}

/**
 * Asks for a new path for the selected resource and renames it.
 * @returns {Promise} promise
 */
function renameResource() {
  return promptSubmit("rename-target", "New path of the resource:", _resource);
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

/** The sort and page links of the list panels are followed in place. */
followPanelLinks({ "databases-panel": refreshDatabases, "database-panel": refreshDatabase,
  "backups-panel": refreshBackups, "index-panel": refreshIndex });

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
  initDocument(editable);
  // the lists are rendered unfiltered: what was typed for the database is asked for again
  if(restoreField("resource-filter", _db)) refreshDatabase();
  const index = storedField("index-select", _db);
  if(restoreField("index-prefix", _db) || index && index !== fieldValue("index-select")) {
    refreshIndex();
  }
}
