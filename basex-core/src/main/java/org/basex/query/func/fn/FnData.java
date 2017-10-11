package org.basex.query.func.fn;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnData extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return ctxArg(0, qc).atomIter(qc, info);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return ctxArg(0, qc).atomValue(qc, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Value v = cc.qc.focus.value;
    final Expr expr = exprs.length > 0 ? exprs[0] : v != null ? v : this;
    if(expr != this) {
      final SeqType st = expr.seqType();
      if(st.type instanceof NodeType) {
        seqType = SeqType.get(AtomType.ATM, st.occ);
      } else if(!st.mayBeArray()) {
        seqType = st;
      }
      size = expr.size();
    }
    return this;
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CTX && exprs.length == 0 || super.has(flag);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return (exprs.length != 0 || visitor.lock(Locking.CONTEXT)) && super.accept(visitor);
  }
}
