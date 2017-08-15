/**
 * Opens a query file.
 * @param {string} file  optional file name
 */
function openQuery(file) {
  if(_editorMirror.getValue() != "" && !confirm("Replace editor contents?")) return;

  var name;
  if(file) {
    name = file;
    document.getElementById("file").value = name;
    checkButtons();
  } else {
    name = document.getElementById("file").value.trim();
  }
  request("POST", "open-query?name=" + encodeURIComponent(name),
    null,
    function(req) {
      _editorMirror.setValue(req.responseText);
      setInfo("File was opened.");
    },
    function(req) {
      setError("File could not be opened: " + name);
    }
  )
};

/**
 * Saves a query file.
 */
function saveQuery() {
  if(queryExists() && !confirm("Overwrite existing query?")) return;

  var file = document.getElementById("file");
  request("POST", "save-query?name=" + encodeURIComponent(file.value.trim()),
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
 * Deletes a query file.
 */
function deleteQuery() {
  if(!confirm("Are you sure?")) return;

  var file = document.getElementById("file");
  request("POST", "delete-query?name=" + encodeURIComponent(file.value.trim()),
    null,
    function(req) {
      refreshDataList(req);
      setInfo("Query was deleted");
      file.value = "";
    },
    function(req) {
      setError("Query could not be deleted.");
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
  var file = document.getElementById("file").value.trim();
  document.getElementById("save").disabled = file.length == 0;
  var disable = !queryExists();
  document.getElementById("open").disabled = disable;
  document.getElementById("delete").disabled = disable;
};

/**
 * Checks if the typed in query file exists.
 * @returns {boolean} result of check
 */
function queryExists() {
  var file = document.getElementById("file").value.trim();
  var files = document.getElementById("files").children;
  for (var f = 0; f < files.length; f++) {
    if(files[f].value == file) return true;
  }
  return false;
};
