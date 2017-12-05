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
public final class ArrayForEach extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    final FItem func = checkArity(exprs[1], 1, qc);
    final ArrayBuilder builder = new ArrayBuilder();
    for(final Value value : array.members()) builder.append(func.invokeValue(qc, info, value));
    return builder.freeze();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Type t1 = exprs[0].seqType().type;
    if(t1 instanceof ArrayType) coerceFunc(1, cc, SeqType.ITEM_ZM, ((ArrayType) t1).declType);

    // assign type after coercion (expression might have changed)
    final Type t2 = exprs[1].seqType().type;
    if(t2 instanceof FuncType) exprType.assign(ArrayType.get(((FuncType) t2).declType));

    return this;
  }
}
