package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class MapFilter extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQMap map = toMap(exprs[0], qc);
    final FItem func = toFunction(exprs[1], 2, qc);

    final MapBuilder builder = new MapBuilder(info);
    map.apply((k, v) -> {
      qc.checkStop();
      if(toBoolean(func.invoke(qc, info, k, v).item(qc, info))) builder.put(k, v);
    });
    return builder.finish();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0];
    if(expr1 == XQMap.empty()) return expr1;

    final Type type1 = expr1.seqType().type;
    if(type1 instanceof MapType) {
      final MapType mtype1 = (MapType) type1;
      final SeqType declType1 = mtype1.argTypes[0].with(Occ.EXACTLY_ONE);
      exprs[1] = coerceFunc(exprs[1], cc, SeqType.BOOLEAN_O, declType1, mtype1.declType);
      exprType.assign(type1);
    }
    return this;
  }
}
