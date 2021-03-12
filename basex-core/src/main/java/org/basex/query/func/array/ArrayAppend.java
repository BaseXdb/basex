package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.constr.*;
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
public final class ArrayAppend extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return toArray(exprs[0], qc).snoc(exprs[1].value(qc));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if(expr1 == XQArray.empty()) return new CArray(info, true, expr2).optimize(cc);

    final Type type1 = expr1.seqType().type;
    if(type1 instanceof ArrayType) {
      final SeqType dt = ((ArrayType) type1).declType.union(expr2.seqType());
      exprType.assign(ArrayType.get(dt));
    }
    return this;
  }
}
