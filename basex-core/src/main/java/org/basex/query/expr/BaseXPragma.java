package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.expr.Expr.Flag;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Pragma for database options.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class BaseXPragma extends Pragma {
  /**
   * Constructor.
   * @param name name of pragma
   * @param value optional value
   */
  public BaseXPragma(final QNm name, final byte[] value) {
    super(name, value);
  }

  @Override
  void init(final QueryContext qc, final InputInfo info) throws QueryException {
  }

  @Override
  void finish(final QueryContext qc) {
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.NDT && Token.eq(name.local(), Token.token(QueryText.NON_DETERMNISTIC));
  }

  @Override
  public Pragma copy() {
    return new BaseXPragma(name, value);
  }
}
