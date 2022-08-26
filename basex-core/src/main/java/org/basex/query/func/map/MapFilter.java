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
    final FItem predicate = toFunction(exprs[1], 2, qc);

    final MapBuilder mb = new MapBuilder(info);
    map.apply((k, v) -> {
      if(toBoolean(predicate.invoke(qc, info, k, v).item(qc, info))) {
        mb.put(k, v);
      }
    });
    return mb.map();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = exprs[0];
    if(map == XQMap.empty()) return map;

    final Type type = map.seqType().type;
    if(type instanceof MapType) {
      final MapType mtype = (MapType) type;
      final SeqType declType = mtype.argTypes[0].with(Occ.EXACTLY_ONE);
      exprs[1] = coerceFunc(exprs[1], cc, SeqType.BOOLEAN_O, declType, mtype.declType);
      exprType.assign(type);
    }
    return this;
  }
}
