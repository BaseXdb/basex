package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class ArrayFoldLeft extends FnFoldLeft {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final FItem action = action(qc);

    int p = 0;
    Value result = arg(1).value(qc);
    for(final Value value : array.members()) {
      if(skip(qc, result, value)) break;
      result = action.invoke(qc, info, result, value, Int.get(++p));
    }
    return result;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return optType(cc, true, true);
  }
}
