/** Settings view: searching the options. */

/**
 * Shows the entries that contain all words of the search input, and whose cells contain the
 * column filters.
 */
function filterSettings() {
  storeField("input");
  const panel = document.getElementById("input").closest(".panel");
  // as in the logs, every word must occur somewhere in the entry
  const words = fieldValue("input").toLowerCase().split(/\s+/).filter(Boolean);
  const filters = [ ...panel.querySelectorAll("input.filter") ].
    map(input => input.value.trim().toLowerCase());
  filterRows(panel, panel.querySelectorAll("tbody tr"), row => {
    const cells = [ ...row.cells ].map(cell => cell.textContent.toLowerCase());
    const text = cells.join(" ");
    return words.every(word => text.includes(word)) &&
      filters.every((filter, c) => cells[c].includes(filter));
  });
  markTruncated(panel);
}

/**
 * Restores the search input, and applies it.
 */
function initSettings() {
  if(restoreField("input")) filterSettings();
}
