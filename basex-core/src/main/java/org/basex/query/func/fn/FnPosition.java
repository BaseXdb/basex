package org.basex.query.func.fn;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnPosition extends StandardFunc {
  @Override
  public Int item(final QueryContext qc, final InputInfo ii) throws QueryException {
    ctxValue(qc);
    return Int.get(qc.focus.pos);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(Locking.CONTEXT) && super.accept(visitor);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // E[position()]  ->  E[true()]
    return cc.simplify(this, mode.oneOf(Simplify.PREDICATE) ? Bln.TRUE : this, mode);
  }

  @Override
  public Expr optimizePos(final OpV op, final CompileContext cc) {
    return Bln.TRUE;
  }
}
