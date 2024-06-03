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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class MapBuild extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem key = toFunctionOrNull(arg(1), 2, qc);
    final FItem value = toFunctionOrNull(arg(2), 2, qc);
    final FItem combine = toFunctionOrNull(arg(3), 2, qc);

    int p = 0;
    XQMap result = XQMap.empty();
    for(Item item; (item = qc.next(input)) != null;) {
      final Int pos = Int.get(++p);
      final Iter iter = (key != null ? key.invoke(qc, info, item, pos) : item).atomIter(qc, info);
      for(Item k; (k = qc.next(iter)) != null;) {
        Value val = value != null ? value.invoke(qc, info, item, pos) : item;
        if(result.contains(k)) {
          final Value old = result.get(k);
          val = combine != null ? combine.invoke(qc, info, old, val) :
            ValueBuilder.concat(old, val, qc);
        }
        result = result.put(k, val);
      }
    }
    return result;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    if(st.zero()) return cc.voidAndReturn(input, XQMap.empty(), info);

    SeqType rst = st;
    AtomType kt = null;
    if(defined(1)) {
      final FuncType ft = arg(1).funcType();
      if(ft != null) {
        kt = ft.declType.type.atomic();
        if(kt != null) {
          final SeqType dt = kt.seqType(Occ.ZERO_OR_MORE);
          arg(1, arg -> refineFunc(arg, cc, dt, st.with(Occ.EXACTLY_ONE)));
        }
      }
    }
    if(defined(2)) {
      final FuncType ft = arg(2).funcType();
      rst = ft != null ? ft.declType : null;
    }
    if(kt == null) kt = AtomType.ANY_ATOMIC_TYPE;
    if(rst != null && !defined(3)) exprType.assign(MapType.get(kt, rst.with(Occ.ONE_OR_MORE)));
    return this;
  }

  @Override
  public int hofIndex() {
    final boolean key = defined(1), value = defined(2), combine = defined(3);
    return (key ? value || combine : value && combine) ? super.hofIndex() : key ? 1 : value ? 2 : 3;
  }
}
