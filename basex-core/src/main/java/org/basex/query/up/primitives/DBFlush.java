package org.basex.query.up.primitives;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_FLUSH} function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class DBFlush extends DBUpdate {
  /** Autoflush option. */
  private final boolean autoflush;

  /**
   * Constructor.
   * @param data data
   * @param info input info
   * @param qc query context
   */
  public DBFlush(final Data data, final InputInfo info, final QueryContext qc) {
    super(UpdateType.DBFLUSH, data, info);
    autoflush = qc.context.options.get(MainOptions.AUTOFLUSH);
  }

  @Override
  public void merge(final Update update) { }

  @Override
  public void apply() {
    if(!autoflush) data.flush(true);
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public void prepare(final MemData tmp) { }
}
