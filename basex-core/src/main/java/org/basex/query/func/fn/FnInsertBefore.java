package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnInsertBefore extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final long pos = Math.max(1, toLong(exprs[1], qc));
    final Iter ins = exprs[2].iter(qc);

    return new Iter() {
      long p = pos;
      boolean last;

      @Override
      public Item next() throws QueryException {
        while(!last) {
          final boolean sub = p == 0 || --p == 0;
          final Item i = (sub ? ins : iter).next();
          if(i != null) return i;
          if(sub) --p;
          else last = true;
        }
        return p > 0 ? ins.next() : null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value val = exprs[0].value(qc);
    final long pos = toLong(exprs[1], qc);
    final Value sub = exprs[2].value(qc);

    // prepend, append or insert new value
    final long vs = val.size(), ps = Math.min(Math.max(0, pos - 1), vs);
    if(ps == 0)  return ValueBuilder.concat(sub, val);
    if(ps == vs) return ValueBuilder.concat(val, sub);
    return ((Seq) val).insertBefore(ps, sub);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr ex1 = exprs[0], ex2 = exprs[1], ex3 = exprs[2];
    if(ex2.seqType().oneNoArray()) {
      if(ex1 == Empty.SEQ) return ex3;
      if(ex3 == Empty.SEQ) return ex1;
    }
    exprType.assign(ex1.seqType().add(ex3.seqType()));
    return this;
  }
}
