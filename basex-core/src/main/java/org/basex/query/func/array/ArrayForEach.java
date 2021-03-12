package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ArrayForEach extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(exprs[0], qc);
    final FItem func = checkArity(exprs[1], 1, qc);

    final ArrayBuilder builder = new ArrayBuilder();
    for(final Value value : array.members()) builder.append(func.invoke(qc, info, value));
    return builder.freeze();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0];
    if(expr1 == XQArray.empty()) return expr1;

    final Type type1 = expr1.seqType().type;
    if(type1 instanceof ArrayType) {
      exprs[1] = coerceFunc(exprs[1], cc, SeqType.ITEM_ZM, ((ArrayType) type1).declType);
    }

    // assign type after coercion (expression might have changed)
    final FuncType ft = exprs[1].funcType();
    if(ft != null) exprType.assign(ArrayType.get(ft.declType));

    return this;
  }
}
