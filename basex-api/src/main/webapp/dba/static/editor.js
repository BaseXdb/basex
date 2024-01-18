/**
 * Opens a file.
 * @param {string} file optional file name
 */
function openFile(file) {
  if(_editorMirror.getValue().trim() && !confirm("Replace editor contents?")) return;

  var name = file || fileName();
  request("POST", "editor-open?name=" + encodeURIComponent(name),
    null,
    function(request) {
      _editorMirror.setValue(request.responseText);
      _editorMirror.focus();
      document.getElementById("file").value = name;
      checkButtons();
      setInfo("File was opened.");
    },
    function(req) {
      setErrorFromResponse(req, name);
    }
  )
}

/**
 * Saves a file.
 */
function saveFile() {
  // append file suffix
  var value = fileName();
  if(value.indexOf(".") === -1) {
    value += ".xq";
    document.getElementById("file").value = value;
  }
  if(fileExists() && !confirm("Overwrite existing file?")) return;

  request("POST", "editor-save?name=" + encodeURIComponent(value),
    document.getElementById("editor").value,
    function(req) {
      refreshDataList(req);
      setInfo("File was saved.");
    },
    function(req) {
      setErrorFromResponse(req);
    }
  )
}

/**
 * Closes a file.
 */
function closeFile() {
  request("POST", "editor-close?name=" + encodeURIComponent(fileName()),
    null,
    function(req) {
      document.getElementById("file").value = "";
      _editorMirror.setValue("");
      _editorMirror.focus();
      checkButtons();
      setInfo("File was closed.");
    },
    function(req) {
      setErrorFromResponse(req);
    }
  );
}

/**
 * Refreshes the list of available files.
 * @param {object} request HTTP request
 */
function refreshDataList(request) {
  var files = document.getElementById("files");
  while(files.firstChild) {
    files.removeChild(files.firstChild);
  }
  var names = request.responseText.split("/");
  for (var i = 0; i < names.length; i++) {
    var opt = document.createElement("option");
    opt.value = names[i];
    files.appendChild(opt);
  }
  checkButtons();
}

/**
 * Refreshes the editor buttons.
 */
function checkButtons() {
  document.getElementById("open").disabled = !fileExists();
  document.getElementById("save").disabled = fileName().length === 0;
  document.getElementById("close").disabled = !fileExists();
}

/**
 * Checks if the typed in file exists.
 * @returns {boolean} result of check
 */
function fileExists() {
  var file = fileName();
  var files = document.getElementById("files").children;
  for (var f = 0; f < files.length; f++) {
    if(files[f].value === file) return true;
  }
  return false;
}

/**
 * Returns the current file name without file suffix
 * @returns {string} file name
 */
function fileName() {
  return document.getElementById("file").value.trim();
}
