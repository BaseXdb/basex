package org.basex.query.func.array;

import java.util.*;

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
public final class ArrayForEachPair extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array1 = toArray(arg(0), qc), array2 = toArray(arg(1), qc);
    final FItem action = toFunction(arg(2), 3, qc);

    final HofArgs args = new HofArgs(3, action);
    final ArrayBuilder ab = new ArrayBuilder(qc);
    final Iterator<Value> as = array1.iterator(0), bs = array2.iterator(0);
    while(as.hasNext() && bs.hasNext()) {
      ab.add(invoke(action, args.set(0, as.next()).set(1, bs.next()).inc(), qc));
    }
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array1 = arg(0), array2 = arg(1);
    if(array1 == XQArray.empty()) return array1;
    if(array2 == XQArray.empty()) return array2;

    final Type type1 = array1.seqType().type, type2 = array2.seqType().type;
    if(type1 instanceof final ArrayType at1 && type2 instanceof final ArrayType at2) {
      arg(2, arg -> refineFunc(arg, cc, at1.valueType(), at2.valueType(), Types.INTEGER_O));
    }

    // assign type after coercion (expression might have changed)
    final FuncType ft = arg(2).funcType();
    if(ft != null) exprType.assign(ArrayType.get(ft.declType));

    return this;
  }

  @Override
  public int hofIndex() {
    return 2;
  }
}
