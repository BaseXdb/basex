function openQuery() {
  var file = document.getElementById("file");
  request("POST", "open-query?name=" + encodeURIComponent(file.value.trim()),
    null,
    function(req) {
      document.getElementById("editor").value = req.responseText;
      var evt = document.createEvent("HTMLEvents");
      evt.initEvent("change",false,false);
      document.getElementById("editor").dispatchEvent(evt);
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

function loadCodeMirror() {
  if (CodeMirror && dispatchEvent) {
    //Now replace the editor and result areas
    var editorArea = document.getElementById("editor");
    var codeMirrorEditor = CodeMirror.fromTextArea(editorArea, {
      mode: "xquery",
      lineNumbers: true,
      extraKeys: {
        "Ctrl-Enter": function(cm) {
           editor("Please wait…", "Query was successful.", true);
        },
        "Cmd-Enter": function(cm) {
           editor("Please wait…", "Query was successful.", true);
        }
      }
    });
    codeMirrorEditor.on("change",function(cm, cmo) {
        cm.save();
        editor("Please wait…", "Query was successful.", false);
      }
    );
    codeMirrorEditor.display.wrapper.style.border = "solid 1pt black";
    editorArea.addEventListener("change",function() {
      codeMirrorEditor.setValue(editorArea.value);
    });
    var outputArea = document.getElementById("output");
    var codeMirrorResult = CodeMirror.fromTextArea(outputArea, {mode: "xml"});
    codeMirrorResult.display.wrapper.style.border = "solid 1pt black";
    outputArea.addEventListener("change",function() {codeMirrorResult.setValue(outputArea.value)});
    window.addEventListener("load",setDisplayHeight);
    window.addEventListener("resize",setDisplayHeight);
  }
}

function setDisplayHeight() {
  var elem = document.createElement("div");
  document.body.appendChild(elem);
  p = elem.offsetTop + 48 //To account for margin, it works but is not bullet proof.
  var c = document.getElementsByClassName("CodeMirror")[0].offsetHeight;
  var s = window.innerHeight;
  Array.prototype.forEach.call(document.getElementsByClassName("CodeMirror"),function(cm) {
    cm.CodeMirror.setSize("100%",Math.max(200,s-(p-c)));
  });
}
