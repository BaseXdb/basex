package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnOneOrMore extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final long len = iter.size();
    if(len == 0) throw ONEORMORE.get(info);
    if(len > 0) return iter;
    return new Iter() {
      private boolean first = true;
      @Override
      public Item next() throws QueryException {
        final Item it = qc.next(iter);
        if(first) {
          if(it == null) throw ONEORMORE.get(info);
          first = false;
        }
        return it;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value val = exprs[0].value(qc);
    if(val.isEmpty()) throw ONEORMORE.get(info);
    return val;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr ex = exprs[0];
    final SeqType st = ex.seqType();
    if(st.zero()) throw ONEORMORE.get(info);
    return st.mayBeEmpty() ? adoptType(ex) : ex;
  }
}
