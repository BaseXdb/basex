package org.basex.query.func.db;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class DbNodePre extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    return new Iter() {
      @Override
      public Int next() throws QueryException {
        final Item item = qc.next(iter);
        return item == null ? null : Int.get(toDBNode(item).pre());
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final LongList list = new LongList();
    final Iter iter = exprs[0].iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) list.add(toDBNode(item).pre());
    return IntSeq.get(list.finish());
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final SeqType st = seqType();
    exprType.assign(st.type, st.occ, exprs[0].size());
    return this;
  }
}
