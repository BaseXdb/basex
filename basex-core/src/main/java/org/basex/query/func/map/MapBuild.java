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

    XQMap result = XQMap.empty();
    for(Item item; (item = qc.next(input)) != null;) {
      final Item k = (key != null ? eval(key, qc, item) : item).atomItem(qc, info);
      if(!k.isEmpty()) {
        Value v = value != null ? eval(value, qc, item) : item;
        if(result.contains(k, info)) {
          final Value old = result.get(k, info);
          v = combine != null ? eval(combine, qc, old, v) : ValueBuilder.concat(old, v, qc);
        }
        result = result.put(k, v, info);
      }
    }
    return result;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    if(st.zero()) return cc.merge(input, XQMap.empty(), info);

    AtomType kt = null;
    if(defined(1)) {
      final FuncType ft = arg(1).funcType();
      if(ft != null) {
        kt = ft.declType.type.atomic();
        if(kt != null) {
          final SeqType dt = kt.seqType(Occ.ZERO_OR_ONE);
          arg(1, arg -> coerceFunc(arg, cc, dt, st.with(Occ.EXACTLY_ONE)));
        }
      }
    }
    if(kt == null) kt = AtomType.ANY_ATOMIC_TYPE;
    exprType.assign(MapType.get(kt, st.with(Occ.ONE_OR_MORE)));
    return this;
  }
}
