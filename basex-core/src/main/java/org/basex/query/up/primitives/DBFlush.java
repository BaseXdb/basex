package org.basex.query.up.primitives;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_FLUSH} function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DBFlush extends DBUpdate {
  /**
   * Constructor.
   * @param data data
   * @param info input info
   */
  public DBFlush(final Data data, final InputInfo info) {
    super(UpdateType.DBFLUSH, data, info);
  }

  @Override
  public void merge(final Update up) { }

  @Override
  public void apply() {
    if(!data.meta.options.get(MainOptions.AUTOFLUSH)) data.flush(true);
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public void prepare(final MemData tmp) { }
}
