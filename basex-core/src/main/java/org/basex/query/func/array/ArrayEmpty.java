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
public final class ArrayEmpty extends ArrayFn {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(test(qc, ii, 0));
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    return toArray(arg(0), qc) == XQArray.empty();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);
    final long size = arraySize(array);
    return size == -1 || array.has(Flag.NDT) ? this : Bln.get(size == 0);
  }
}
