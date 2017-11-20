package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ArrayFilter extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    final FItem fun = checkArity(exprs[1], 1, qc);
    final ArrayBuilder builder = new ArrayBuilder();
    for(final Value val : array.members()) {
      if(toBoolean(fun.invokeItem(qc, info, val))) builder.append(val);
    }
    return builder.freeze();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr ex1 = exprs[0];
    final Type t1 = ex1.seqType().type;

    if(t1 instanceof ArrayType) {
      coerceFunc(1, cc, SeqType.BLN, ((ArrayType) t1).declType);
      exprType.assign(t1);
    }
    return this;
  }
}
