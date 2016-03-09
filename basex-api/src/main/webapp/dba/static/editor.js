function openQuery() {
  if(!confirm('Replace editor contents?')) return;

  var file = document.getElementById("file");
  request("POST", "open-query?name=" + encodeURIComponent(file.value.trim()),
    null,
    function(req) {
      _editorMirror.setValue(req.responseText);
    },
    function(req) {
      setError('Query could not be opened.');
    }
  )
};

function saveQuery() {
  if(queryExists() && !confirm('Overwrite existing query?')) return;

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

function deleteQuery() {
  if(!confirm('Are you sure?')) return;

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

function refreshDataList(req) {
  var files = document.getElementById("files");
  while(files.firstChild) {
    files.removeChild(files.firstChild);
  }
  var names = req.responseText.split('/');
  for (var i = 0; i < names.length; i++) {
    var opt = document.createElement('option');
    opt.value = names[i];
    files.appendChild(opt);
  }
  checkButtons();
};

function checkButtons() {
  var file = document.getElementById("file").value.trim();
  document.getElementById("save").disabled = file.length == 0;
  var disable = !queryExists();
  document.getElementById("open").disabled = disable;
  document.getElementById("delete").disabled = disable;
};

function queryExists() {
  var file = document.getElementById("file").value.trim();
  var files = document.getElementById("files").children;
  for (var i = 0; i < files.length; i++) {
    if(files[i].value == file) return true;
  }
  return false;
};
