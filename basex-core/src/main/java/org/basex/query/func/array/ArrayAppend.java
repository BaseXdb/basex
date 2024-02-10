package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.constr.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ArrayAppend extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final Value member = arg(1).value(qc);
    return array.append(member);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = arg(0), add = arg(1);
    if(array == XQArray.empty()) return new CArray(info, true, add).optimize(cc);

    final Type type = array.seqType().type;
    if(type instanceof ArrayType) {
      final SeqType dt = ((ArrayType) type).declType.union(add.seqType());
      exprType.assign(ArrayType.get(dt));
    }
    return this;
  }
}
