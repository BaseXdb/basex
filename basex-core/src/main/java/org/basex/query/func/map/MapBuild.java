package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class MapBuild extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem key = defined(1) ? toFunction(arg(1), 1, qc) : null;
    final FItem value = defined(2) ? toFunction(arg(2), 1, qc) : null;
    final FItem combine = defined(3) ? toFunction(arg(3), 2, qc) : null;

    XQMap map = XQMap.empty();
    for(Item item; (item = qc.next(input)) != null;) {
      final Item k = (key != null ? key.invoke(qc, info, item) : item).atomItem(qc, info);
      if(!k.isEmpty()) {
        Value v = value != null ? value.invoke(qc, info, item) : item;
        if(map.contains(k, info)) {
          final Value o = map.get(k, info);
          v = combine != null ? combine.invoke(qc, info, o, v) : ValueBuilder.concat(o, v, qc);
        }
        map = map.put(k, v, info);
      }
    }
    return map;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    if(st.zero()) return cc.merge(input, XQMap.empty(), info);

    AtomType ktype = null;
    if(defined(1)) {
      final FuncType ft = arg(1).funcType();
      if(ft != null) {
        ktype = ft.declType.type.atomic();
        if(ktype != null) {
          final SeqType dt = ktype.seqType(Occ.ZERO_OR_ONE);
          arg(1, arg -> coerceFunc(arg, cc, dt, st.with(Occ.EXACTLY_ONE)));
        }
      }
    }
    if(ktype == null) ktype = AtomType.ANY_ATOMIC_TYPE;
    exprType.assign(MapType.get(ktype, st.with(Occ.ONE_OR_MORE)));
    return this;
  }
}
