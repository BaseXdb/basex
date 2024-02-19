package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class MapKeysWhere extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final FItem predicate = toFunction(arg(1), 2, qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    map.apply((key, value) -> {
      if(toBoolean(qc, predicate, key, value)) vb.add(key);
    });
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final FuncType ft = arg(0).funcType();
    if(ft instanceof MapType) {
      arg(1, arg -> refineFunc(arg, cc, SeqType.BOOLEAN_O, ft.declType));
      exprType.assign(((MapType) ft).keyType());
    }
    return this;
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
