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
    final Iter iter = qc.iter(exprs[0]);
    final long pos = Math.max(1, toLong(exprs[1], qc));
    final Iter ins = qc.iter(exprs[2]);

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
    final Value val = qc.value(exprs[0]);
    final long pos = toLong(exprs[1], qc);
    final Value sub = qc.value(exprs[2]);

    // prepend, append or insert new value
    final long vs = val.size(), p = Math.min(Math.max(0, pos - 1), vs);
    if(p == 0)  return ValueBuilder.concat(sub, val);
    if(p == vs) return ValueBuilder.concat(val, sub);
    return ((Seq) val).insertBefore(p, sub);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    if(exprs[2].isEmpty()) return exprs[0];
    seqType = exprs[0].seqType().add(exprs[2].seqType());
    return this;
  }
}
