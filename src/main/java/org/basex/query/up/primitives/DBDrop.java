package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

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
public final class DBDrop extends BasicOperation {
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
  public void merge(final BasicOperation o) throws QueryException { }

  @Override
  public void prepare(final MemData tmp) throws QueryException { }

  @Override
  public void apply() throws QueryException {
    // trigger early removal of database locks
    data.finishUpdate();
    // close data instance in query processor
    final String name = data.meta.name;
    ctx.resource.removeData(name);
    // invalidate data instance to avoid repeated removal of locks
    data = null;
    // check if database is stilled pinned by another process
    if(ctx.context.pinned(name)) BXDB_OPENED.thrw(info, name);
    // check if database files can be safely removed
    if(!DropDB.drop(name, ctx.context)) UPDBDROP.thrw(info, name);
  }

  @Override
  public int size() {
    return 1;
  }
}
