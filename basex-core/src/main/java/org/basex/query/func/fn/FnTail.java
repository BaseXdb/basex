package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.SeqType.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnTail extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // check if iterator only returns single result
    final Iter iter = qc.iter(exprs[0]);
    final long is = iter.size();
    if(is == 0 || is == 1 || iter.next() == null) return Empty.ITER;

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
        // return -1, or decreased count of original iterator
        return is == -1 ? -1 : is - 1;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value val = qc.value(exprs[0]);
    final long vs = val.size();
    return vs < 2 ? Empty.SEQ : val.subSeq(1, vs - 1);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr ex = exprs[0];
    final long sz = ex.size();
    if(sz == 0 || sz == 1) return Empty.SEQ;
    seqType = ex.seqType().withOcc(sz == 2 ? Occ.ONE : Occ.ZERO_MORE);
    return this;
  }
}
