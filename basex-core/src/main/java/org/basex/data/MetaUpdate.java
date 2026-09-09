package org.basex.data;

/**
 * Accuracy of the database metadata after an update operation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum MetaUpdate {
  /** All metadata remains exact. */
  EXACT,
  /** Statistics counts remain exact; value statistics do not. */
  COUNTS,
  /** Path and name indexes remain complete; statistics do not. */
  COMPLETE,
  /** No metadata remains accurate. */
  NONE
}
