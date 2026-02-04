package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnIndexOf extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).atomIter(qc, info);
    final Item search = toAtomItem(arg(1), qc);
    final Collation collation = toCollation(arg(2), qc);

    return new Iter() {
      int c;

      @Override
      public Itr next() throws QueryException {
        for(Item item; (item = qc.next(input)) != null;) {
          ++c;
          if(item.comparable(search) && item.compare(search, collation, false, info) == 0) {
            return Itr.get(c);
          }
        }
        return null;
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    if(st.zero()) return input;
    if(st.zeroOrOne() && !st.mayBeArray()) exprType.assign(Occ.ZERO_OR_ONE);
    return this;
  }
}
