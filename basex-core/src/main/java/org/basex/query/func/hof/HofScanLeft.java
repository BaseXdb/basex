package org.basex.query.func.hof;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class HofScanLeft extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem action = toFunction(arg(2), 2, qc);

    return new Iter() {
      final HofArgs args = new HofArgs(2).set(0, arg(1).value(qc));
      Iter inner = args.get(0).iter();

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item in = inner.next();
          if(in != null) return in;
          final Item item = input.next();
          if(item == null) return null;

          args.set(0, invoke(action, args.set(1, item), qc));
          inner = args.get(0).iter();
        }
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr input = arg(0), zero = arg(1);
    if(input == Empty.VALUE) return zero;

    exprType.assign(input.seqType().union(Occ.ZERO)).data(input);
    return this;
  }
}
