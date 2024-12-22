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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class MapFilter extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final FItem predicate = toFunction(arg(1), 2, qc);

    final MapBuilder mb = new MapBuilder();
    final HofArgs args = new HofArgs(2);
    map.forEach((key, value) -> {
      if(test(predicate, args.set(0, key).set(1, value), qc)) mb.put(key, value);
    });
    return mb.map();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0);
    if(map == XQMap.empty()) return map;

    final Type type = map.seqType().type;
    if(type instanceof MapType) {
      final MapType mtype = (MapType) type;
      final SeqType declType = SeqType.get(mtype.keyType, Occ.EXACTLY_ONE);
      arg(1, arg -> refineFunc(arg, cc, declType, mtype.valueType));
      exprType.assign(type);
    }
    return this;
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
