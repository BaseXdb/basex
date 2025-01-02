package org.basex.query.func.array;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayFoldRight extends ArrayFoldLeft {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final FItem action = action(qc);

    final HofArgs args = new HofArgs(3, action).set(1, arg(1).value(qc));
    long p = array.structSize();
    for(final ListIterator<Value> iter = array.iterator(p); iter.hasPrevious();) {
      args.set(1, invoke(action, args.set(0, iter.previous()).pos(p--), qc));
      if(skip(qc, args)) break;
    }
    return args.get(1);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return optType(cc, true, false);
  }
}
