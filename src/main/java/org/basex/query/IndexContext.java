package org.basex.query;

import org.basex.data.Data;
import org.basex.query.path.Step;

/**
 * Container for all information needed to determine whether an index is
 * accessible or not.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Sebastian Gath
 */
public final class IndexContext {
  /** Query context. */
  public final QueryContext ctx;
  /** Data reference. */
  public final Data data;
  /** Index Step. */
  public final Step step;
  /** Flag for potential duplicates. */
  public final boolean dupl;

  /** Number of estimated results. */
  public int is = Integer.MAX_VALUE;
  /** Flag for ftnot expressions. */
  public boolean not;
  /** Flag for sequential processing. */
  public boolean seq;

  /**
   * Constructor.
   * @param c query context
   * @param d data reference
   * @param s index step
   * @param l duplicate flag
   */
  public IndexContext(final QueryContext c, final Data d, final Step s,
      final boolean l) {
    ctx = c;
    data = d;
    step = s;
    dupl = l;
  }
}
