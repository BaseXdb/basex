package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.hash.*;

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
   * @param map index options
   * @param ii input info
   * @throws QueryException query exception
   */
  public DBOptimize(final Data dt, final QueryContext ctx, final boolean al,
      final TokenMap map, final InputInfo ii) throws QueryException {

    super(TYPE.DBOPTIMIZE, dt, ctx, ii);
    all = al;
    options = map;
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
    final Options opts = meta.options;

    nprops.put(Options.TEXTINDEX, meta.createtext);
    nprops.put(Options.ATTRINDEX, meta.createattr);
    nprops.put(Options.FTINDEX, meta.createftxt);
    initOptions();
    assignOptions();

    final boolean rebuild = opts.num(Options.MAXCATS) != meta.maxcats ||
        opts.num(Options.MAXLEN) != meta.maxlen;
    meta.maxcats = opts.num(Options.MAXCATS);
    meta.maxlen  = opts.num(Options.MAXLEN);
    meta.createtext = opts.is(Options.TEXTINDEX);
    meta.createattr = opts.is(Options.ATTRINDEX);
    meta.createftxt = opts.is(Options.FTINDEX);

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
