package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MapBuild extends MapMerge {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem keys = toFunctionOrNull(arg(1), 2, qc);
    final FItem value = toFunctionOrNull(arg(2), 2, qc);
    final MergeOptions options = toOptions(arg(3), new MergeOptions(), qc);
    final ValueMerger merger = merger(options, qc, Duplicates.COMBINE);

    final HofArgs args = new HofArgs(2, keys, value);
    final MapBuilder builder = new MapBuilder(input.size());
    for(Item item; (item = qc.next(input)) != null;) {
      args.set(0, item).inc();
      final Iter iter = (keys != null ? invoke(keys, args, qc) : item).atomIter(qc, info);
      for(Item key; (key = qc.next(iter)) != null;) {
        final Value old = builder.get(key);
        final Value val = merger.merge(key, old, value != null ? invoke(value, args, qc) : item);
        if(val != null) builder.put(key, val);
      }
    }
    return builder.map(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    prepareMerge(3, Duplicates.COMBINE, cc);

    final Expr input = arg(0), keys = arg(1), value = arg(2);
    final SeqType st = input.seqType(), s1t = st.with(Occ.EXACTLY_ONE);
    if(st.zero()) return cc.voidAndReturn(input, XQMap.empty(), info);

    final boolean noKey = keys.size() == 0, fiKey = keys instanceof FuncItem;
    Type kt = noKey || fiKey ? s1t.type : AtomType.ITEM;
    if(fiKey) {
      arg(1, arg -> refineFunc(arg, cc, s1t));
      kt = arg(1).funcType().declType.type;
    }
    kt = kt.atomic();

    final boolean noValue = value.size() == 0, fiValue = value instanceof FuncItem;
    SeqType vt = noValue || fiValue ? s1t : Types.ITEM_ZM;
    if(fiValue) {
      arg(2, arg -> refineFunc(arg, cc, s1t));
      vt = arg(2).funcType().declType;
    }
    assignType(kt, vt);
    return this;
  }

  @Override
  public long structSize() {
    final Expr input = arg(0), keys = arg(1);
    return input instanceof RangeSeq && keys.size() == 0 ? input.size() : -1;
  }

  @Override
  public int hofOffsets() {
    return defined(3) ? Integer.MAX_VALUE : hofOffset(1) | hofOffset(2);
  }
}
