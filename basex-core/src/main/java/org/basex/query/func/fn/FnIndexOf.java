package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
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
      final Iter iter = exprs[0].atomIter(qc, info);
      final Item srch = toAtomItem(exprs[1], qc);
      final Collation coll = toCollation(2, qc);
      int c = 1;

      @Override
      public Int next() throws QueryException {
        for(Item item; (item = qc.next(iter)) != null; ++c) {
          if(item.comparable(srch) && OpV.EQ.eval(item, srch, coll, sc, info)) return Int.get(c);
        }
        return null;
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(st.zero()) return expr;
    if(st.zeroOrOne() && !st.mayBeArray()) exprType.assign(Occ.ZERO_OR_ONE);
    return this;
  }
}
