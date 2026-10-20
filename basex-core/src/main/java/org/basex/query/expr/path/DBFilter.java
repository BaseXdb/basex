package org.basex.query.expr.path;

import org.basex.data.*;

/**
 * Filter for database nodes, derived from a node test.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param kind database kind of matching nodes ({@code -1}: all kinds)
 * @param nameId name ID of matching nodes ({@code 0}: all names, {@code -1}: no names)
 * @param exact indicates if the filter decides the node test
 */
public record DBFilter(int kind, int nameId, boolean exact) {
  /** Filter that accepts all nodes. */
  public static final DBFilter ALL = new DBFilter(-1, 0, true);

  /**
   * Checks if a database node may match the filter.
   * @param data data reference
   * @param pre PRE value
   * @param k database kind of the node
   * @return result of check
   */
  public boolean mayMatch(final Data data, final int pre, final int k) {
    return (kind == -1 || kind == k) && (nameId == 0 || data.nameId(pre) == nameId);
  }
}
