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
public final class ArrayForEach extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final FItem action = toFunction(arg(1), 2, qc);

    final HofArgs args = new HofArgs(2, action);
    final ArrayBuilder ab = new ArrayBuilder(qc);
    for(final Value value : array.iterable()) {
      ab.add(invoke(action, args.set(0, value).inc(), qc));
    }
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = arg(0);
    if(array == XQArray.empty()) return array;

    final Type type = array.seqType().type;
    if(type instanceof ArrayType) {
      arg(1, arg -> refineFunc(arg, cc, ((ArrayType) type).valueType(), SeqType.INTEGER_O));
    }

    // assign type after coercion (expression might have changed)
    final FuncType ft = arg(1).funcType();
    if(ft != null) exprType.assign(ArrayType.get(ft.declType));

    return this;
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
