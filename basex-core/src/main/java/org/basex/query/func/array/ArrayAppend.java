package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.constr.*;
import org.basex.query.value.array.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class ArrayAppend extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return toArray(exprs[0], qc).snoc(exprs[1].value(qc));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = exprs[0], add = exprs[1];
    if(array == XQArray.empty()) return new CArray(info, true, add).optimize(cc);

    final Type type = array.seqType().type;
    if(type instanceof ArrayType) {
      final SeqType dt = ((ArrayType) type).declType.union(add.seqType());
      exprType.assign(ArrayType.get(dt));
    }
    return this;
  }
}
