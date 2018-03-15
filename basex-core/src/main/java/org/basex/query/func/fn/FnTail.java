package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.file.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FnTail extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // retrieve and decrement iterator size
    final Iter iter = exprs[0].iter(qc);
    final long size = iter.size() - 1;

    // return empty iterator if iterator yields 0 or 1 items, or if result is an empty sequence
    if(size == -1 || size == 0 || iter.next() == null) return Empty.ITER;

    // check if iterator is value-based
    final Value value = iter.value();
    if(value != null) return value.subSequence(1, size, qc).iter();

    // return optimized iterator if result size is known
    if(size > 0) return new Iter() {
      @Override
      public Item next() throws QueryException {
        return qc.next(iter);
      }
      @Override
      public Item get(final long i) throws QueryException {
        return iter.get(i + 1);
      }
      @Override
      public long size() {
        return size;
      }
    };

    // otherwise, return standard iterator
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        return qc.next(iter);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // return empty sequence if value has 0 or 1 items
    final Value value = exprs[0].value(qc);
    final long size = value.size() - 1;
    return size < 1 ? Empty.SEQ : value.subSequence(1, size, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // ignore standard limitation for large values
    final Expr expr = exprs[0];
    if(expr instanceof Value) return value(cc.qc);

    final long size = expr.size() - 1;
    final SeqType st = expr.seqType();
    if(size == -1 || size == 0 || st.zeroOrOne()) return Empty.SEQ;
    exprType.assign(st.type, Occ.ZERO_MORE, size);

    // faster retrieval of lines
    return FileReadTextLines.rewrite(this, 2, Long.MAX_VALUE, cc, info);
  }
}
