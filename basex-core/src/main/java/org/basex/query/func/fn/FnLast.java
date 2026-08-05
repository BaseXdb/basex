package org.basex.query.func.fn;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnLast extends StandardFunc {
  @Override
  protected Itr item(final QueryContext qc) throws QueryException {
    ctxValue(qc);
    return Itr.get(qc.focus.size);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(Locking.CONTEXT, false) && super.accept(visitor);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final long size = cc.qc.focus.size;
    // last() → INTEGER  (statically known context size)
    return size > 1 ? Itr.get(size) : this;
  }
}
