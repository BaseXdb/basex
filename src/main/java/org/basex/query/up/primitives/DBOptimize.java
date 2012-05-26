package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Update primitive for the optimize function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public final class DBOptimize extends UpdatePrimitive {
  /** Database context. */
  private final Context ctx;
  /** Flag to optimize all database structures. */
  private boolean all;

  /**
   * Constructor.
   * @param d data
   * @param c database context
   * @param a optimize all database structures flag
   * @param ii input info
   */
  public DBOptimize(final Data d, final Context c, final boolean a, final InputInfo ii) {
    super(PrimitiveType.DBOPTIMIZE, -1, d, ii);
    ctx = c;
    all = a;
  }

  @Override
  public void merge(final UpdatePrimitive p) {
    final DBOptimize o = (DBOptimize) p;
    if(o.all) all = o.all;
  }

  @Override
  public void apply() throws QueryException {
    try {
      if(all) OptimizeAll.optimizeAll(data, ctx, null);
      else Optimize.optimize(data, null);
    } catch(final IOException ex) {
      UPDBOPTERR.thrw(info, ex);
    }
  }

  @Override
  public int size() {
    return 1;
  }
}
