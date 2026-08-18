/** Files: file handling, queries and their results. */


/** Id of the job of the query that is currently evaluated in the editor panel. */
let _job;

/** File names that are treated as XQuery, matching IO.XQSUFFIXES. */
const XQUERY_SUFFIXES = /\.(xq|xqm|xqy|xql|xqu|xquery|xpath)$/i;

/** localStorage key prefix for unsaved editor drafts; see draftKey. */
const DRAFT = "dba-draft:";

/** localStorage key for the directory of the file panel. */
const DIR_KEY = "dba-dir";

/** localStorage keys for the open documents and the one that is shown. */
const TABS_KEY = "dba-tabs";
const TAB_KEY = "dba-tab";

/** Documents that are open in the editor, in tab order. A tab is { dir, name, id, saved,
    edited, state }: its identity, the number that tells unnamed documents apart, its on-disk
    content, whether the user typed in it, and the editor state that restores it (undefined
    until the file has been read; a plain string if CodeMirror is unavailable). */
let _tabs = [];

/** Number of the next document. */
let _nextId = 1;

/** Index of the active tab. */
let _tab = 0;

/** What the tab strip currently shows, so that typing does not rebuild it. */
let _strip;

/** Whether the editor is currently written to by the code, not by the user. */
let _writing = false;

/** Document whose content is awaited; a response for another one is outdated. */
let _opening;

/**
 * Returns the active document.
 * @returns {object} tab
 */
function tab() {
  return _tabs[_tab];
}

/**
 * Creates a document.
 * @param {string} dir directory
 * @param {string} name file name; empty for an unnamed document
 * @returns {object} tab
 */
function newTab(dir, name) {
  return { dir: dir, name: name, id: _nextId++, saved: "", edited: false, state: undefined };
}

/**
 * Opens an empty document.
 */
function newFile() {
  captureTab();
  _tabs.push(newTab(filesDir(), ""));
  _tab = _tabs.length - 1;
  applyTab();
  storeTabs();
  setText("", "");
}

/**
 * Returns the text of a document: the editor holds the active one, the others their state.
 * @param {object} t tab
 * @returns {string} text
 */
function tabText(t) {
  if(t === tab()) return editorValue();
  return typeof t.state === "string" ? t.state : t.state ? t.state.doc.toString() : "";
}

/**
 * Indicates whether a document holds unsaved work.
 * @param {object} t tab
 * @returns {boolean} modified state
 */
function tabModified(t) {
  return t.edited && tabText(t) !== t.saved;
}

/**
 * Persists the open documents. Only their identity is stored: the contents are read again,
 * and unsaved work is already kept as a draft.
 */
function storeTabs() {
  try {
    localStorage.setItem(TABS_KEY,
      JSON.stringify(_tabs.map(t => ({ dir: t.dir, name: t.name, id: t.id }))));
    localStorage.setItem(TAB_KEY, _tab);
  } catch { /* storage disabled or full: the tabs are restored best-effort */ }
}

/**
 * Returns the label of a document: its file name, or a name that one could be saved under.
 * The first unnamed document is 'file', the ones after it are numbered.
 * @param {object} t tab
 * @returns {string} label
 */
function tabLabel(t) {
  if(t.name) return t.name;
  const n = _tabs.filter(u => !u.name).indexOf(t);
  return n > 0 ? `file${n + 1}` : "file";
}

/**
 * Draws the tab strip.
 */
function renderTabs() {
  const strip = document.getElementById("tabs");
  if(!strip) return;
  // this runs on every keystroke: rebuild only when something it shows has changed
  const signature = JSON.stringify(_tabs.map((t, i) =>
    [ t.dir, t.name, i === _tab, tabModified(t) ]));
  if(signature === _strip) return;
  _strip = signature;

  strip.replaceChildren(..._tabs.map((t, i) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "tab";
    const edited = tabModified(t);
    button.classList.toggle("active", i === _tab);
    button.title = t.name ? t.dir + t.name : "New file";
    // file names are user input: they are text, never markup
    const label = document.createElement("span");
    label.textContent = tabLabel(t);
    const close = document.createElement("span");
    close.className = "close";
    close.textContent = "\u00d7";
    close.addEventListener("click", event => {
      event.stopPropagation();
      closeTab(i);
    });
    button.append(label);
    // unsaved work is marked after the name, in a span of its own: a name that is too long
    // is clipped, and would take the marker with it
    if(edited) {
      const mark = document.createElement("span");
      mark.textContent = "*";
      button.append(mark);
    }
    button.append(close);
    button.addEventListener("click", () => selectTab(i));
    // a middle click closes the tab, as it does in the browser itself
    button.addEventListener("auxclick", event => {
      if(event.button === 1) closeTab(i);
    });
    return button;
  }));
}

/**
 * Keeps what the editor shows in the active document, so that switching back restores it,
 * with its undo history.
 */
function captureTab() {
  const t = tab();
  if(t) t.state = _editor.view ? _editor.view.state : editorValue();
}

/**
 * Shows the state of the active document in the editor.
 */
function applyTab() {
  const t = tab();
  // an unnamed document that was never shown holds its unsaved draft, if it kept one; a file
  // that was never shown is read from disk, and its draft is applied there
  const draft = t.state === undefined && !t.name ? localStorage.getItem(draftKey(t)) : null;
  _writing = true;
  try {
    if(_editor.view && t.state && typeof t.state !== "string") {
      _editor.view.setState(t.state);
    } else {
      _editor.setValue(typeof t.state === "string" ? t.state : draft ?? "");
      _editor.clearHistory();
    }
  } finally {
    _writing = false;
  }
  if(draft) t.edited = true;
  showTab();
}

/**
 * Refreshes what the strip shows of the active document, and returns the focus to the editor.
 */
function showTab() {
  renderTabs();
  checkButtons();
  if(_editor.setLanguage) _editor.setLanguage(fileLanguage(tab()?.name));
  _editor.focus();
}

/**
 * Shows another document, reading its content if this is the first time it is shown.
 * @param {number} index tab index
 */
function selectTab(index) {
  if(index === _tab || !_tabs[index]) return;
  captureTab();
  _tab = index;
  const t = tab();
  // show the document before its content arrives: while the editor still held the previous one,
  // an edit would be saved as a draft of this one
  applyTab();
  if(t.state === undefined && t.name) loadTab(t);
  storeTabs();
}

/**
 * Closes a document, asking what to do with unsaved work: whether to keep it, throw it away,
 * or leave the document open after all.
 * @param {number} index tab index
 * @returns {Promise} promise
 */
async function closeTab(index) {
  const t = _tabs[index];
  if(!t) return;
  if(tabModified(t)) {
    // ask about the document the user is looking at
    if(index !== _tab) selectTab(index);
    const answer = await askQuestion(`Save changes to ${tabLabel(t)}?`,
      [ [ "yes", "Yes" ], [ "no", "No" ], [ "", "Cancel" ] ]);
    // the question was about closing as much as about saving: cancelling closes nothing
    if(!answer) return;
    if(answer === "yes") {
      // a failed save must not take the document with it
      if(!await saveFile()) return;
    } else {
      localStorage.removeItem(draftKey(t));
    }
    index = _tabs.indexOf(t);
  }
  const active = index === _tab;
  if(active) captureTab();
  _tabs.splice(index, 1);
  // there is always a document: the last one that closes leaves an unnamed one
  if(!_tabs.length) _tabs.push(newTab(filesDir(), ""));
  if(index < _tab) _tab--;
  if(_tab >= _tabs.length) _tab = _tabs.length - 1;
  if(active) applyTab();
  else renderTabs();
  storeTabs();
  setText("", "");
}

/**
 * Opens the job view of the running query in a browser tab of its own. Repeated clicks reuse
 * that tab; a click with Ctrl or Cmd opens another one.
 * @param {Event} event click event
 */
function openJob(event) {
  const target = event?.ctrlKey || event?.metaKey ? "_blank" : "activity";
  if(_job) window.open(`activity?job=${encodeURIComponent(_job)}`, target);
}

/**
 * Remembers the job of the running query; the Job button jumps to its details.
 * @param {string} id job id; if omitted, no query is running
 */
function setJob(id) {
  _job = id;
  setDisabled("job", !id);
}

/**
 * Returns the directory of the file panel. It is remembered by the browser and outlives the
 * session; the server resolves what is sent and answers with the directory it resolved.
 * @returns {string} directory, empty before the first panel was rendered
 */
function filesDir() {
  return localStorage.getItem(DIR_KEY) ?? "";
}

/**
 * Shows another directory in the file panel.
 * @param {string} dir directory
 */
function changeDir(dir) {
  refreshFiles(undefined, dir);
}

/**
 * Enters a directory next to the shown one; the server resolves the step.
 * @param {string} name name of a subdirectory, or '..' for the parent
 */
function enterDir(name) {
  changeDir(`${filesDir()}/${name}`);
}

/**
 * Requests the file panel, keeping the sort order it shows. The answer is pushed back over the
 * connection that the view already opened for its queries; see showFiles.
 * @param {string} sort sort key; if omitted, the shown order is kept
 * @param {string} dir directory; if omitted, the shown directory is kept
 */
function refreshFiles(sort, dir) {
  if(!document.getElementById("files-panel")) return;
  sendMessage("", {
    type: "files",
    sort: sort ?? document.querySelector("#files-panel [data-sort]")?.dataset.sort ?? "name",
    dir: dir ?? filesDir()
  });
}

/**
 * Shows the file panel that was pushed by the server.
 * @param {string} html panel contents
 */
function showFiles(html) {
  const panel = document.getElementById("files-panel");
  panel.innerHTML = html;
  // the chooser shows the directory the server resolved: that is what is remembered
  localStorage.setItem(DIR_KEY, document.getElementById("dir").value);
  // the panel arrives after the shared setup ran, so its buttons are checked here
  buttons();
  markTruncated(panel);
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

  const run = startRequest();
  if(!await sendMessage("", {
    type: "run",
    run: run,
    query: editorValue(),
    indent: indentOn(),
    // relative paths in the query resolve against the opened file, or against the directory
    dir: tabDir(),
    file: tab() ? tab().name : ""
  })) return;
  awaitResult(run);
}

/**
 * Stops the query that is currently evaluated in the editor panel. The server confirms the
 * request with a 'stopped' message; see showMessage.
 */
async function stopQuery() {
  if(_editor) _editor.focus();

  // drop the number of the run: the result of the stopped query will be ignored
  endRequest();
  await sendMessage("", { type: "stop" });
  setJob();
}

/**
 * Guesses the result language from its first character.
 * @param {string} text serialized result
 * @returns {string} language for _output.setLanguage
 */
function resultLanguage(text) {
  const s = text.replace(/^\s+/, "");
  if(s[0] === "<") return "xml";
  if(s[0] === "{" || s[0] === "[") return "json";
  return "text";
}

/**
 * Shows the result of a query.
 * @param {string} text result
 */
function showResult(text) {
  setText("Query was successful.", "info");
  if(_output.setLanguage) _output.setLanguage(resultLanguage(text));
  _output.setValue(text);
}

/**
 * Indicates whether the editor content can be run: an unnamed document, or a file that is
 * named like an XQuery file.
 * @returns {boolean} runnable state
 */
function runnable() {
  const t = tab();
  return !t || !t.name || XQUERY_SUFFIXES.test(t.name);
}

/**
 * Chooses the editor language from the file name: XQuery (see runnable), else by
 * extension, else plain text.
 * @param {string} name file name (may be empty)
 * @returns {string} language for _editor.setLanguage
 */
function fileLanguage(name) {
  if(!name || XQUERY_SUFFIXES.test(name)) return "xquery";
  const ext = name.replace(/^.*\./, "").toLowerCase();
  if([ "xml", "xsd", "xsl", "xslt", "svg", "rng", "rdf", "wsdl", "xhtml" ].includes(ext)) return "xml";
  if(ext === "json") return "json";
  return "text";
}

/**
 * Opens a file in a tab of its own; a file that is already open is shown again.
 * @param {string} file file name
 * @param {string} dir directory of the file; if omitted, the one the file panel shows
 */
function openFile(file, dir) {
  const from = dir ?? filesDir();
  const open = _tabs.findIndex(t => t.name === file && t.dir === from);
  if(open > -1) {
    selectTab(open);
    return;
  }
  captureTab();
  const opened = newTab(from, file);
  // an unnamed document that was never used is replaced, rather than left behind
  if(tab() && !tab().name && !tab().edited && !editorValue()) {
    Object.assign(tab(), opened);
  } else {
    _tabs.push(opened);
    _tab = _tabs.length - 1;
  }
  // as in selectTab: the editor must not hold another document while this one is read
  applyTab();
  loadTab(tab());
}

/**
 * Reads the content of a document and shows it.
 * @param {object} t tab
 * @returns {Promise} promise
 */
async function loadTab(t) {
  const key = _opening = t.dir + t.name;
  try {
    const disk = await request(`editor-open?${new URLSearchParams({ name: t.name, dir: t.dir })}`);
    // drop the answer of a request that was superseded by a newer one
    if(_opening !== key || t !== tab()) return;
    // set the baseline before setValue, whose synchronous change event runs saveDraft
    t.saved = disk;
    setEditorValue(disk);
    _editor.clearHistory();
    // the editor normalizes line endings: take the baseline back from it, or every file that
    // is stored with CRLF would count as modified the moment it is opened
    t.saved = editorValue();
    t.edited = false;
    showTab();
    setText("", "");
    // apply a newer unsaved draft on top of the saved file (undo reverts to disk)
    const draft = localStorage.getItem(draftKey(t));
    if(draft !== null && draft !== t.saved) {
      setEditorValue(draft);
      // the document differs from the file: mark it
      t.edited = true;
      showTab();
    }
    storeTabs();
  } catch(response) {
    // the file is gone or unreadable: drop its tab, so it is not requested again. The tab is
    // unedited, so closing it asks nothing; its message is awaited, then replaced by the error
    await closeTab(_tabs.indexOf(t));
    showError(response, t.name);
  }
}

/**
 * Indicates whether the active document holds unsaved work. Content that the code wrote (a file
 * that was opened) is no reason to treat it as edited; a restored draft is.
 * @returns {boolean} edit state
 */
function modified() {
  const t = tab();
  return t ? tabModified(t) : false;
}

/**
 * Writes text to the editor without counting it as an edit of the user.
 * @param {string} text text to be shown
 */
function setEditorValue(text) {
  _writing = true;
  try {
    _editor.setValue(text);
  } finally {
    _writing = false;
  }
}

/**
 * Saves the active document. An unnamed one is named first; it is stored in the directory
 * the file panel shows, as it has none of its own.
 * @param {boolean} saveAs ask for a name, whether or not the document has one
 * @returns {Promise} promise, resolved with true if the document was saved
 */
async function saveFile(saveAs) {
  const t = tab();
  if(!t) return false;
  let name = t.name;
  if(!name || saveAs) {
    // the label of an unnamed document is a name it can be saved under
    name = await promptDialog("Name of the file:", tabLabel(t));
    if(!name) return false;
    // append file suffix
    if(!name.includes(".")) name += ".xq";
  }
  const dir = tabDir();
  const text = editorValue();
  // the document is renamed below: its draft is dropped under both keys
  const key = draftKey(t);
  try {
    await request(`editor-save?${new URLSearchParams({ name: name, dir: dir })}`, text);
    Object.assign(t, { dir: dir, name: name, saved: text, edited: false });
    localStorage.removeItem(key);
    localStorage.removeItem(draftKey(t));
    showTab();
    setText("File was saved.", "info");
    storeTabs();
    refreshFiles();
    return true;
  } catch(response) {
    showError(response, name);
    return false;
  }
}

/**
 * Returns the directory of the active document: its own, or the one the file panel shows if
 * the document has no name yet. Not to be confused with filesDir, which is the panel's.
 * @returns {string} directory
 */
function tabDir() {
  return tab() && tab().dir || filesDir();
}

/**
 * Returns the key under which the unsaved draft of a document is kept. A named document is
 * known by its path, so that reopening the file restores its draft, and files of the same name
 * in two directories keep two drafts; unnamed documents are told apart by their number.
 * @param {object} t tab
 * @returns {string} key
 */
function draftKey(t) {
  return DRAFT + (t.name ? t.dir + t.name : "#" + t.id);
}

/**
 * Persists the editor buffer as a local draft, or drops it once it matches the saved file.
 */
function saveDraft() {
  const t = tab();
  // drafts belong to the Files view; skip on the other CodeMirror pages
  if(!t) return;
  const content = editorValue();
  const key = draftKey(t);
  try {
    if(content === t.saved) localStorage.removeItem(key);
    else localStorage.setItem(key, content);
  } catch { /* storage disabled or full: drafts are best-effort */ }
}

/**
 * Opens the file chooser of the upload form; choosing files submits it.
 */
function chooseFiles() {
  document.getElementById("upload").click();
}

/**
 * Asks for a name and creates a directory.
 * @returns {Promise} promise
 */
async function createDir() {
  const name = await promptDialog("Name of the new directory:");
  if(!name) return;
  document.getElementById("dir-name").value = name;
  document.getElementById("dir-create").submit();
}

/**
 * Refreshes the editor buttons.
 */
function checkButtons() {
  setDisabled("run", !runnable());
  // an unchanged document has nothing to save; a copy of it can be saved at any time
  setDisabled("save", !modified());
  setDisabled("saveas", !editorValue());
}

/** The editor of the Files view runs queries, tracks edits and keeps drafts. */
_editor_run = runQuery;
_editor_changed = () => {
  // content the code wrote is not an edit, and must not be saved as a draft
  if(_writing) return;
  if(tab()) tab().edited = true;
  renderTabs();
  checkButtons();
  saveDraft();
};

/** The endpoint of the view reports the job of a query, its outcome, and the file panel. */
_handlers[""] = json => {
  if(json.type === "job") {
    setJob(json.id);
  } else if(json.type === "files") {
    showFiles(json.html);
  } else {
    // the query has ended: the job is gone, and there is nothing left to jump to
    setJob();
    if(json.type === "stopped") setText("Query was stopped.", "warning");
    else if(json.type === "result") showResult(json.result);
  }
};

/**
 * Prepares the view: the editors, the draggable splits, the file panel, and the documents that
 * were left open. A deep link names the directory and the file it refers to; both are adopted,
 * so that following it and reloading the page show the same.
 */
function initFiles() {
  loadCodeMirror("xquery", true, "fill");
  initResizers();

  const params = new URLSearchParams(window.location.search);
  const dir = params.get("dir"), name = params.get("name");
  if(dir) localStorage.setItem(DIR_KEY, dir);
  hideParams("dir", "name");

  refreshFiles();

  // the strip is restored as a whole; only the active document is read, the others when they
  // are selected. The file panel and the editor stay independent: a document is read from its
  // own directory, whichever one the panel is asked to show
  try {
    _tabs = JSON.parse(localStorage.getItem(TABS_KEY) ?? "[]").map(t =>
      Object.assign(newTab(t.dir, t.name), { id: t.id ?? 0 }));
  } catch {
    _tabs = [];
  }
  // numbers are handed out after the restored ones, so no draft of theirs is overwritten
  _nextId = Math.max(0, ..._tabs.map(t => t.id)) + 1;
  if(!_tabs.length) _tabs.push(newTab(filesDir(), ""));
  _tab = Math.min(Math.max(0, Number(localStorage.getItem(TAB_KEY)) || 0), _tabs.length - 1);
  renderTabs();

  if(name) openFile(name, dir ?? filesDir());
  else if(tab().name) loadTab(tab());
  else applyTab();
}
