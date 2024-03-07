package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.array.XQArray;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ArrayPut extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final long position = toLong(arg(1), qc);
    final Value member = arg(2).value(qc);
    return array.put(toPos(array, position, false), member);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type type = arg(0).seqType().type;
    if(type instanceof ArrayType) {
      final SeqType mt = ((ArrayType) type).memberType.union(arg(2).seqType());
      exprType.assign(ArrayType.get(mt));
    }
    return this;
  }
}
