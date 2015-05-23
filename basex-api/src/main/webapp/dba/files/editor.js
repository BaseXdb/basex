function openQuery() {
  var file = document.getElementById("file");
  request("POST", "open-query?name=" + encodeURIComponent(file.value.trim()),
    null,
    function(req) {
      document.getElementById("editor").value = req.responseText;
      //The next line sends a 'change' event to all listeners to the 'editor'
      //This allows the syntax highlighter to hear that the text area value has changed
      //Programatic changes to the value don't send events automatically
      document.getElementById("editor").dispatchEvent(new Event("change",{ bubbles: false, cancelable: false }));
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

/**
 * Replaces the 'editor' text area with a CodeMirror syntax-highlighted editor
 * Adds event listeners to each to synchronise changes between the two
 *
 */
function replaceEditor() {
  if (CodeMirror) {
    var editor = document.getElementById("editor")
    var codeMirrorEditor = CodeMirror.fromTextArea(editor);
    codeMirrorEditor.on("change",function(cm, cmo) { cm.save() });
    codeMirrorEditor.display.wrapper.style.border = "solid 1pt black";
    editor.addEventListener("change",function() {codeMirrorEditor.setValue(editor.value)});
  }
}
