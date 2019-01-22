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
 * @author BaseX Team 2005-19, BSD License
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
          final Item item = qc.next(sub ? ins : iter);
          if(item != null) return item;
          if(sub) --p;
          else last = true;
        }
        return p > 0 ? ins.next() : null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc);
    final long pos = toLong(exprs[1], qc);
    final Value sub = exprs[2].value(qc);

    // prepend, append or insert new value
    final long size = value.size(), ps = Math.min(Math.max(0, pos - 1), size);
    if(ps == 0)  return ValueBuilder.concat(sub, value, qc);
    if(ps == size) return ValueBuilder.concat(value, sub, qc);
    return ((Seq) value).insertBefore(ps, sub, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr1 = exprs[0], expr2 = exprs[1], expr3 = exprs[2];
    if(expr2.seqType().oneNoArray()) {
      if(expr1 == Empty.SEQ) return expr3;
      if(expr3 == Empty.SEQ) return expr1;
    }
    exprType.assign(expr1.seqType().add(expr3.seqType()));
    return this;
  }
}
