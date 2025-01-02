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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MapBuild extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem key = toFunctionOrNull(arg(1), 2, qc);
    final FItem value = toFunctionOrNull(arg(2), 2, qc);
    final FItem combine = toFunctionOrNull(arg(3), 2, qc);

    final HofArgs args = new HofArgs(2, key, value), cargs = new HofArgs(2);
    final MapBuilder map = new MapBuilder(input.size());
    for(Item item; (item = qc.next(input)) != null;) {
      args.set(0, item).inc();
      final Iter iter = (key != null ? invoke(key, args, qc) : item).atomIter(qc, info);
      for(Item k; (k = qc.next(iter)) != null;) {
        Value val = value != null ? invoke(value, args, qc) : item;
        final Value old = map.get(k);
        if(old != null) {
          val = combine != null ? invoke(combine, cargs.set(0, old).set(1, val), qc) :
            ValueBuilder.concat(old, val, qc);
        }
        map.put(k, val);
      }
    }
    return map.map();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    final SeqType st = input.seqType(), s1t = st.with(Occ.EXACTLY_ONE);
    if(st.zero()) return cc.voidAndReturn(input, XQMap.empty(), info);

    final boolean fiKey = arg(1) instanceof FuncItem;
    Type kt = arg(1).size() == 0 || fiKey ? s1t.type : AtomType.ITEM;
    if(fiKey) {
      arg(1, arg -> refineFunc(arg, cc, s1t));
      kt = arg(1).funcType().declType.type;
    }
    kt = kt.atomic();
    if(kt == null) kt = AtomType.ANY_ATOMIC_TYPE;

    final boolean fiValue = arg(2) instanceof FuncItem;
    SeqType vt = arg(2).size() == 0 || fiValue ? s1t : SeqType.ITEM_ZM;
    if(fiValue) {
      arg(2, arg -> refineFunc(arg, cc, s1t));
      vt = arg(2).funcType().declType;
    }

    // do not refine value type if function for combining items exists
    if(arg(3).size() != 0) vt = SeqType.ITEM_ZM;

    exprType.assign(MapType.get(kt, vt.union(Occ.ONE_OR_MORE)));
    return this;
  }

  @Override
  public int hofIndex() {
    final boolean key = defined(1), value = defined(2), combine = defined(3);
    return (key ? value || combine : value && combine) ? super.hofIndex() : key ? 1 : value ? 2 : 3;
  }
}
