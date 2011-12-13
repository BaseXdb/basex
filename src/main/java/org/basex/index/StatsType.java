package org.basex.index;

/**
 * Content types, used for index statistics.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public enum StatsType {
  /** Category. */
  CATEGORY,
  /** Numeric.  */
  INTEGER,
  /** Numeric.  */
  DOUBLE,
  /** Text.     */
  TEXT,
  /** No values. */
  NONE
}
