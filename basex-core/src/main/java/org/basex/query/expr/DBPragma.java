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
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class DBPragma extends Pragma {
  /** Option key. */
  private final Option<?> option;

  /**
   * Constructor.
   * @param name name of pragma
   * @param option option
   * @param value value
   */
  public DBPragma(final QNm name, final Option<?> option, final byte[] value) {
    super(name, value);
    this.option = option;
  }

  @Override
  Object init(final QueryContext qc, final InputInfo info) throws QueryException {
    final Object old = qc.context.options.get(option);
    try {
      qc.context.options.assign(option.name(), string(value));
    } catch(final BaseXException ex) {
      Util.debug(ex);
      throw BASX_VALUE_X_X.get(info, option.name(), value);
    }
    return old;
  }

  @Override
  void finish(final QueryContext qc, final Object cache) {
    qc.context.options.put(option, cache);
  }

  @Override
  public boolean has(final Flag flag) {
    return false;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof DBPragma && option.equals(((DBPragma) obj).option) &&
        super.equals(obj);
  }

  @Override
  public Pragma copy() {
    return new DBPragma(name, option, value);
  }
}
