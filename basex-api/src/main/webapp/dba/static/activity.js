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
  if(!await sendMessage("/activity", message)) scheduleLive(refreshActivity);
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
    // every panel is named by the block it is filled into; what was ticked in the meantime is
    // ticked again by fillPanel
    for(const [ id, html ] of Object.entries(json.panels)) {
      if(document.getElementById(id)) fillPanel(id, html);
    }
    // a running job's details are replaced as well; the final ones are applied once, together
    // with the editor for its result, and are then left alone
    const details = document.getElementById("job-details");
    if(details && json.job && details.dataset.done !== "true") {
      details.innerHTML = json.job;
      details.dataset.done = json.done;
      if(json.done) loadCodeMirror("xml");
      buttons();
      markTruncated(details);
    }

    const live = document.getElementById("live");
    if(live) live.checked = wasLive;
  }
  scheduleLive(refreshActivity, wasLive);
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
  showNote(`${kind}-note`, value.note);
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
