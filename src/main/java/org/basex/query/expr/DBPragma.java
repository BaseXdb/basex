package org.basex.query.expr;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Pragma for database options.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class DBPragma extends Pragma {
  /** Option key. */
  private final String key;
  /** Cached value. */
  private Object old;

  /**
   * Constructor.
   * @param n name of pragma
   * @param v optional value
   */
  public DBPragma(final QNm n, final byte[] v) {
   super(n, v);
    key = string(uc(n.local()));
  }

  @Override
  void init(final QueryContext ctx, final InputInfo info) throws QueryException {
    old = ctx.context.prop.get(key);
    try {
      ctx.context.prop.set(key, string(value));
    } catch(final Exception ex) {
      BASX_VALUE.thrw(info, ex.getMessage());
    }
  }

  @Override
  void finish(final QueryContext ctx) {
    ctx.context.prop.setObject(key, old);
  }

  @Override
  public Pragma copy() {
    return new DBPragma(name, value);
  }
}
