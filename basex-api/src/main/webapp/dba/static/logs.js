/** Log view: searching and monitoring log entries. */

/** Most recent log entry search state. */
let _logInput;

/** localStorage key for the logs 'ignore entries' filter. */
const IGNORE_KEY = "dba-ignore-logs";

/**
 * Queries the entries of the current log file.
 * @param {string} key typed key
 */
function logEntries(key) {
  const reset = key && key !== "Enter";
  const input = document.getElementById("input").value.trim();
  const ignore = document.getElementById("ignore")?.value.trim() ?? "";
  const filters = document.querySelectorAll("input.filter");
  // the filter fields belong to the rendered table, so they are missing until the first
  // result arrives; empty ones must not count, or the first key press after a search
  // would look like a new search and would jump back to page 1
  const state = JSON.stringify([ input, ignore,
    [ ...filters ].map(f => [ f.name, f.value.trim() ]).filter(([ , value ]) => value) ]);
  if(reset && _logInput === state) return false;
  _logInput = state;

  const message = {
    type: "entries",
    input: input,
    ignore: ignore,
    date: document.getElementById("date").value,
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
  message.run = _pending = ++_run;
  sendMessage("/logs", message);

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
  if(liveOn()) _live = setTimeout(() => logEntries(), REFRESH_INTERVAL);
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
  let count = 0;
  for(const input of list.querySelectorAll("input[name=name]")) {
    const visible = !value || input.value.startsWith(value);
    input.closest("tr").style.display = visible ? null : "none";
    if(visible) count++;
    else input.checked = false;
  }
  // the summary counts what is left; the buttons are derived from the selection by buttons()
  const summary = list.querySelector("h3");
  if(summary) summary.textContent = `${count} ${count === 1 ? "Entry" : "Entries"}`;
  buttons();
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
