package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArraySize extends ArrayFn {
  @Override
  public Itr item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    return Itr.get(array.structSize());
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);
    final long size = arraySize(array);
    return size == -1 || array.has(Flag.NDT) ? this : Itr.get(size);
  }
}
