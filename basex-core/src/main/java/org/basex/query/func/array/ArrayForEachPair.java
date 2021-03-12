package org.basex.query.func.array;

import java.util.*;

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
public final class ArrayForEachPair extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array1 = toArray(exprs[0], qc), array2 = toArray(exprs[1], qc);
    final FItem func = checkArity(exprs[2], 2, qc);

    final ArrayBuilder builder = new ArrayBuilder();
    final Iterator<Value> as = array1.iterator(0), bs = array2.iterator(0);
    while(as.hasNext() && bs.hasNext()) builder.append(func.invoke(qc, info, as.next(), bs.next()));
    return builder.freeze();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if(expr1 == XQArray.empty()) return expr1;
    if(expr2 == XQArray.empty()) return expr2;

    final Type type1 = expr1.seqType().type, type2 = expr2.seqType().type;
    if(type1 instanceof ArrayType && type2 instanceof ArrayType) {
      exprs[2] = coerceFunc(exprs[2], cc,
        SeqType.ITEM_ZM, ((ArrayType) type1).declType, ((ArrayType) type2).declType);
    }

    // assign type after coercion (expression might have changed)
    final FuncType ft = exprs[2].funcType();
    if(ft != null) exprType.assign(ArrayType.get(ft.declType));

    return this;
  }
}
