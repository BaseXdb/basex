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
      finishFile(name, "File was opened.");
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
  var name = fileName();
  if(!name.includes(".")) name += ".xq";
  if(fileExists(name) && !confirm("Overwrite existing file?")) return;

  request("POST", "editor-save?name=" + encodeURIComponent(name),
    document.getElementById("editor").value,
    function(req) {
      finishFile(name, "File was saved.");
      refreshDataList(req);
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
      _editorMirror.setValue("");
      finishFile("", "File was closed.");
    },
    function(req) {
      setErrorFromResponse(req);
    }
  );
}

/**
 * Finish file operation.
 * @param {string} name new filename
 * @param {string} info info message
 */
function finishFile(name, info) {
  document.getElementById("file").value = name;
  document.getElementById("run").disabled = name && !name.match(/\.xq(m|l|uery)?$/i);
  checkButtons();
  setInfo(info);
  _editorMirror.focus();
};

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
}

/**
 * Refreshes the editor buttons.
 */
function checkButtons() {
  var name = fileName();
  var exists = fileExists(name);
  document.getElementById("open").disabled = !exists;
  document.getElementById("save").disabled = !name;
  document.getElementById("close").disabled = !exists;
}

/**
 * Checks if the specified file exists.
 * @param {string} filename
 * @returns {boolean} result of check
 */
function fileExists(file) {
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
