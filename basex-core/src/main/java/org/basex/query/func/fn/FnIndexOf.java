package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnIndexOf extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter input = exprs[0].atomIter(qc, info);
      final Item search = toAtomItem(exprs[1], qc);
      final Collation coll = toCollation(2, qc);
      int c;

      @Override
      public Int next() throws QueryException {
        for(Item item; (item = qc.next(input)) != null;) {
          ++c;
          if(item.comparable(search) && OpV.EQ.eval(item, search, coll, sc, info)) {
            return Int.get(c);
          }
        }
        return null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr input = exprs[0];
    final SeqType st = input.seqType();
    if(st.zero()) return input;
    if(st.zeroOrOne() && !st.mayBeArray()) exprType.assign(Occ.ZERO_OR_ONE);
    return this;
  }
}
