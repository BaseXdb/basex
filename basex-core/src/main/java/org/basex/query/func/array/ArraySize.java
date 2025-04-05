package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArraySize extends ArrayFn {
  @Override
  public Int item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    return Int.get(array.structSize());
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = arg(0);
    if(array.seqType().type instanceof ArrayType) {
      final long size = array.structSize();
      if(size >= 0 && !array.has(Flag.NDT)) return Int.get(size);
    }
    return this;
  }
}
