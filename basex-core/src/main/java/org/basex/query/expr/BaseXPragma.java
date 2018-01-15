package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Pragma for database options.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Leo Woerteler
 */
public final class BaseXPragma extends Pragma {
  /** Non-deterministic flag. */
  private final boolean ndt;

  /**
   * Constructor.
   * @param name name of pragma
   * @param value value
   */
  public BaseXPragma(final QNm name, final byte[] value) {
    super(name, value);
    ndt = Token.eq(name.local(), Token.token(QueryText.NON_DETERMNISTIC));
  }

  @Override
  Object init(final QueryContext qc, final InputInfo info) {
    return null;
  }

  @Override
  void finish(final QueryContext qc, final Object state) {
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.NDT.in(flags) && ndt;
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
