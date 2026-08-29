package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayEmpty extends ArrayFn {
  @Override
  public Bln value(final QueryContext qc) throws QueryException {
    return Bln.get(ebv(qc));
  }

  @Override
  protected boolean ebv(final QueryContext qc) throws QueryException {
    return toArray(arg(0), qc) == XQArray.empty();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);
    final long size = arraySize(array);
    // array:empty([]) → true(), array:empty($nonempty) → false()
    return size == -1 || array.has(Flag.NDT) ? this : Bln.get(size == 0);
  }
}
