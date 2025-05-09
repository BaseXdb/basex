package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Pragma for database options.
 *
 * @author BaseX Team, BSD License
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
      throw BASEX_OPTIONS_X_X.get(info, option.name(), value);
    }
    return old;
  }

  @Override
  void finish(final QueryContext qc, final Object state) {
    qc.context.options.put(option, state);
  }

  @Override
  public boolean has(final Flag... flags) {
    return false;
  }

  @Override
  public void accept(final ASTVisitor visitor) {
  }

  @Override
  public Pragma copy() {
    return new DBPragma(name, option, value);
  }

  @Override
  public boolean simplify() {
    return true;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final DBPragma db && option.equals(db.option) &&
        super.equals(obj);
  }
}
