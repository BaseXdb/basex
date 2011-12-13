package org.basex.index;

/**
 * Content types, used for index statistics.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public enum StatsType {
  // Don't change order (new items can be attached, however)

  /** Text.     */
  TEXT,
  /** Category. */
  CATEGORY,
  /** Numeric.  */
  INTEGER,
  /** Numeric.  */
  DOUBLE,
  /** No values. */
  NONE
}
