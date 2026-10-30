/** Users view: the users of the server, and what each of them may do. */

/** Path of the endpoint that serves the panels of this view. */
const USERS_WS = "/users";

/** Selected user. It is part of the address: a link reproduces what the panels show, and the
    browser history steps through the selections that were made. */
let _user = "";

/**
 * Shows another user.
 * @param {string} name user
 */
function selectUser(name) {
  if(name === _user) return;
  _user = name;
  pushParams({ name: _user });
  showUser();
}

/**
 * Adopts the selection of the address bar.
 */
function adoptSelection() {
  _user = new URLSearchParams(window.location.search).get("name") ?? "";
}

/**
 * Requests everything that the selected user supplies. The list it was chosen from is not
 * among it: the entry it points out is the only thing that changes there.
 */
function showUser() {
  mark("users-panel", _user);
  requestPanel(USERS_WS, "user-panel", { type: "user", name: _user });
}

/**
 * Opens the dialog that adds a local permission to the selected user.
 */
function addPattern() {
  document.getElementById("pattern-user").value = _user;
  showDialog("pattern");
}

/** The data of a user is edited as XML, and the panel brings a new text area with it. */
_panel_filled["user-panel"] = () => loadCodeMirror("xml", [ "editor" ]);

/** The sort links of the user list are followed in place. */
followPanelLinks({
  "users-panel": panelRequest(USERS_WS, "users-panel", () => ({ type: "users", name: _user }))
});

/**
 * Prepares the view. The panels are rendered by the server, which knows the selection from the
 * address; only what is selected later is requested over the connection.
 */
function initUsers() {
  // both data fields are edited as XML. The general one is loaded first: it outlives every
  // selection, and is therefore the editor the page-wide helpers refer to
  loadCodeMirror("xml", [ "user-info", "editor" ]);
  initSelection(adoptSelection, showUser);
  // the server opens the first user if the address names none: the address is completed
  _user ||= adoptOpened("user-panel");
}
