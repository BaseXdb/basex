package org.basex.query.func.array;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.array.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayReverse extends ArrayFn {
  @Override
  public XQArray value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    return array.reverseArray(qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr array = arg(0);
    // array:reverse(array:reverse($array)) → $array
    if(_ARRAY_REVERSE.is(array) && array.arg(0).seqType().instanceOf(Types.ARRAY_O))
      return array.arg(0);

    if(array.seqType().type instanceof final ArrayType at) exprType.assign(at);
    return this;
  }

  @Override
  public long structSize() {
    return arraySize(arg(0));
  }
}
