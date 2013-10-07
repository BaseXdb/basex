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
public final class DBOptimize extends DBNew {
  /** Flag to optimize all database structures. */
  private boolean all;

  /**
   * Constructor.
   * @param dt data
   * @param ctx database context
   * @param al optimize all database structures flag
   * @param opts database options
   * @param ii input info
   * @throws QueryException query exception
   */
  public DBOptimize(final Data dt, final QueryContext ctx, final boolean al,
      final Options opts, final InputInfo ii) throws QueryException {

    super(TYPE.DBOPTIMIZE, dt, ctx, ii);
    all = al;
    options = opts.free();
    check(false);
  }

  @Override
  public void merge(final BasicOperation o) {
    all |= ((DBOptimize) o).all;
  }

  @Override
  public void prepare(final MemData tmp) throws QueryException { }

  @Override
  public void apply() throws QueryException {
    final MetaData meta = data.meta;
    final MainOptions opts = meta.options;

    nprops.put(MainOptions.TEXTINDEX, meta.createtext);
    nprops.put(MainOptions.ATTRINDEX, meta.createattr);
    nprops.put(MainOptions.FTINDEX,   meta.createftxt);
    initOptions();
    assignOptions();

    final boolean rebuild = opts.number(MainOptions.MAXCATS) != meta.maxcats ||
        opts.number(MainOptions.MAXLEN) != meta.maxlen;
    meta.maxcats = opts.number(MainOptions.MAXCATS);
    meta.maxlen  = opts.number(MainOptions.MAXLEN);
    meta.createtext = opts.bool(MainOptions.TEXTINDEX);
    meta.createattr = opts.bool(MainOptions.ATTRINDEX);
    meta.createftxt = opts.bool(MainOptions.FTINDEX);

    try {
      if(all) OptimizeAll.optimizeAll(data, qc.context, null);
      else Optimize.optimize(data, rebuild, null);
    } catch(final IOException ex) {
      UPDBOPTERR.thrw(info, ex);
    } finally {
      resetOptions();
    }

    // remove old database reference
    if(all) qc.resource.removeData(data.meta.name);
  }

  @Override
  public int size() {
    return 1;
  }
}
