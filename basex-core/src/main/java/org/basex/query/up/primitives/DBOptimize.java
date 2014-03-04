package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.options.*;

/**
 * Update primitive for the optimize function.
 *
 * @author BaseX Team 2005-14, BSD License
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
  public void prepare(final MemData tmp) { }

  @Override
  public void apply() throws QueryException {
    final MetaData meta = data.meta;
    final MainOptions opts = meta.options;

    nprops.put(MainOptions.TEXTINDEX, meta.createtext);
    nprops.put(MainOptions.ATTRINDEX, meta.createattr);
    nprops.put(MainOptions.FTINDEX,   meta.createftxt);
    initOptions();
    assignOptions();

    meta.createtext = opts.get(MainOptions.TEXTINDEX);
    meta.createattr = opts.get(MainOptions.ATTRINDEX);
    meta.createftxt = opts.get(MainOptions.FTINDEX);

    final int mc = opts.get(MainOptions.MAXCATS);
    final int ml = opts.get(MainOptions.MAXLEN);
    final boolean rebuild = mc != meta.maxcats || ml != meta.maxlen;

    final boolean st = opts.get(MainOptions.STEMMING);
    final boolean cs = opts.get(MainOptions.CASESENS);
    final boolean dc = opts.get(MainOptions.DIACRITICS);
    final String sw = opts.get(MainOptions.STOPWORDS);
    final Language ln = Language.get(opts);
    final boolean rebuildFT = rebuild || !ln.equals(meta.language) || st != meta.stemming ||
        cs != meta.casesens ||  dc != meta.diacritics || !sw.equals(meta.stopwords);

    meta.language   = ln;
    meta.stemming   = st;
    meta.casesens   = cs;
    meta.diacritics = dc;
    meta.stopwords  = sw;
    meta.maxcats    = mc;
    meta.maxlen     = ml;

    try {
      if(all) OptimizeAll.optimizeAll(data, qc.context, null);
      else Optimize.optimize(data, rebuild, rebuildFT, null);
    } catch(final IOException ex) {
      throw UPDBOPTERR.get(info, ex);
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
