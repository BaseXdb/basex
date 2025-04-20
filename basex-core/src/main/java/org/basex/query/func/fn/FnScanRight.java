package org.basex.query.func.fn;

import org.basex.query.*;
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
public final class FnScanRight extends FnScanLeft {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem action = toFunction(arg(2), 3, qc);

    final HofArgs args = new HofArgs(3, action).set(1, arg(1).value(qc));
    final ValueBuilder vb = new ValueBuilder(qc);
    final Value value = input.value(qc, arg(0));
    for(long p = value.size(); p > 0; p--) {
      vb.add(XQArray.get(args.get(1)));
      args.set(1, invoke(action, args.set(0, value.itemAt(p - 1)).inc(), qc));
    }
    return vb.add(XQArray.get(args.get(1))).value().reverse(qc);
  }
}
