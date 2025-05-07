package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArrayFilter extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final FItem predicate = toFunction(arg(1), 2, qc);

    final HofArgs args = new HofArgs(2, predicate).set(0, arg(0).value(qc));
    final ArrayBuilder ab = new ArrayBuilder(qc);
    for(final Value value : array.iterable()) {
      if(test(predicate, args.set(0, value).inc(), qc)) ab.add(value);
    }
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = arg(0);
    if(array == XQArray.empty()) return array;

    final Type type = array.seqType().type;
    if(type instanceof final ArrayType at) {
      arg(1, arg -> refineFunc(arg, cc, at.valueType(), SeqType.INTEGER_O));
      exprType.assign(type);
    }
    return this;
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
