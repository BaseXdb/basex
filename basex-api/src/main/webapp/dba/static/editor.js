
/**
 * Opens a query file.
 * @param {string} file  optional file name
 */
function openQuery(file) {
  if(_editorMirror.getValue().trim() && !confirm("Replace editor contents?")) return;

  var name;
  if(file) {
    name = file;
    document.getElementById("file").value = name;
    checkButtons();
  } else {
    name = fileName();
  }
  request("POST", "open-query?name=" + encodeURIComponent(name),
    null,
    function(req) {
      _editorMirror.setValue(req.responseText);
      setInfo("Query was opened.");
      _editorMirror.focus();
    },
    function(req) {
      setError("Query could not be opened.");
    }
  )
};

/**
 * Saves a query file.
 */
function saveQuery() {
  if(queryExists() && !confirm("Overwrite existing query?")) return;

  request("POST", "save-query?name=" + encodeURIComponent(fileName()),
    document.getElementById("editor").value,
    function(req) {
      refreshDataList(req);
      setInfo("Query was saved.");
    },
    function(req) {
      setError("Query could not be saved.");
    }
  )
};

/**
 * Refreshes the list of available query files.
 * @param {object} req  HTTP request
 */
function refreshDataList(req) {
  var files = document.getElementById("files");
  while(files.firstChild) {
    files.removeChild(files.firstChild);
  }
  var names = req.responseText.split("/");
  for (var i = 0; i < names.length; i++) {
    var opt = document.createElement("option");
    opt.value = names[i];
    files.appendChild(opt);
  }
  checkButtons();
};

/**
 * Refreshes the editor buttons.
 */
function checkButtons() {
  document.getElementById("open").disabled = !queryExists();
  document.getElementById("save").disabled = fileName().length == 0;
};

/**
 * Checks if the typed in query file exists.
 * @returns {boolean} result of check
 */
function queryExists() {
  var file = fileName();
  var files = document.getElementById("files").children;
  for (var f = 0; f < files.length; f++) {
    if(files[f].value == file) return true;
  }
  return false;
};

/**
 * Returns the current file name without file suffix
 * @returns {string} file name
 */
function fileName() {
  return document.getElementById("file").value.trim().replace(/\.xq$/, '');
};
