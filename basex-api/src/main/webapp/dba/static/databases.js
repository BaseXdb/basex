/** Databases: the database chooser, the resource browser and the document editor. */

/** Path of the endpoint that serves the panels and the queries of this view. */
const DB_WS = "/databases";

/** Selected database and resource. Both are part of the address: a link reproduces what the
    panels show, and the browser history steps through the selections that were made. */
let _db = "";
let _resource = "";

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
  let url = replaceParam(window.location.href, "name", _db);
  url = replaceParam(url, "resource", _resource);
  window.history.pushState({}, "", url);
}

/**
 * Adopts the selection of the address bar, after a step in the browser history.
 */
function popSelection() {
  const params = new URLSearchParams(window.location.search);
  _db = params.get("name") ?? "";
  _resource = params.get("resource") ?? "";
  showDatabase();
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
  requestPanel(DB_WS, "database-panel", { type: "database", name: _db, resource: _resource },
    sort, page);
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
  foldPanels();
  // the 'Indent' preference belongs to the editor, and outlives the panel that shows it
  const indent = document.getElementById("indent");
  if(indent) indent.checked = indentOn();
  initDocument(json.editable, json.text);
}

/**
 * Assigns the collapsed state of the panels, which follows from what the view shows and is
 * therefore not remembered: while a document is open, what it was chosen from steps back to a
 * strip; without one, the lists are what there is to see.
 */
function foldPanels() {
  // the panel decides, not the selection: a resource that does not exist opens nothing
  const shown = !document.querySelector("[data-label=Resource]").classList.contains("hidden");
  for(const [ label, collapse ] of [
    [ "Databases", shown ], [ "Database", false ], [ "Resource", false ],
    [ "Backups", shown ], [ "Information", true ]
  ]) {
    showPanel(document.querySelector(`.content > .panel[data-label='${label}']`), collapse);
  }
  applyColumns();
  // the panels that stay open have grown: what was clipped at the old widths is measured again
  window.dispatchEvent(new Event("resize"));
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
    editResource(editable);
  }
}

/**
 * Shows the document, raw or indented, or the result of a query on it.
 * @param {boolean} enforce enforce execution
 * @param {boolean} keep keep the shown message: it was rendered with the page and reports
 *   the action that led here, which the first rendering of the document must not discard
 */
function queryResource(enforce, keep) {
  const input = document.getElementById("input")?.value.trim() ?? "";
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
  editResource(false);
  if(input && _editable) {
    showNote("Read-only: query result. Clear the query to edit the document again.");
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
 * Stops the query that is currently evaluated on the resource.
 */
async function stopQuery() {
  // drop the number of the run: the result of the stopped query will be ignored
  endRequest();
  await sendMessage(DB_WS, { type: "stop" });
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
  editResource(_editable);
  showNote(_editable && indentOn() ?
    "Whitespace may be stripped when the document is saved." : undefined);
}

/**
 * Shows a note below the resource toolbar.
 * @param {string} message message; if omitted, the server-rendered reason is restored
 */
function showNote(message) {
  const note = document.getElementById("note");
  if(note) [ note.textContent, note.className ] =
    message ? [ message, "note warn" ] : _note;
}

/**
 * Enables or disables editing of the shown document.
 * @param {boolean} enabled edit state
 */
function editResource(enabled) {
  editorReadOnly(!enabled);
  setDisabled("save-resource", !enabled);
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
  let url = `db-save?name=${encodeURIComponent(_db)}&resource=${encodeURIComponent(_resource)}`;
  if(indent) url += "&indent=true";
  try {
    await request(url, content);
    // the raw document has changed: request it again
    _saved = indent ? undefined : content;
    setText("Resource was saved.", "info");
    refreshDatabase();
  } catch(response) {
    showError(response);
  }
}

/**
 * Asks for a new name for the selected database and renames it.
 * @returns {Promise} promise
 */
function renameDatabase() {
  return promptDatabase("rename", "New name of the database:");
}

/**
 * Asks for a name for the copy of the selected database and creates it.
 * @returns {Promise} promise
 */
function copyDatabase() {
  return promptDatabase("copy", "Name of the copy:");
}

/**
 * Asks for a database name and submits it to the requested action.
 * @param {string} action name of the action
 * @param {string} label text of the question
 * @returns {Promise} promise
 */
async function promptDatabase(action, label) {
  const name = await promptDialog(label, _db);
  if(!name) return;
  document.getElementById("database-form").action = `databases/${action}`;
  submitPrompt("database-newname", name);
}

/**
 * Asks for a new path for the selected resource and renames it.
 * @returns {Promise} promise
 */
async function renameResource() {
  const target = await promptDialog("New path of the resource:", _resource);
  if(target) submitPrompt("rename-target", target);
}

/**
 * Fills a hidden field with the value that was asked for and submits the form it belongs to.
 * @param {string} id id of the field
 * @param {string} value value
 */
function submitPrompt(id, value) {
  const input = document.getElementById(id);
  input.value = value;
  input.form.submit();
}

/**
 * Opens the file chooser that replaces the selected resource.
 */
function replaceResource() {
  document.getElementById("replace-file").click();
}

/**
 * Opens the file chooser that uploads backups; choosing files submits them.
 */
function chooseBackups() {
  document.getElementById("upload-backups").click();
}

/** Ctrl-Enter and the 'Indent' preference re-render what the editor shows. */
_editor_run = () => queryResource(true);
_indent_changed = () => queryResource(true);

/** The sort and page links of the list panels are followed in place. */
followPanelLinks({ "databases-panel": refreshDatabases, "database-panel": refreshDatabase });

/** The endpoint of the view serves the three panels and the queries on a resource. */
_handlers[DB_WS] = json => {
  switch(json.type) {
    case "databases": fillPanel("databases-panel", json.html); break;
    case "database": fillPanel("database-panel", json.html); break;
    case "backups": fillPanel("backups-panel", json.html); break;
    case "information": fillPanel("information-panel", json.html); break;
    case "resource": showResource(json); break;
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

  const params = new URLSearchParams(window.location.search);
  _db = params.get("name") ?? "";
  _resource = params.get("resource") ?? "";
  initDocument(editable, undefined);

  window.addEventListener("popstate", popSelection);
}
