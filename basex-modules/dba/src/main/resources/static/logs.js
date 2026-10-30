/** Log view: searching and monitoring log entries. */

/** Most recent log entry search state. */
let _logInput;

/** Number of log files that the search covers. */
let _logFiles = 1;

/** Indicates whether the entry named in the address still has to be scrolled to. */
let _logJump = window.location.hash !== "";

/**
 * Queries the entries of the log files that are searched: the checked ones, or the file that
 * is opened.
 * @param {string} key 'Enter', or the cause of the request ('input', 'select', 'filter'); omitted
 *   for a plain refresh
 */
function logEntries(key) {
  const reset = key && key !== "Enter";
  const input = fieldValue("input");
  const ignore = fieldValue("ignore");
  const filters = document.querySelectorAll("input.filter");
  // the checked files are what is listed and searched; without a selection, the opened file
  const checked = [ ...document.querySelectorAll("#list input[name=name]:checked") ].
    map(input => input.value);
  const dates = checked.length ? checked : [ document.getElementById("date").value ];
  // the filter fields belong to the rendered table, so they are missing until the first
  // result arrives; empty ones must not count, or the first key press after a search
  // would look like a new search. Before that, the stored ones
  // are sent, and the server renders them into the fields
  const typed = filters.length ?
    [ ...filters ].map(f => [ f.name, f.value.trim() ]).filter(([ , value ]) => value) :
    storedJson(fieldKey("filters"), [], value => Array.isArray(value) && value.every(pair =>
      Array.isArray(pair) && pair.length === 2 && pair.every(s => typeof s === "string")));
  storeField("input");
  storeField("ignore");
  store(fieldKey("filters"), typed.length ? JSON.stringify(typed) : null);
  const state = JSON.stringify([ input, ignore, dates, typed ]);
  if(reset && _logInput === state) return;
  _logInput = state;
  // what a previous search reported is outdated; the message of the page and a live refresh
  // leave the status line alone
  if(key) setText("", "");
  // a new search or filter starts at the top
  if(reset) _toTop.add("output");

  // a range of files is not reloaded every second: the live refresh follows a single file
  _logFiles = dates.length;
  const live = document.getElementById("live");
  if(live) live.disabled = _logFiles > 1;
  // what is searched is stated where the date of a single file is
  const heading = document.querySelector(".searchbar h2");
  if(heading) heading.textContent = _logFiles > 1 ? `Logs: ${_logFiles} files` : `Log: ${dates[0]}`;

  const message = {
    type: "entries",
    input,
    ignore,
    dates,
    sort: document.getElementById("sort").value,
    // a new search or filter keeps the number of entries that are shown
    page: Number(document.getElementById("page").value) || 1,
    // the entry that was jumped to belongs to the opened file
    entry: checked.length ? "" : document.getElementById("entry").value,
    // the fields are named after the columns they filter, with a prefix of their own
    filters: Object.fromEntries(typed.map(([ name, value ]) => [ name.replace(/^f-/, ""), value ]))
  };
  // the server stops a search that is superseded by a newer one; a search that takes longer
  // than a moment says so, and the message is revoked by showMessage when the entries arrive
  sendRequest("/logs", message);

  // refresh browser history, so that a reload shows what the page shows
  // the number of shown pages is not kept: a reload starts with the first one, unless an entry
  // that was jumped to has to be shown again
  setParams({ input, page: message.entry ? message.page : "", sort: message.sort });
}

/**
 * Shows the log entries that were pushed by the server.
 * @param {string} text HTML table
 */
function showLogEntries(text) {
  const output = document.getElementById("output");
  // the filter fields belong to the replaced table: the one that was typed into keeps the
  // focus and the caret
  refocus(() => {
    output.innerHTML = text;
    if(_toTop.delete("output")) output.closest(".pane")?.scrollTo(0, 0);
    markTruncated(output);
    // a table that is more than the first page can be returned to
    document.getElementById("top").hidden = shownPages("output") === 1 &&
      !output.querySelector("[data-offset]");
    // a range of files is not reloaded: the live refresh follows a single file
    scheduleLive(() => logEntries(), liveOn() && _logFiles === 1);
    // the entry that was jumped to is scrolled to once; a live refresh leaves the position alone
    if(_logJump) {
      _logJump = false;
      document.getElementById(decodeURIComponent(window.location.hash.slice(1)))?.
        scrollIntoView({ block: "center" });
    }
  }, "input.filter");
}

/**
 * Restores the stored text fields, then loads the log entries.
 */
function initLogs() {
  // an entry that is jumped to is shown in the whole log, apart from the ignored entries
  if(document.getElementById("entry").value) {
    store(fieldKey("input"), null);
    store(fieldKey("filters"), null);
  }
  // a search of the address wins over the stored one
  restoreField("input");
  restoreField("ignore");
  _panel_filled.list();
  // the checked files are what is listed, so a new selection is a new request; the clicks of a
  // row are collected first. Clicks, not changes: the checkbox of the table header ticks the
  // rows by script, which raises no change event of its own
  document.getElementById("list").addEventListener("click", event => {
    if(event.target.matches("input[type=checkbox]")) {
      filterKey("select", "logEntries", () => logEntries("select"));
    }
  });
  logEntries();
}

/**
 * Shows the first page of entries, without an entry that was jumped to.
 * @param {object} params further query parameters of the address
 */
function showFirstPage(params) {
  document.getElementById("page").value = 1;
  document.getElementById("entry").value = "";
  // a reload shows what the page shows; the fragment named the entry
  const url = new URL(replaceParams(window.location.href, { ...params, page: "", entry: "" }));
  url.hash = "";
  window.history.replaceState(null, "", url);
  logEntries();
}

/**
 * Shows the first page of entries instead of the ones that were jumped to or loaded below it.
 */
function showTop() {
  _toTop.add("output");
  showFirstPage({});
}

/**
 * Filters log files.
 */
function logFilter() {
  const value = document.getElementById("log-filter").value;
  storeField("log-filter");
  const list = document.getElementById("dates");
  let unchecked = false;
  filterRows(list, list.querySelectorAll("tbody tr"), row => {
    const input = row.querySelector("input[name=name]");
    const visible = !value || input.value.startsWith(value);
    if(!visible && input.checked) {
      // a file that is filtered out is no longer listed
      input.checked = false;
      unchecked = true;
    }
    return visible;
  });
  // the buttons are derived from the selection
  buttons();
  if(unchecked) logEntries("filter");
}

/**
 * Shows the entries of another log file. The search and the order it is shown in are kept; the
 * page and the highlighted entry are not, as both refer to the file that was left.
 * @param {string} date name of the log file
 */
function selectLog(date) {
  const file = document.getElementById("date");
  if(date === file.value) return;
  file.value = date;
  mark("list", date);
  showFirstPage({ name: date });
}

/** Requests the list of log files; see panelRequest. */
const refreshFiles = panelRequest("/logs", "list", () => ({ type: "files",
  date: document.getElementById("date").value, entries: document.getElementById("sort").value }));

/** The sort and page links of the entry table are followed in place: they name what the table
    shows, and the fields that state it are what the next request reads. */
followPanelLinks({ output: (sort, page) => {
  if(sort !== undefined) document.getElementById("sort").value = sort;
  document.getElementById("page").value = page;
  logEntries();
}, list: refreshFiles });

/** The filter of the log files is part of the replaced list, and is applied again. */
_panel_filled.list = () => {
  if(restoreField("log-filter")) logFilter();
};

/** The log view receives its entries as a rendered table. */
_handlers["/logs"] = json => {
  if(json.type === "result") showLogEntries(json.result);
};
_live_actions.logs = logEntries;
