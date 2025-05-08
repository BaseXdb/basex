package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnScanLeft extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem action = toFunction(arg(2), 3, qc);

    return new Iter() {
      final HofArgs args = new HofArgs(3).set(0, arg(1).value(qc));
      XQArray init = XQArray.get(args.get(0));

      @Override
      public Item next() throws QueryException {
        Item item = init;
        if(item != null) {
          init = null;
        } else {
          item = input.next();
          if(item != null) {
            args.set(0, invoke(action, args.set(1, item).inc(), qc));
            item = XQArray.get(args.get(0));
          }
        }
        return item;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    final Expr input = arg(0), init = arg(1);
    return input.seqType().zero() ? init : this;
  }

  @Override
  public final int hofIndex() {
    return 2;
  }
}
