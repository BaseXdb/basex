package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ArrayFoot extends ArrayFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    if(array == XQArray.empty()) throw QueryError.ARRAYEMPTY.get(info);
    return array.foot();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type type = arg(0).seqType().type;
    if(type instanceof ArrayType) exprType.assign(((ArrayType) type).memberType);
    return this;
  }
}
