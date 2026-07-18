/** Link to the resizer area. */
let _resizer;
/** Link to the left editor component. */
let _left;

/** localStorage key prefix for unsaved editor drafts (per file name). */
const DRAFT = "dba-draft:";
/** On-disk content of the current file (empty for an untitled buffer). */
let _saved = "";

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
  _left = document.getElementById("left");
  _resizer = document.querySelector(".resizer");

  _left.style.width = (localStorage.getItem("editorWidth") || 50) + "%";
  _resizer.addEventListener("pointerdown", e => {
    document.addEventListener("pointermove", resize);
    document.addEventListener("pointerup", stopResize);
    _resizer.setPointerCapture(e.pointerId);
  });
}

/**
 * Resizes the left panel.
 * @param {e} event
 */
function resize(e) {
  const w = (-28 + e.clientX) / _left.parentElement.clientWidth * 100;
  _left.style.width = Math.min(85, Math.max(10, w)) + "%";
}

/**
 * Stops resizing.
 * @param {e} event
 */
function stopResize(e) {
  document.removeEventListener("pointermove", resize);
  document.removeEventListener("pointerup", stopResize);
  _resizer.releasePointerCapture(e.pointerId);
  localStorage.setItem("editorWidth", _left.style.width.replace(/%/, ''));
}
