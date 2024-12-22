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

    final HofArgs args = new HofArgs(2);
    final ValueBuilder vb = new ValueBuilder(qc);
    map.forEach((key, value) -> {
      if(test(predicate, args.set(0, key).set(1, value), qc)) vb.add(key);
    });
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Type tp = arg(0).seqType().type;
    if(tp instanceof MapType) {
      final MapType mt = (MapType) tp;
      final Type kt = mt.keyType;
      arg(1, arg -> refineFunc(arg, cc, kt.seqType()));
      exprType.assign(kt);
    }
    return this;
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
