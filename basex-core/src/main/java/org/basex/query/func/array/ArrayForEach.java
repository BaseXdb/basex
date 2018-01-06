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
 * @author BaseX Team 2005-18, BSD License
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
    final Type type1 = exprs[0].seqType().type;
    if(type1 instanceof ArrayType) coerceFunc(1, cc, SeqType.ITEM_ZM, ((ArrayType) type1).declType);

    // assign type after coercion (expression might have changed)
    final Type type2 = exprs[1].seqType().type;
    if(type2 instanceof FuncType) exprType.assign(ArrayType.get(((FuncType) type2).declType));

    return this;
  }
}
