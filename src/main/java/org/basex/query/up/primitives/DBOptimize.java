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
    final Prop prop = qc.context.prop;
    final int ns = N_OPT.length;
    final int[] onums = new int[ns];
    for(int o = 0; o < ns; o++) onums[o] = prop.num(N_OPT[o]);
    final int bs = B_OPT.length;
    final boolean[] obools = new boolean[bs];
    for(int o = 0; o < bs; o++) obools[o] = prop.is(B_OPT[o]);
    final int ss = S_OPT.length;
    final String[] ostrs = new String[ss];
    for(int o = 0; o < ss; o++) ostrs[o] = prop.get(S_OPT[o]);

    assignOptions();
    final MetaData meta = data.meta;
    final boolean rebuild = prop.num(Prop.MAXCATS) != meta.maxcats ||
        prop.num(Prop.MAXLEN) != meta.maxlen;
    meta.maxcats = prop.num(Prop.MAXCATS);
    meta.maxlen  = prop.num(Prop.MAXLEN);
    meta.createtext = prop.is(Prop.TEXTINDEX);
    meta.createattr = prop.is(Prop.ATTRINDEX);
    meta.createftxt = prop.is(Prop.FTINDEX);

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
