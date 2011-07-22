package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import org.basex.core.Context;
import org.basex.core.cmd.OptimizeAll;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Update primitive for the optimize function.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 */
public final class Optimize extends UpdatePrimitive {
  /** Database context. */
  private final Context ctx;
  /** Flag to optimize all database structures. */
  private boolean all;

  /**
   * Constructor.
   * @param d data
   * @param c database context
   * @param a optimize all database structures flag
   * @param info input info
   */
  public Optimize(final Data d, final Context c, final boolean a,
      final InputInfo info) {
    super(PrimitiveType.OPTIMIZE, -1, d, info);
    ctx = c;
    all = a;
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    if(p instanceof Optimize) {
      final Optimize o = (Optimize) p;
      if(o.all) all = o.all;
    } else {
      Util.notexpected(p);
    }
  }

  @Override
  public void apply() throws QueryException {
    try {
      if(all) OptimizeAll.optimizeAll(data, ctx, null);
      else org.basex.core.cmd.Optimize.optimize(data, data.meta.prop);
    } catch(final Exception ex) {
      DBERR.thrw(input, ex);
    }
  }
}
