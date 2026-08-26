/** Users view: the users of the server, and what each of them may do. */

/** Path of the endpoint that serves the panels of this view. */
const USERS_WS = "/users";

/** Selected user. It is part of the address: a link reproduces what the panels show, and the
    browser history steps through the selections that were made. */
let _user = "";

/**
 * Shows another user. What is attached to no user in particular steps back while one of them
 * is being looked at.
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
  requestPanel(USERS_WS, "permissions-panel", { type: "permissions", name: _user });
}

/**
 * Requests the users panel.
 * @param {string} sort sort key; if omitted, the shown order is kept
 */
function refreshUsers(sort) {
  requestPanel(USERS_WS, "users-panel", { type: "users", name: _user }, sort);
}

/** The data of a user is edited as XML, and the panel brings a new text area with it. The data
    that belongs to no user in particular steps back to a strip while one is being looked at;
    the panel decides, not the selection, as a user that does not exist opens nothing. */
_panel_filled["user-panel"] = () => {
  loadCodeMirror("xml", [ "editor" ]);
  foldPanels([ [ "General User Data", panelShown("User") ] ]);
};

/** The sort links of the user list are followed in place. */
followPanelLinks({ "users-panel": refreshUsers });

/**
 * Prepares the view. The panels are rendered by the server, which knows the selection from the
 * address; only what is selected later is requested over the connection.
 */
function initUsers() {
  // both data fields are edited as XML. The general one is loaded first: it outlives every
  // selection, and is therefore the editor the page-wide helpers refer to
  loadCodeMirror("xml", [ "user-info", "editor" ]);
  initSelection(adoptSelection, showUser);
}
