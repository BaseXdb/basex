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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnTail extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // retrieve and decrement iterator size
    final Iter iter = qc.iter(exprs[0]);
    final long is = iter.size() - 1;
    // return empty iterator if iterator is known to yield 0 or 1 results, or if it yields no item
    if(is == -1 || is == 0 || iter.next() == null) return Empty.ITER;

    // create new iterator, based on original iterator
    return new Iter() {
      @Override
      public Item get(final long i) throws QueryException {
        return iter.get(i + 1);
      }
      @Override
      public Item next() throws QueryException {
        return iter.next();
      }
      @Override
      public long size() {
        return is < 0 ? -1 : is;
      }
      @Override
      public Value value(final QueryContext q) throws QueryException {
        return is < 0 ? super.value(q) : iter.value(q).subSequence(1, is);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // return empty sequence if value has 0 or 1 items
    final Value val = qc.value(exprs[0]);
    final long vs = val.size() - 1;
    return vs < 1 ? Empty.SEQ : val.subSequence(1, vs);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // ignore standard limitation for large values
    if(allAreValues()) return value(cc.qc);

    final Expr ex = exprs[0];
    final long sz = ex.size() - 1;
    final SeqType st = ex.seqType();
    if(sz == -1 || sz == 0 || st.zeroOrOne()) return Empty.SEQ;
    if(sz > 0) {
      exprType.assign(st.type, sz);
    } else {
      exprType.assign(st.type);
    }
    return this;
  }
}
