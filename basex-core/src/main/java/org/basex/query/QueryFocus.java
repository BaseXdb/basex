package org.basex.query;

import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Query focus: context value, position, size.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryFocus {
  /** Context value (can be {@code null}). */
  public Value value;
  /** Context position (1 or greater). */
  public long pos = 1;
  /** Context size (1 or greater). */
  public long size = 1;

  /**
   * Creates a copy of this query focus.
   * @return copy
   */
  public QueryFocus copy() {
    final QueryFocus qf = new QueryFocus();
    qf.value = value;
    qf.pos = pos;
    qf.size = size;
    return qf;
  }

  /**
   * Checks if two query focuses are deep-equal.
   * @param qf1 first query focus (can be {@code null})
   * @param qf2 second query focus (can be {@code null})
   * @param deep comparator (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  public static boolean deepEqual(final QueryFocus qf1, final QueryFocus qf2, final DeepEqual deep)
      throws QueryException {
    if(qf1 == null || qf2 == null) return qf1 == qf2;
    if(qf1.pos != qf2.pos || qf1.size != qf2.size) return false;
    final Value v1 = qf1.value, v2 = qf2.value;
    return v1 == null || v2 == null ? v1 == v2 :
      deep != null ? deep.equal(v1, v2) : v1.equals(v2);
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + value + ": " + pos + '/' + size + " ]";
  }
}
