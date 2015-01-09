package org.basex.query.func.fn;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnData extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return arg(0, qc).atomIter(qc, info);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return arg(0, qc).atomValue(qc, info);
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    if(exprs.length == 1) {
      final SeqType st = exprs[0].seqType();
      if(st.type instanceof NodeType) {
        seqType = SeqType.get(AtomType.ATM, st.occ);
      } else if(!st.mayBeArray()) {
        seqType = st;
      }
    }
    return this;
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.CTX && exprs.length == 0 || super.has(flag);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return (exprs.length != 0 || visitor.lock(DBLocking.CTX)) && super.accept(visitor);
  }
}
