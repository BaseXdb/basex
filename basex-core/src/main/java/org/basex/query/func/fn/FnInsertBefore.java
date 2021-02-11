package org.basex.query.func.fn;

import org.basex.data.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnInsertBefore extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final long pos = pos(qc);
    final Iter insert = exprs[2].iter(qc);

    return new Iter() {
      long p = pos;
      boolean last;

      @Override
      public Item next() throws QueryException {
        while(!last) {
          final boolean sub = p == -1 || --p == -1;
          final Item item = qc.next(sub ? insert : iter);
          if(item != null) return item;
          if(sub) --p;
          else last = true;
        }
        return p >= 0 ? insert.next() : null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc), insert = exprs[2].value(qc);
    final long size = value.size(), pos = Math.min(pos(qc), size);

    // prepend, append or insert new value
    return pos == 0 ? ValueBuilder.concat(insert, value, qc) :
           pos == size ? ValueBuilder.concat(value, insert, qc) :
           ((Seq) value).insertBefore(pos, insert, qc);
  }


  /**
   * Returns the insertion position.
   * @param qc query context
   * @return position
   * @throws QueryException query exception
   */
  private long pos(final QueryContext qc) throws QueryException {
    return Math.max(0, toLong(exprs[1], qc) - 1);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1], expr3 = exprs[2];
    if(expr1 == Empty.VALUE) return expr3;
    if(expr3 == Empty.VALUE) return expr1;

    final SeqType st1 = expr1.seqType(), st3 = expr3.seqType();
    final long size1 = expr1.size(), size3 = expr3.size();

    if(expr2 instanceof Value) {
      final long pos = pos(cc.qc);
      if(pos == 0) return List.get(cc, info, expr3, expr1);
      if(size1 != -1 && pos >= size1) return List.get(cc, info, expr1, expr3);
    }

    final long sz = size1 != -1 && size3 != -1 ? size1 + size3 : -1;
    exprType.assign(st1.union(st3), st1.occ.add(st3.occ), sz);
    final Data data = expr1.data();
    if(data != null && expr3.data() == data) data(data);

    return this;
  }
}
