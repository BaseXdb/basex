/** Details of a job: a dialog that is opened over the activity view or the workspace. */

/** Requests the details of the shown job; a view with panels asks for them along with those. */
let _requestJob = requestJob;
/** Handle of the timer that asks for the details of a running job again. */
let _jobTimer;

/**
 * Returns the job that the address names.
 * @returns {string} job id (can be null)
 */
function jobParam() {
  return new URLSearchParams(window.location.search).get("job");
}

/**
 * Requests the details of the job that the address names, unless they are final.
 */
function requestJob() {
  const job = jobParam();
  const done = document.getElementById("job-details")?.dataset.done === "true";
  if(job && !done) sendMessage("/activity", { type: "panels", open: [], job });
}

/**
 * Asks for the details of the shown job again after a while.
 */
function watchJob() {
  clearTimeout(_jobTimer);
  if(document.getElementById("details-dialog").open) {
    _jobTimer = setTimeout(requestJob, REFRESH_INTERVAL);
  }
}

/** A view without panels asks for the details alone, and again until they are final. */
_handlers["/activity"] = json => {
  showJobDetails(json);
  watchJob();
};

/**
 * Shows the details of a job that were pushed by the server. A running job's details are
 * updated; the final ones are applied once, together with the editor for its result, and are
 * then left alone.
 * @param {object} json message with the details
 */
function showJobDetails(json) {
  const details = document.getElementById("job-details");
  if(!details || !json.job || details.dataset.done === "true") return;
  const template = document.createElement("template");
  template.innerHTML = json.job;
  const facts = details.querySelector(".job-facts");
  const update = template.content.querySelector(".job-facts");
  if(facts && update && !json.done) {
    // the string of a running job does not change: only its facts are replaced, and the
    // editor of the string is kept
    facts.replaceWith(update);
  } else {
    details.replaceChildren(template.content);
    loadJobEditors();
  }
  details.dataset.done = json.done;
  buttons();
  markTruncated(details);
}

/**
 * Turns the texts of the details into editors. They belong to the dialog, not to the page:
 * the query of a service is edited, a job string and a result are only highlighted.
 */
function loadJobEditors() {
  if(!codeMirrorUsed()) return;
  for(const [ id, language ] of [ [ "job-string", "xquery" ], [ "job-output", "xml" ] ]) {
    const area = document.getElementById(id);
    if(area) CM6.fromTextArea(area, { language, readOnly: area.readOnly });
  }
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
 * details are filled in by the request, which is sent at once.
 */
function showJob() {
  const dialog = document.getElementById("details-dialog");
  const job = jobParam();
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
  _requestJob();
}

/**
 * Opens the details of the job that the address names; the editors in them are measured when
 * they are shown, so they are loaded afterwards.
 */
function initJobDetails() {
  const dialog = document.getElementById("details-dialog");
  if(jobParam()) dialog.showModal();
  loadJobEditors();
  // a closed job is no longer asked for, and a reload does not open it again
  dialog.addEventListener("close", () => hideParams("job"));
  // the back button steps through the jobs that were opened
  initSelection(() => {}, showJob);

  // the download of a result that the view has already given up; the browser keeps the page
  const download = document.getElementById("download-form");
  if(download) {
    download.submit();
    // a reload must not ask for a result that is gone by then
    hideParams("download");
  }
}
