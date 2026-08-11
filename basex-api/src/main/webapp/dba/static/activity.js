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
  for(const [ id, html ] of [["jobs-panel", json.jobs], ["web-panel", json.web], ["db-panel", json.db]]) {
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
  // the next request follows the answer, so that a slow one cannot queue up others
  if(wasLive) _live = setTimeout(refreshActivity, REFRESH_INTERVAL);
}

/** The activity view keeps its panels up to date over its own connection. */
_handlers["/activity"] = showActivity;
_live_actions.activity = refreshActivity;

/**
 * Prepares the activity view: the result of a shown job, and the refresh if it was left on.
 */
function initActivity() {
  loadCodeMirror("xml");
  // refreshActivity checks the 'Live' state itself
  refreshActivity();
}
