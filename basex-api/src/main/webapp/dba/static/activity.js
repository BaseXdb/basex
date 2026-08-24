/** Activity view: jobs and sessions of the server. */

/**
 * Requests the panels of the activity view. The answer is pushed back by the server;
 * see showActivity, which asks for the next one.
 */
async function refreshActivity() {
  // a single chain: a request that is started while one is pending replaces it
  clearTimeout(_live);
  if(!liveOn()) return;
  const params = new URLSearchParams(window.location.search);
  // a job that is done does not change any more, and is not requested again
  const done = document.getElementById("job-details")?.dataset.done === "true";
  const message = {
    type: "panels",
    sort: params.get("sort") ?? "",
    job: done ? "" : params.get("job") ?? ""
  };
  // no connection: retry, rather than leaving the panels frozen for good
  if(!await sendMessage("/activity", message)) _live = setTimeout(refreshActivity, REFRESH_INTERVAL);
}

/**
 * Shows the panels that were pushed by the server, and requests the next ones.
 * @param {object} json message with the contents of the panels
 */
function showActivity(json) {
  // the 'Live' checkbox is part of the replaced markup, and the server always renders it as
  // ticked: without this, unticking it would be undone by the answer that is still on its way
  const wasLive = liveOn();
  // a question is answered by clicking the button it was asked from, and a dialog submits the
  // form it belongs to: while one is open, the panels are left as they are, and the answer
  // that arrives a second later is applied instead
  if(!dialogOpen()) {
    for(const [ id, html ] of [["jobs-panel", json.jobs], ["web-panel", json.web],
        ["db-panel", json.db], ["ws-panel", json.ws], ["caches-panel", json.caches]]) {
      const panel = document.getElementById(id);
      if(!panel) continue;
      // the entries are replaced: what the user ticked in the meantime is restored
      const checked = [ ...panel.querySelectorAll("input[name]:checked") ].map(i => i.value);
      panel.innerHTML = html;
      for(const input of panel.querySelectorAll("input[name]")) {
        input.checked = checked.includes(input.value);
      }
    }
    // a running job's details are replaced as well; the final ones are applied once, together
    // with the editor for its result, and are then left alone
    const details = document.getElementById("job-details");
    if(details && json.job && details.dataset.done !== "true") {
      details.innerHTML = json.job;
      details.dataset.done = json.done;
      if(json.done) loadCodeMirror("xml");
    }

    const live = document.getElementById("live");
    if(live) live.checked = wasLive;

    buttons();
    markTruncated();
  }
  // the next request follows the answer, so that a slow one cannot queue up others
  if(wasLive) _live = setTimeout(refreshActivity, REFRESH_INTERVAL);
}

/** The activity view keeps its panels up to date over its own connection. */
_handlers["/activity"] = showActivity;
_live_actions.activity = refreshActivity;

/**
 * Opens the dialog that assigns an attribute of a session or of a WebSocket connection. The
 * panel it is opened from is replaced by the refresh, which is why the dialog is filled in
 * from the row that was clicked.
 * @param {DOMStringMap} data dataset of the link: what holds the attribute ('session',
 *          'websocket'), its id, and the name of the attribute
 */
async function setAttribute({ kind, id, name }) {
  document.getElementById(`${kind}-id`).value = id;
  document.getElementById(`${kind}-text`).textContent = id;
  document.getElementById(`${kind}-name`).value = name;
  // the value is fetched before the dialog opens: it is what the user edits
  let value = { text: "", note: "" };
  try {
    value = JSON.parse(await request(`${kind}-value?${new URLSearchParams({ id, name })}`));
  } catch(response) {
    // the dialog is opened either way: a value that cannot be read can still be replaced
    showError(response, name);
  }
  setEditorText(`${kind}-value`, value.text);
  const note = document.getElementById(`${kind}-note`);
  [ note.textContent, note.className ] = value.note ? [ value.note, "note warn" ] : [ "", "note" ];
  showDialog(kind);
}

/**
 * Prepares the activity view: the result of a shown job, and the refresh if it was left on.
 */
function initActivity() {
  // the query of the dialog and the definition of a service are edited, a result is only shown
  loadCodeMirror("xquery", [ "job-query", "job-string", "session-value", "websocket-value" ]);

  // the download of a result that the view has already given up; the browser keeps the page
  const download = document.getElementById("download-form");
  if(download) {
    download.submit();
    // a reload must not ask for a result that is gone by then
    hideParams("download");
  }

  // refreshActivity checks the 'Live' state itself
  refreshActivity();
}
