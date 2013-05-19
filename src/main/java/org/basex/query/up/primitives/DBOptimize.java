package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.io.*;

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
public final class DBOptimize extends BasicOperation {
  /** Query context. */
  private final QueryContext qc;
  /** Flag to optimize all database structures. */
  private boolean all;

  /**
   * Constructor.
   * @param d data
   * @param c database context
   * @param a optimize all database structures flag
   * @param ii input info
   */
  public DBOptimize(final Data d, final QueryContext c, final boolean a,
      final InputInfo ii) {
    super(TYPE.DBOPTIMIZE, d, ii);
    qc = c;
    all = a;
  }

  @Override
  public void merge(final BasicOperation o) {
    all |= ((DBOptimize) o).all;
  }

  @Override
  public void apply() throws QueryException {
    try {
      if(all) OptimizeAll.optimizeAll(data, qc.context, null);
      else Optimize.optimize(data, null);
    } catch(final IOException ex) {
      UPDBOPTERR.thrw(info, ex);
    }

    // remove old database reference
    if(all) qc.resource.removeData(data.meta.name);
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public void prepare(final MemData tmp) throws QueryException { }
}
