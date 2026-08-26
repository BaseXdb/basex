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
 * Shows another resource of the selected database; an empty name closes the document.
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
  const filter = document.getElementById("resource-filter");
  if(filter) filter.value = "";
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
  requestPanel(DB_WS, "backups-panel", { type: "backups", name: _db });
  requestPanel(DB_WS, "information-panel", { type: "information", name: _db });
  refreshIndex();
}

/**
 * Requests the panel that browses an index of the selected database.
 * @param {string} sort sort key; if omitted, the shown order is kept
 * @param {number} page page; if omitted, the first one
 */
function refreshIndex(sort, page) {
  requestPanel(DB_WS, "index-panel", { type: "index", name: _db,
    index: fieldValue("index-select", "element-name"),
    prefix: fieldValue("index-prefix") }, sort, page);
}

/**
 * Requests the index entries that start with the supplied prefix. Every key is a new request,
 * so the ones that are typed in a row are collected first; Enter asks at once.
 * @param {string} key typed key
 */
function filterIndex(key) {
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
    { type: "database", name: _db, resource: _resource, dir: _dir, filter: dbFilter() },
    sort, page);
}

/**
 * Returns what the resource list is filtered by.
 * @returns {string} filter
 */
function dbFilter() {
  return fieldValue("resource-filter");
}

/**
 * Requests the resources that match the filter. Every key is a new request, so the ones that
 * are typed in a row are collected first; Enter asks at once.
 * @param {string} key typed key
 */
function filterResources(key) {
  filterKey(key, "resource-filter", refreshDatabase);
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
 * Assigns the collapsed state of the panels, which follows from what the view shows and is
 * therefore not remembered: while a document is open, what it was chosen from steps back to a
 * strip; without one, the lists are what there is to see.
 */
function foldResourcePanels() {
  // the panel decides, not the selection: a resource that does not exist opens nothing
  const shown = panelShown("Resource");
  foldPanels([
    [ "Databases", shown ], [ "Database", false ], [ "Resource", false ],
    [ "Backups", shown ], [ "Information", true ]
  ]);
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

  if(document.getElementById("input") && indentOn()) {
    // XML resource with indentation enabled: request the indented document
    queryResource(true, true);
  } else {
    setEditable("save-resource", editable);
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
 * Copies the shown document to the clipboard.
 */
function copyResource() {
  copy(editorValue());
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
  }
}

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
_indent_changed = () => queryResource(true);

/** The sort and page links of the list panels are followed in place. */
followPanelLinks({ "databases-panel": refreshDatabases, "database-panel": refreshDatabase,
  "index-panel": refreshIndex });

/** The controls of the list panels keep the focus and the caret while their panel is replaced. */
_panel_focus["database-panel"] = [ "#resource-filter" ];
_panel_focus["index-panel"] = [ "#index-select", "#index-prefix" ];

/** The panels of the view are filled by showMessage; what is left is the shown document. */
_handlers[DB_WS] = json => {
  switch(json.type) {
    case "editor": showResource(json); break;
    case "result": showResourceResult(json.result); break;
    case "stopped": setText("Query was stopped.", "warning"); break;
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
  initDocument(editable, undefined);
}
