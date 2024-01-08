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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnInsertBefore extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter input = arg(0).iter(qc), insert = arg(2).iter(qc);
      final long osize = input.size(), isize = insert.size();
      final long size = osize != -1 && isize != -1 ? osize + isize : -1;
      final long ps = pos(qc), pos = osize != -1 ? Math.min(ps, osize) : ps;
      long p = pos;
      boolean last;

      @Override
      public Item next() throws QueryException {
        while(!last) {
          final boolean sub = p == -1 || --p == -1;
          final Item item = qc.next(sub ? insert : input);
          if(item != null) return item;
          if(sub) --p;
          else last = true;
        }
        return p >= 0 ? insert.next() : null;
      }
      @Override
      public Item get(final long i) throws QueryException {
        final long off = i - pos;
        return off < 0 ? input.get(i) : off < isize ? insert.get(off) : input.get(i - isize);
      }
      @Override
      public long size() {
        return size;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc), insert = arg(2).value(qc);
    final long osize = input.size(), pos = Math.min(pos(qc), osize);

    // prepend, append or insert new value
    return pos == 0 ? ValueBuilder.concat(insert, input, qc) :
           pos == osize ? ValueBuilder.concat(input, insert, qc) :
           ((Seq) input).insert(pos, insert, qc);
  }

  /**
   * Returns the insertion position.
   * @param qc query context
   * @return position
   * @throws QueryException query exception
   */
  private long pos(final QueryContext qc) throws QueryException {
    return Math.max(0, toLong(arg(1), qc) - 1);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), pos = arg(1), insert = arg(2);
    if(input == Empty.VALUE) return insert;
    if(insert == Empty.VALUE) return input;

    final SeqType st = input.seqType(), stInsert = insert.seqType();
    final long size = input.size(), sizeInsert = insert.size();

    if(pos instanceof Value) {
      final long ps = pos(cc.qc);
      if(ps == 0) return List.get(cc, info, insert, input);
      if(size != -1 && ps >= size) return List.get(cc, info, input, insert);
    }

    final long sz = size != -1 && sizeInsert != -1 ? size + sizeInsert : -1;
    exprType.assign(st.union(stInsert), st.occ.add(stInsert.occ), sz).data(input, insert);

    return this;
  }
}
