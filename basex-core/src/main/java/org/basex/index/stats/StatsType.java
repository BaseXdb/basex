package org.basex.index.stats;

import java.util.*;

/**
 * Value types, used for index statistics and query optimizations.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public enum StatsType {
  // Don't change order (new items can be attached, though)

  /** Values are arbitrary strings. */
  STRING,
  /** A limited number of distinct strings exists. */
  CATEGORY,
  /** All values are of type integer.  */
  INTEGER,
  /** All values are of type double. */
  DOUBLE,
  /** No values exist. */
  NONE;

  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH);
  }
}
