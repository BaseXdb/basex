package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.Expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Pragma for database options.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class DBPragma extends Pragma {
  /** Option key. */
  private final Option<?> option;
  /** Cached value. */
  private Object old;

  /**
   * Constructor.
   * @param name name of pragma
   * @param option option
   * @param value optional value
   */
  public DBPragma(final QNm name, final Option<?> option, final byte[] value) {
    super(name, value);
    this.option = option;
  }

  @Override
  void init(final QueryContext qc, final InputInfo info) throws QueryException {
    old = qc.context.options.get(option);
    try {
      qc.context.options.assign(option.name(), string(value));
    } catch(final BaseXException ex) {
      throw BASX_VALUE_X_X.get(info, option.name(), value);
    }
  }

  @Override
  void finish(final QueryContext qc) {
    qc.context.options.put(option, old);
  }

  @Override
  public boolean has(final Flag flag) {
    return false;
  }

  @Override
  public Pragma copy() {
    return new DBPragma(name, option, value);
  }
}
