package org.basex.query.up.primitives.db;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;

/**
 * Update primitive for the {@link Function#_DB_FLUSH} function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DBFlush extends DBUpdate {
  /** Autoflush option. */
  private final boolean autoflush;

  /**
   * Constructor.
   * @param data data
   * @param qc query context
   * @param info input info
   */
  public DBFlush(final Data data, final QueryContext qc, final InputInfo info) {
    super(UpdateType.DBFLUSH, data, info);
    autoflush = qc.context.options.get(MainOptions.AUTOFLUSH);
  }

  @Override
  public void prepare() {
  }

  @Override
  public void apply() {
    if(!autoflush) data.flush(true);
  }

  @Override
  public void merge(final Update update) {
  }

  @Override
  public int size() {
    return 1;
  }
}
