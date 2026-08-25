/** Log view: searching and monitoring log entries. */

/** Most recent log entry search state. */
let _logInput;

/** Whether the search covers more than one log file. */
let _logFiles = 1;

/** localStorage key for the logs 'ignore entries' filter. */
const IGNORE_KEY = "dba-ignore-logs";

/**
 * Queries the entries of the log files that are searched: the checked ones, or the file that
 * is opened.
 * @param {string} key typed key
 */
function logEntries(key) {
  const reset = key && key !== "Enter";
  const input = document.getElementById("input").value.trim();
  const ignore = document.getElementById("ignore")?.value.trim() ?? "";
  const filters = document.querySelectorAll("input.filter");
  // the checked files are the scope of a search; without a search term, and without a
  // selection, the opened file is what is listed
  const checked = [ ...document.querySelectorAll("#list input[name=name]:checked") ].
    map(input => input.value);
  const dates = input && checked.length ? checked :
    [ document.getElementById("date").value ];
  // the filter fields belong to the rendered table, so they are missing until the first
  // result arrives; empty ones must not count, or the first key press after a search
  // would look like a new search and would jump back to page 1
  const state = JSON.stringify([ input, ignore, dates,
    [ ...filters ].map(f => [ f.name, f.value.trim() ]).filter(([ , value ]) => value) ]);
  if(reset && _logInput === state) return false;
  _logInput = state;

  // a range of files is not reloaded every second: the live refresh follows a single file
  _logFiles = dates.length;
  const live = document.getElementById("live");
  if(live) live.disabled = _logFiles > 1;
  // what is searched is stated where the date of a single file is
  const heading = document.querySelector(".logbar h3");
  if(heading) heading.textContent = _logFiles > 1 ? `${_logFiles} files` : dates[0];

  const message = {
    type: "entries",
    input: input,
    ignore: ignore,
    dates: dates,
    sort: document.getElementById("sort").value,
    page: reset ? 1 : Number(document.getElementById("page").value) || 1,
    time: document.getElementById("time").value,
    filters: {}
  };
  for(const filter of filters) {
    const value = filter.value.trim();
    if(value) message.filters[filter.name.replace(/^f-/, "")] = value;
  }
  // the server stops a search that is superseded by a newer one
  message.run = startRequest();
  sendMessage("/logs", message).then(sent => {
    // a search that takes longer than a moment says so; the message is revoked by showMessage
    // when the entries arrive
    if(sent) awaitResult(message.run);
  });

  // refresh browser history, so that a reload shows what the page shows
  let href = replaceParam(window.location.href, "input", input);
  for(const filter of filters) href = replaceParam(href, filter.name, filter.value.trim());
  href = replaceParam(href, "page", message.page);
  href = replaceParam(href, "sort", message.sort);
  window.history.replaceState(null, "", href);
}

/**
 * Follows a sort or page link of the log table: the entries are requested via the open
 * connection instead of reloading the page.
 * @param {HTMLAnchorElement} link clicked link
 */
function logLink(link) {
  const params = new URLSearchParams(link.getAttribute("href").replace(/^\?/, ""));
  if(params.has("sort")) document.getElementById("sort").value = params.get("sort");
  // a link that does not name a page refers to the first one (a new sort order)
  document.getElementById("page").value = params.get("page") ?? 1;
  logEntries();
}

/**
 * Shows the log entries that were pushed by the server.
 * @param {string} text HTML table
 */
function showLogEntries(text) {
  setText("", "");
  // preserve focus and caret of a filter field across the table refresh
  const active = document.activeElement;
  const focused = active?.matches("input.filter") && active;
  const output = document.getElementById("output");
  output.innerHTML = text;
  markTruncated(output);
  // the next reload follows the result, so a search that takes longer than the interval
  // cannot queue up further ones
  clearTimeout(_live);
  if(liveOn() && _logFiles === 1) _live = setTimeout(() => logEntries(), REFRESH_INTERVAL);
  const e = document.getElementById(window.location.hash.replace(/^#/, ""));
  if(e) e.scrollIntoView();
  if(focused) {
    const filter = document.querySelector(`input.filter[name="${focused.name}"]`);
    if(filter) {
      filter.value = focused.value;
      filter.focus();
      filter.setSelectionRange(focused.selectionStart, focused.selectionEnd);
    }
  }
}

/**
 * Persists the log ignore filter and refreshes the entries.
 * @param {string} key typed key
 */
function ignoreLogs(key) {
  localStorage.setItem(IGNORE_KEY, document.getElementById("ignore").value);
  return logEntries(key);
}

/**
 * Restores the persisted ignore filter, then loads the log entries.
 */
function initLogs() {
  const ignore = document.getElementById("ignore");
  if(ignore) ignore.value = localStorage.getItem(IGNORE_KEY) ?? "";
  // the checked files are the scope of the search, so a new selection is a new search.
  // Clicks, not changes: the checkbox of the table header ticks the rows by script, which
  // raises no change event of its own
  document.getElementById("list").addEventListener("click", event => {
    if(event.target.matches("input[type=checkbox]")) logEntries("select");
  });
  // sort and page links belong to the rendered table, so they are caught here
  document.getElementById("output").addEventListener("click", event => {
    const link = event.target.closest("a[href^='?']");
    if(link) {
      event.preventDefault();
      logLink(link);
    }
  });
  return logEntries();
}

/**
 * Filters log files.
 */
function logFilter() {
  const value = document.getElementById("log-filter").value;
  const list = document.getElementById("dates");
  let count = 0, unchecked = false;
  for(const input of list.querySelectorAll("input[name=name]")) {
    const visible = !value || input.value.startsWith(value);
    input.closest("tr").style.display = visible ? null : "none";
    if(visible) {
      count++;
    } else if(input.checked) {
      // a file that is filtered out is no longer searched
      input.checked = false;
      unchecked = true;
    }
  }
  // the summary counts what is left; the buttons are derived from the selection by buttons()
  const summary = list.querySelector("h3");
  if(summary) summary.textContent = `${count} ${count === 1 ? "Entry" : "Entries"}`;
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
  document.getElementById("page").value = 1;
  document.getElementById("time").value = "";
  document.querySelector(".logbar h3").textContent = date;
  for(const link of document.querySelectorAll("#list a[data-select]")) {
    link.classList.toggle("selected", link.dataset.select === date);
  }
  // the file is part of the address, so a reload shows what the page shows; the fragment
  // named an entry of the previous file
  const url = new URL(replaceParam(window.location.href, "name", date));
  url.hash = "";
  window.history.replaceState(null, "", url);
  logEntries();
}

/** The log view receives its entries as a rendered table. */
_handlers["/logs"] = json => {
  if(json.type === "result") showLogEntries(json.result);
};
_live_actions.logs = logEntries;
