package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.expr.Expr.Flag;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Pragma for database options.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class BaseXPragma extends Pragma {
  /**
   * Constructor.
   * @param name name of pragma
   * @param value value
   */
  public BaseXPragma(final QNm name, final byte[] value) {
    super(name, value);
  }

  @Override
  Object init(final QueryContext qc, final InputInfo info) {
    return null;
  }

  @Override
  void finish(final QueryContext qc, final Object cache) {
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.NDT && Token.eq(name.local(), Token.token(QueryText.NON_DETERMNISTIC));
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof BaseXPragma && super.equals(obj);
  }

  @Override
  public Pragma copy() {
    return new BaseXPragma(name, value);
  }
}
