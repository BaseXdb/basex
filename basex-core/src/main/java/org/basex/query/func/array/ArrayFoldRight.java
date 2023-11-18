package org.basex.query.func.array;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class ArrayFoldRight extends ArrayFoldLeft {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final FItem action = action(qc);

    Value result = arg(1).value(qc);
    long p = array.arraySize();
    for(final ListIterator<Value> iter = array.iterator(p); iter.hasPrevious();) {
      final Value value = iter.previous();
      result = action.invoke(qc, info, value, result, Int.get(p--));
      if(skip(qc, value, result)) break;
    }
    return result;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return optType(cc, true, false);
  }
}
