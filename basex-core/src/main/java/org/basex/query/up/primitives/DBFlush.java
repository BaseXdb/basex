package org.basex.query.up.primitives;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_FLUSH} function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class DBFlush extends BasicOperation {
  /**
   * Constructor.
   * @param d data
   * @param ii input info
   */
  public DBFlush(final Data d, final InputInfo ii) {
    super(TYPE.DBFLUSH, d, ii);
  }

  @Override
  public void merge(final BasicOperation o) { }

  @Override
  public void apply() {
    final MainOptions opts = data.meta.options;
    if(opts.get(MainOptions.AUTOFLUSH)) return;
    opts.set(MainOptions.AUTOFLUSH, true);
    data.finishUpdate();
    opts.set(MainOptions.AUTOFLUSH, false);
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public void prepare(final MemData tmp) throws QueryException { }
}
