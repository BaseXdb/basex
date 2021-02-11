package org.basex.query;

import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Query focus: context value, position, size.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class QueryFocus {
  /** Context value. */
  public Value value;
  /** Context position. */
  public long pos = 1;
  /** Context size. */
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

  @Override
  public String toString() {
    return Util.className(this) + '[' + value + ": " + pos + '/' + size + " ]";
  }
}
