package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class FnInsertBefore extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final long pos = Math.max(1, toLong(exprs[1], qc));
    final Iter insert = exprs[2].iter(qc);

    return new Iter() {
      long p = pos;
      boolean last;

      @Override
      public Item next() throws QueryException {
        while(!last) {
          final boolean sub = p == 0 || --p == 0;
          final Item item = qc.next(sub ? insert : iter);
          if(item != null) return item;
          if(sub) --p;
          else last = true;
        }
        return p > 0 ? insert.next() : null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc);
    final long pos = toLong(exprs[1], qc);
    final Value insert = exprs[2].value(qc);

    // prepend, append or insert new value
    final long size = value.size(), ps = Math.min(Math.max(0, pos - 1), size);
    return ps == 0 ? ValueBuilder.concat(insert, value, qc) :
           ps == size ? ValueBuilder.concat(value, insert, qc) :
           ((Seq) value).insertBefore(ps, insert, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr3 = exprs[2];
    if(expr1 == Empty.VALUE) return expr3;
    if(expr3 == Empty.VALUE) return expr1;

    final SeqType st1 = expr1.seqType(), st3 = expr3.seqType();
    final long size1 = expr1.size(), size3 = expr3.size();
    final long sz = size1 != -1 && size3 != -1 ? size1 + size3 : -1;
    exprType.assign(st1.type.union(st3.type), st1.occ.add(st3.occ), sz);
    return this;
  }
}
