package org.basex.api.xqj;

/**
 * Java XQuery API - Texts.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public interface BXQText {
  /** Java XQuery API version. */
  String VERSION = "1.0";
  /** Temporary database instance (skip?). */
  String TMP = "tmp";

  /** Error Message. */
  String CLOSED = " has been closed.";
  /** Error Message. */
  String NULL = "Specified % is null.";
  /** Error Message. */
  String WRONG = "Wrong data type; % expected, % found";
  /** Error Message. */
  String SQL = "SQL sources not supported.";
  /** Error Message. */
  String PROPS = "No property support.";
  /** Error Message. */
  String NODE = "Current item is not a node.";
  /** Error Message. */
  String ATTR = "Cannot serialize top-level attributes.";
  /** Error Message. */
  String NUM = "Number is no integer.";
  /** Error Message. */
  String ELMATT = "Node must be element, attribute or atomic.";
  /** Error Message. */
  String ELMATT1 = "Node must be element, attribute or atomic.";
  /** Error Message. */
  String ATT = "Node must not be UNTYPED or ANYTYPE.";
  /** Error Message. */
  String ELM = "Node must not be ELEMENT.";
  /** Error Message. */
  String PI = "Node must be a processing instruction.";
  /** Error Message. */
  String NOBASE = "Item has no base type.";
  /** Error Message. */
  String TWICE = "Current item was already requested.";
  /** Error Message. */
  String FORWARD = "Sequence is forwards-only";
  /** Error Message. */
  String CURSOR = "Cursor does not point to an item.";
  /** Error Message. */
  String OCC = "Occurrence indicator and item type do not match.";
  /** Error Message. */
  String OCCINV = "Invalid occurrence indicator.";
  /** Error Message. */
  String PRE = "Unknown namespace prefix '%'.";
  /** Error Message. */
  String ARG = "Wrong argument for %.";
  /** Error Message. */
  String ARGC = "Construction Mode";
  /** Error Message. */
  String ARGO = "Ordering Mode";
  /** Error Message. */
  String ARGS = "Boundary Space";
  /** Error Message. */
  String ARGN = "Namespace Mode";
  /** Error Message. */
  String ARGB = "Binding Mode";
  /** Error Message. */
  String ARGH = "Holdability";
  /** Error Message. */
  String ARGR = "Scrollability";
  /** Error Message. */
  String ARGL = "Language Type";
  /** Error Message. */
  String TIME = "Timeout must be positive.";
  /** Error Message. */
  String TIMEOUT = "Query exceeded timeout.";
  /** Error Message. */
  String VAR = "Unknown variable %.";
  
}
