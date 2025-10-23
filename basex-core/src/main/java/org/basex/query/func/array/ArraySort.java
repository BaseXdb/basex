package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArraySort extends ArraySortBy {
  @Override
  protected Integer[] index(final Value[] values, final QueryContext qc) throws QueryException {
    // identical to {@link FnSort#index}
    final FItem[] keys = { toFunctionOrNull(arg(2), 1, qc) };
    final Collation[] collations = { toCollation(arg(1), qc) };
    final boolean[] invert = { false };
    return index(values, keys, collations, invert, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = arg(0);
    if(array == XQArray.empty()) return array;

    if(array.seqType().type instanceof final ArrayType at) {
      if(defined(2)) arg(2, arg -> refineFunc(arg, cc, at.valueType()));
      exprType.assign(at);
    }
    return this;
  }

  @Override
  public int hofIndex() {
    return 2;
  }
}
