package org.basex.api.xqj;

/**
 * Java XQuery API - Texts.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
interface BXQText {
  /** Java XQuery API version. */
  String VERSION = "1.0";

  /** Connection property. */
  String USER = "user";
  /** Connection property. */
  String PASSWORD = "password";
  /** Connection property. */
  String SERVERNAME = "serverName";
  /** Connection property. */
  String PORT = "port";

  /** Error message. */
  String CLOSED = " has been closed.";
  /** Error message. */
  String NULL = "% argument must not be null.";
  /** Error message. */
  String WRONG = "Wrong data type; % expected, % found.";
  /** Error message. */
  String TRANS = "No manual transaction support.";
  /** Error message. */
  String ATOM = "Atomic value expected.";
  /** Error message. */
  String CONV = "No mapping available for '%'.";
  /** Error message. */
  String SQL = "SQL sources not supported.";
  /** Error message. */
  String PROPS = "Unknown property '%'.";
  /** Error message. */
  String NODE = "Current item is not a node.";
  /** Error message. */
  String ATTR = "Cannot serialize top-level attributes.";
  /** Error message. */
  String NUM = "Number '%' is no integer.";
  /** Error message. */
  String TYPE = "Item has wrong type.";
  /** Error message. */
  String ATT = "Node must not be UNTYPED or ANYTYPE.";
  /** Error message. */
  String ELM = "Node must not be ELEMENT.";
  /** Error message. */
  String PI = "Node must be a processing instruction.";
    /** Error message. */
  String NOBASE = "Item has no base type.";
  /** Error message. */
  String TWICE = "Current item has already been requested.";
  /** Error message. */
  String FORWARD = "Sequence is forwards-only";
  /** Error message. */
  String CURSOR = "Cursor does not point to an item.";
  /** Error message. */
  String OCC = "Occurrence indicator and item type do not match.";
  /** Error message. */
  String OCCINV = "Invalid occurrence indicator.";
  /** Error message. */
  String PRE = "Unknown namespace prefix '%'.";
  /** Error message. */
  String DENIED = "Access denied for user '%'.";
  /** Error message. */
  String ARG = "Wrong argument for %.";
  /** Error message. */
  String ARGC = "Construction Mode";
  /** Error message. */
  String ARGO = "Ordering Mode";
  /** Error message. */
  String ARGS = "Boundary Space";
  /** Error message. */
  String ARGN = "Namespace Mode";
  /** Error message. */
  String ARGB = "Binding Mode";
  /** Error message. */
  String ARGH = "Holdability";
  /** Error message. */
  String ARGR = "Scrollability";
  /** Error message. */
  String ARGL = "Language Type";
  /** Error message. */
  String TIME = "Timeout must be positive.";
  /** Error message. */
  String TIMEOUT = "Query exceeded timeout.";
  /** Error message. */
  String VAR = "Unknown variable %.";
}
