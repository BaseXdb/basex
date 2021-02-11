package org.basex.query.expr;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Pragma for database options.
 *
 * @author BaseX Team 2005-21, BSD License
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
  Object init(final QueryContext qc, final InputInfo ii) {
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
  public void accept(final ASTVisitor visitor) {
    if(Token.eq(name.local(), Annotation._BASEX_LOCK.local())) {
      for(final String lock : Locking.queryLocks(value)) {
        visitor.lock(lock, false);
      }
    }
  }

  @Override
  public Pragma copy() {
    return new BaseXPragma(name, value);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof BaseXPragma && super.equals(obj);
  }
}
