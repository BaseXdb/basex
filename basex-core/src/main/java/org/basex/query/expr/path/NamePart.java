package org.basex.query.expr.path;

/**
 * Part of name to be tested.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public enum NamePart {
  /** Local test (*:name). */
  LOCAL,
  /** URI test (prefix:*). */
  URI,
  /** Full test (prefix:name). */
  FULL
}
