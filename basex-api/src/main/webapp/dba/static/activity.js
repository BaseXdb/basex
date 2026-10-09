/** Activity view: jobs and sessions of the server. */

/** Duration of the fade of a row that was added by a refresh, in milliseconds. */
const ADDED_MS = 3000;
/** Times when rows were added, keyed by panel and row id. */
const _added = new Map();

/** The sorted lists of the view, by the block they are filled into. */
const LISTS = [ "jobs-panel", "caches-panel", "db-panel" ];
/** The panels of the view, by the block they are filled into. */
const PANELS = [ ...LISTS, "clients-panel" ];
/** Whether the clients panel lists the idle sessions. */
let _idle = false;
/** Whether the jobs of the DBA itself are listed. */
let _dbaJobs = false;

/**
 * Lists the idle sessions, or stops listing them.
 */
function idleChanged() {
  _idle = document.getElementById("idle").checked;
  refreshActivity(true);
}

/**
 * Lists the jobs of the DBA itself, or stops listing them.
 */
function dbaJobsChanged() {
  _dbaJobs = document.getElementById("dba-jobs").checked;
  refreshActivity(true);
}

/**
 * Returns the states of the listed jobs. The queries of the DBA itself are left out: they are
 * started and ended by the one who is looking at them.
 * @returns {Map} states, by job id
 */
function jobStates() {
  const rows = document.querySelectorAll("#jobs-panel tbody tr[id]");
  return new Map([ ...rows ].filter(row => !row.id.startsWith("dba:")).
    map(row => [ row.id, row.cells[1]?.textContent.trim() ]));
}

/**
 * Announces to screen readers how the jobs have changed.
 * @param {Map} before states of the jobs before the refresh
 * @param {Map} after states of the jobs after the refresh
 */
function announceJobs(before, after) {
  const changes = [];
  for(const [ id, state ] of after) {
    if(!before.has(id)) changes.push(`Job ${id} was added: ${state}`);
    else if(before.get(id) !== state) changes.push(`Job ${id}: ${state}`);
  }
  for(const id of before.keys()) {
    if(!after.has(id)) changes.push(`Job ${id} has ended`);
  }
  if(changes.length) announce(changes.join(". "));
}

/**
 * Requests the panels of the activity view. The answer is pushed back by the server;
 * see showActivity, which asks for the next one.
 * @param {boolean} force request the panels even if the view is not refreshed by itself
 */
async function refreshActivity(force) {
  // a single chain: a request that is started while one is pending replaces it
  clearTimeout(_live);
  if(!force && !liveOn()) return;
  const params = new URLSearchParams(window.location.search);
  // a job that is done does not change any more, and is not requested again
  const done = document.getElementById("job-details")?.dataset.done === "true";
  const message = {
    type: "panels",
    // every list is sorted as it is shown
    sorts: Object.fromEntries(LISTS.map(id => [ id, sortedList(id)?.dataset.sort ?? "" ])),
    // and keeps the pages that were loaded
    pages: Object.fromEntries(LISTS.map(id => [ id, shownPages(id) ])),
    // a folded panel is not rendered; it is asked for once it is opened
    open: PANELS.filter(id => !document.getElementById(id)?.closest(".panel")
      .classList.contains("collapsed")),
    idle: _idle,
    dba: _dbaJobs,
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
  // that arrives a second later is applied instead. The details of a job are refreshed along
  // with the panels behind them
  if(!document.querySelector("dialog[open]:not(#details-dialog)")) {
    // rows whose fade is over are forgotten, including the ones that are gone
    const now = Date.now();
    for(const [ key, time ] of _added) {
      if(now - time >= ADDED_MS) _added.delete(key);
    }
    // every panel is named by the block it is filled into; what was ticked in the meantime is
    // ticked again by fillPanel
    // a panel that has not changed is left alone, along with what is pointed at in it
    for(const [ id, html ] of Object.entries(json.panels)) {
      const pane = document.getElementById(id);
      if(pane && _filled.get(pane) !== html) {
        // rows are named by what they list: a name that was not shown before marks a new entry
        const ids = new Set([ ...pane.querySelectorAll("tr[id]") ].map(row => row.id));
        const states = id === "jobs-panel" ? jobStates() : null;
        // a screen reader waits until the panel is complete again
        pane.setAttribute("aria-busy", "true");
        fillPanel(id, html);
        pane.removeAttribute("aria-busy");
        if(states) announceJobs(states, jobStates());
        for(const row of pane.querySelectorAll("tr[id]")) {
          const key = `${id}/${row.id}`;
          if(!ids.has(row.id)) _added.set(key, now);
          // a row is replaced while it fades in: the fade is resumed where it was
          const time = _added.get(key);
          if(time !== undefined) {
            row.classList.add("added");
            row.style.setProperty("--added-delay", `-${now - time}ms`);
          }
        }
      }
    }
    // a running job's details are replaced as well; the final ones are applied once, together
    // with the editor for its result, and are then left alone
    const details = document.getElementById("job-details");
    if(details && json.job && details.dataset.done !== "true") {
      details.innerHTML = json.job;
      details.dataset.done = json.done;
      // the query of a service is edited; the result is shown by the same call
      if(json.done) loadCodeMirror("xquery", [ "job-string" ]);
      buttons();
      markTruncated(details);
    }

    const live = document.getElementById("live");
    if(live) live.checked = wasLive;
    // as the 'Live' checkbox, the one for idle sessions is rendered with the state it was asked
    // for, which can be older than the one it was given since
    const idle = document.getElementById("idle");
    if(idle) idle.checked = _idle;
    const dbaJobs = document.getElementById("dba-jobs");
    if(dbaJobs) dbaJobs.checked = _dbaJobs;
  }
  scheduleLive(refreshActivity, wasLive);
}

/** The activity view keeps its panels up to date over its own connection. */
_handlers["/activity"] = showActivity;
_live_actions.activity = refreshActivity;
/** A panel that is opened was not refreshed while it was folded: it is asked for at once. */
_panel_opened = () => refreshActivity(true);

/** The sort and page links of the lists are followed in place: the list states its new order
    and pages, and the panels are asked for at once, whether or not the view refreshes itself. */
followPanelLinks(Object.fromEntries(LISTS.map(id => [ id, (sort, page) => {
  const list = sortedList(id);
  if(list) Object.assign(list.dataset, { sort: sort ?? list.dataset.sort, page });
  refreshActivity(true);
} ])));

/**
 * Opens the dialog that assigns an attribute of a session or of a WebSocket connection. The
 * panel it is opened from is replaced by the refresh, which is why the dialog is filled in
 * from the row that was clicked.
 * @param {DOMStringMap} data dataset of the link: what holds the attribute ('session',
 *          'websocket'), its id, and the name of the attribute
 */
async function editAttribute({ kind, id, name }) {
  document.getElementById(`${kind}-id`).value = id;
  document.getElementById(`${kind}-text`).textContent = id;
  document.getElementById(`${kind}-name`).value = name;
  // the value is fetched before the dialog opens: it is what the user edits
  showValue(kind, await requestValue(`${kind}-value`, { id, name }, name));
  showDialog(kind);
}

/**
 * Requests a value as the expression that yields it again.
 * @param {string} path path of the endpoint
 * @param {object} params query parameters that name the value
 * @param {string} label what the value is reported as if it cannot be read
 * @returns {Promise} promise, resolved with the text and the note of the value
 */
async function requestValue(path, params, label) {
  try {
    return JSON.parse(await request(`${path}?${new URLSearchParams(params)}`));
  } catch(response) {
    // the dialog is opened either way: a value that cannot be read can still be replaced
    showError(response, label);
    return { text: "", note: "" };
  }
}

/**
 * Shows a value in the editor and the note of a dialog.
 * @param {string} kind prefix of the ids of the editor and the note
 * @param {object} value text and note of the value
 */
function showValue(kind, { text, note }) {
  setEditorText(`${kind}-value`, text);
  showNote(`${kind}-note`, note);
}

/**
 * Opens the dialog that shows the entries of a cache. As with attributes, the dialog is filled
 * in from the link that was clicked, as the panel is replaced by the refresh.
 * @param {string} name name of the cache; empty for the default cache
 */
async function showCache(name) {
  document.getElementById("cache-name").value = name;
  document.querySelector("#cache-dialog h2").textContent = `Cache: ${name || "(default)"}`;
  await listCacheKeys();
  showDialog("cache");
}

/**
 * Lists the keys of the cache whose dialog is shown; no key is selected afterwards.
 */
async function listCacheKeys() {
  const name = document.getElementById("cache-name").value;
  let result = { keys: [], count: 0 };
  try {
    result = JSON.parse(await request(`cache-keys?${new URLSearchParams({ name })}`));
  } catch(response) {
    // the dialog is opened either way: an entry can still be assigned
    showError(response, name);
  }
  document.getElementById("cache-keys").replaceChildren(
    ...result.keys.map(key => new Option(key, key)));
  const shown = result.keys.length;
  document.getElementById("cache-count").textContent = shown < result.count ?
    `${shown} of ${result.count} keys shown` : plural(result.count, "key");
  showCacheEntry();
}

/**
 * Returns the keys that are selected in the cache dialog.
 * @returns {Array} keys
 */
function cacheKeys() {
  return [ ...document.getElementById("cache-keys").selectedOptions ].map(option => option.value);
}

/**
 * Shows the selected entry of the cache dialog; the value is only shown for a single entry.
 */
async function showCacheEntry() {
  const keys = cacheKeys();
  setDisabled("cache-remove", keys.length === 0);
  const key = keys.length === 1 ? keys[0] : "";
  document.getElementById("cache-key").value = key;
  let value = { text: "", note: "" };
  if(key) {
    const name = document.getElementById("cache-name").value;
    value = await requestValue("cache-value", { name, key }, key);
    // the selection may have changed while the value was requested
    const now = cacheKeys();
    if(now.length !== 1 || now[0] !== key) return;
  }
  showValue("cache", value);
}

/**
 * Removes the selected entries of the cache dialog, which stays open.
 */
async function removeCacheEntries() {
  const params = new URLSearchParams({ name: document.getElementById("cache-name").value });
  for(const key of cacheKeys()) params.append("key", key);
  try {
    setText(await request(`cache-remove?${params}`), "info");
  } catch(response) {
    showError(response);
  }
  await listCacheKeys();
}

/**
 * Shows the details of a job in place; the address names the job, as a step of its own.
 * @param {string} job job id
 */
function selectJob(job) {
  pushParams({ job });
  showJob();
}

/**
 * Opens the details of the job that the address names, or closes them if it names none. The
 * details are filled in by the refresh, which is asked for at once.
 */
function showJob() {
  const dialog = document.getElementById("details-dialog");
  const job = new URLSearchParams(window.location.search).get("job");
  if(!job) {
    dialog.close();
    return;
  }
  // the heading is what is known before the details arrive
  const details = document.getElementById("job-details");
  const heading = document.createElement("h2");
  heading.textContent = `Job: ${job}`;
  details.replaceChildren(heading);
  details.dataset.done = "false";
  if(!dialog.open) dialog.showModal();
  refreshActivity(true);
}

/**
 * Prepares the activity view: the result of a shown job, and the refresh if it was left on.
 */
function initActivity() {
  // the details of a job are opened first: the editors in them are measured when they are shown
  const dialog = document.getElementById("details-dialog");
  if(new URLSearchParams(window.location.search).get("job")) dialog.showModal();
  // a closed job is no longer asked for, and a reload does not open it again
  dialog.addEventListener("close", () => hideParams("job"));
  // the back button steps through the jobs that were opened
  initSelection(() => {}, showJob);

  // the query of the dialog and the definition of a service are edited, a result is only shown
  loadCodeMirror("xquery", [ "job-query", "job-string", "session-value", "websocket-value",
    "cache-value" ]);

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
