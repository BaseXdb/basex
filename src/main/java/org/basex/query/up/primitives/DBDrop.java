package org.basex.query.up.primitives;

import org.basex.core.Text;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/* [LK]
 * ADMIN permissions (checked in FNDb)
 * check if db exists - FNDb.drop
 * pinning - ContextModifier.apply
 * close db - ?
 * drop in apply
 * error - db not dropped?
 * multiple DBDrop on same target allowed
 */

/**
 * Update primitive for the {@link Function#_DB_DROP} function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public class DBDrop extends BasicOperation {
  /** Query Context. */
  private final QueryContext ctx;

  /**
   * Constructor.
   * @param d target data reference
   * @param ii input info
   * @param c query context
   */
  public DBDrop(final Data d, final InputInfo ii, final QueryContext c) {
    super(TYPE.DBDROP, d, ii);
    ctx = c;
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public void merge(final BasicOperation o) throws QueryException { }

  @Override
  public void apply() throws QueryException {
    if(!DropDB.drop(data.meta.name, ctx.context))
      Util.err(Text.DB_NOT_DROPPED_X, data.meta.name);
  }

  @Override
  public void prepare() throws QueryException { }
}
