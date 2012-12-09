package org.basex.query.up.primitives;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_FLUSH} function.
 *
 * @author BaseX Team 2005-12, BSD License
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
    final Prop prop = data.meta.prop;
    if(prop.is(Prop.AUTOFLUSH)) return;
    prop.set(Prop.AUTOFLUSH, true);
    data.finishUpdate();
    prop.set(Prop.AUTOFLUSH, false);
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public void prepare(final MemData tmp) throws QueryException { }
}
