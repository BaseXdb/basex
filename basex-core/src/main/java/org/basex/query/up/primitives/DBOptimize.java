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
public final class DBOptimize extends DBUpdate {
  /** Database update options. */
  private final DBOptions updates;
  /** Flag to optimize all database structures. */
  private boolean all;

  /**
   * Constructor.
   * @param data data
   * @param all optimize all database structures flag
   * @param opts database options
   * @param qc database context
   * @param info input info
   * @throws QueryException query exception
   */
  public DBOptimize(final Data data, final boolean all, final Options opts,
      final QueryContext qc, final InputInfo info) throws QueryException {

    super(UpdateType.DBOPTIMIZE, data, info);
    this.all = all;
    updates = new DBOptions(qc, opts.free(), info);
    updates.check(false);
  }

  @Override
  public void merge(final Update up) {
    all |= ((DBOptimize) up).all;
  }

  @Override
  public void prepare(final MemData tmp) { }

  @Override
  public void apply() throws QueryException {
    final MetaData meta = data.meta;
    final MainOptions opts = meta.options;

    updates.nprops.put(MainOptions.TEXTINDEX, meta.createtext);
    updates.nprops.put(MainOptions.ATTRINDEX, meta.createattr);
    updates.nprops.put(MainOptions.FTINDEX,   meta.createftxt);
    updates.initOptions();
    updates.assignOptions();

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
      if(all) OptimizeAll.optimizeAll(data, updates.qc.context, null);
      else Optimize.optimize(data, rebuild, rebuildFT, null);
    } catch(final IOException ex) {
      throw UPDBOPTERR.get(info, ex);
    } finally {
      updates.resetOptions();
    }

    // remove old database reference
    if(all) updates.qc.resources.remove(data.meta.name);
  }

  @Override
  public int size() {
    return 1;
  }
}
