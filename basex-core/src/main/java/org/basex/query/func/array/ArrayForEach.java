package org.basex.query.func.array;

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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ArrayForEach extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final FItem action = toFunction(arg(1), 2, qc);

    int p = 0;
    final ArrayBuilder ab = new ArrayBuilder();
    for(final Value value : array.members()) {
      ab.append(action.invoke(qc, info, value, Int.get(++p)));
    }
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr array = arg(0);
    if(array == XQArray.empty()) return array;

    final Type type = array.seqType().type;
    if(type instanceof ArrayType) {
      arg(1, arg -> refineFunc(arg, cc, SeqType.ITEM_ZM, ((ArrayType) type).declType,
          SeqType.INTEGER_O));
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
