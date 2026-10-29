/** Activity view: jobs and sessions of the server. */

/** Duration of the fade of a row that was added by a refresh, in milliseconds. */
const ADDED_MS = 3000;
/** Times when rows were added, keyed by panel and row id. */
const _added = new Map();

/** The panels of the view, all of them sorted lists, by the block they are filled into. */
const LISTS = [ "jobs-panel", "clients-panel", "caches-panel", "db-panel" ];
/** localStorage key prefix for the checkboxes that choose what the lists show. */
const FILTER_KEY = "dba-activity-";
/** Whether the checkboxes are ticked, by their ids: the WebSocket connections and the idle
    sessions in the clients panel, the finished jobs in the jobs panel. */
const _listFilters = Object.fromEntries([ "websockets", "idle", "finished" ].map(
  id => [ id, stored(FILTER_KEY + id) === "yes" ]));

/**
 * Applies a checkbox that chooses what the lists show; the choice is remembered.
 * @param {HTMLInputElement} box checkbox
 */
function filterChanged(box) {
  _listFilters[box.id] = box.checked;
  store(FILTER_KEY + box.id, box.checked ? "yes" : null);
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
  // a job that is done does not change any more, and is not requested again
  const done = document.getElementById("job-details")?.dataset.done === "true";
  const message = {
    type: "panels",
    // every list is sorted as it is shown
    sorts: Object.fromEntries(LISTS.map(id => [ id, sortedList(id)?.dataset.sort ?? "" ])),
    // and keeps the pages that were loaded
    pages: Object.fromEntries(LISTS.map(id => [ id, shownPages(id) ])),
    // a folded panel is not rendered; it is asked for once it is opened
    open: LISTS.filter(id => !document.getElementById(id)?.closest(".panel")
      .classList.contains("collapsed")),
    idle: _listFilters.idle,
    websockets: _listFilters.websockets,
    finished: _listFilters.finished,
    job: done ? "" : jobParam() ?? ""
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
    showJobDetails(json);

    // the checkboxes are rendered with the state they were asked for, which can be older than
    // the one they were given since
    for(const [ id, checked ] of [ [ "live", wasLive ], ...Object.entries(_listFilters) ]) {
      const box = document.getElementById(id);
      if(box) box.checked = checked;
    }
  }
  scheduleLive(refreshActivity, wasLive);
}

/** The activity view keeps its panels up to date over its own connection, and the details of a
    job along with them. */
_handlers["/activity"] = showActivity;
_requestJob = () => refreshActivity(true);
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
  // the value is inserted once the dialog is open: a hidden editor that is shown is measured
  // line by line, which freezes the page for a large value
  showValue(kind, { text: "", note: "" });
  showDialog(kind);
  showValue(kind, await requestJson(`${kind}-value`, { id, name }, name));
}

/**
 * Requests what a dialog shows: by default, a value as the expression that yields it again.
 * @param {string} path path of the endpoint
 * @param {object} params query parameters that name what is requested
 * @param {string} label what is reported if it cannot be read
 * @param {object} fallback result if it cannot be read
 * @returns {Promise} promise, resolved with the parsed result
 */
async function requestJson(path, params, label, fallback = { text: "", note: "" }) {
  try {
    return JSON.parse(await request(`${path}?${new URLSearchParams(params)}`));
  } catch(response) {
    // the dialog is opened either way: what cannot be read can still be replaced or assigned
    showError(response, label);
    return fallback;
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
  const result = await requestJson("cache-keys", { name }, name, { keys: [], count: 0 });
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
    value = await requestJson("cache-value", { name, key }, key);
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
  const keys = cacheKeys();
  if(!await confirmDialog(`Remove ${plural(keys.length, "entry", "entries")}?`, "Remove")) return;
  const params = new URLSearchParams({ name: document.getElementById("cache-name").value });
  for(const key of keys) params.append("key", key);
  try {
    setText(await request(`cache-remove?${params}`), "info");
  } catch(response) {
    showError(response);
  }
  await listCacheKeys();
}

/**
 * Prepares the activity view: the result of a shown job, and the refresh if it was left on.
 */
function initActivity() {
  initJobDetails();

  // the query of a new job and the values of the dialogs; the details of a job load their own
  loadCodeMirror("xquery", [ "job-query", "session-value", "websocket-value", "cache-value" ]);

  // refreshActivity checks the 'Live' state itself; the page is rendered with no checkbox
  // ticked, so it is replaced at once if one of them was
  refreshActivity(Object.values(_listFilters).some(Boolean));
}
