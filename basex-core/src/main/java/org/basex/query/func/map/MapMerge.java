package org.basex.query.func.map;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class MapMerge extends StandardFunc {
  /** Duplicate handling. */
  public enum Duplicates {
    /** Reject.    */ REJECT,
    /** Use first. */ USE_FIRST,
    /** Use last.  */ USE_LAST,
    /** Use any.   */ USE_ANY,
    /** Combine.   */ COMBINE;
    @Override
    public String toString() {
      return EnumOption.string(this);
    }
  }
  /** Merge options. */
  public static final class MergeOptions extends Options {
    /** Handle duplicates. */
    public static final EnumOption<Duplicates> DUPLICATES =
        new EnumOption<>("duplicates", Duplicates.USE_FIRST);
  }

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter maps = arg(0).iter(qc);
    final MergeOptions options = toOptions(arg(1), new MergeOptions(), qc);
    final Duplicates merge = options.get(MergeOptions.DUPLICATES);

    // empty input: return empty map
    final Item first = qc.next(maps);
    if(first == null) return XQMap.empty();

    // single input: return single map
    XQMap mp = toMap(first);
    Item current = qc.next(maps);
    if(current == null) return mp;

    // update first map if exactly 2 maps are supplied. otherwise, use map builder
    Item next = qc.next(maps);
    final MapBuilder mb = next == null ? null : new MapBuilder(arg(0).size());
    if(mb != null) mp.forEach((k, v) -> mb.put(k,  v));

    while(current != null) {
      final XQMap map = toMap(current);
      for(final Item key : map.keys()) {
        final Value old = mb != null ? mb.get(key) : mp.getInternal(key, false);
        Value value = map.get(key);
        switch(merge) {
          case REJECT:
            if(old != null) throw MERGE_DUPLICATE_X.get(info, key);
            break;
          case COMBINE:
            if(old != null) value = ValueBuilder.concat(old, value, qc);
            break;
          case USE_FIRST:
            if(old != null) continue;
            break;
          default:
        }
        if(mb != null) mb.put(key, value);
        else mp = mp.put(key, value);
      }
      current = next;
      next = current != null ? qc.next(maps) : null;
    }
    return mb != null ? mb.map() : mp;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    if(arg(0).seqType().type instanceof MapType) {
      // remove empty entries
      if(arg(0) instanceof List &&
          ((Checks<Expr>) arg -> arg == XQMap.empty()).any(arg(0).args())) {
        final ExprList list = new ExprList();
        for(final Expr arg : arg(0).args()) {
          if(arg != XQMap.empty()) list.add(arg);
        }
        arg(0, arg -> List.get(cc, info, list.finish()));
      }
      // return simple arguments
      final SeqType st = arg(0).seqType();
      if(st.one()) return arg(0);

      // rewrite map:merge(map:entry, map) to map:put(map, key, value)
      if(!defined(1) && arg(0) instanceof List && arg(0).args().length == 2) {
        final Expr[] args = arg(0).args();
        if(_MAP_ENTRY.is(args[0]) && args[1].seqType().instanceOf(SeqType.MAP_O)) {
          return cc.function(_MAP_PUT, info, args[1], args[0].arg(0), args[0].arg(1));
        }
      }

      // check if duplicates will be combined (if yes, adjust occurrence of return type)
      MapType mt = (MapType) st.type;
      final SeqType vt = mt.valueType;
      // broaden type if values may be combined
      //   map:merge((1 to 2) ! map { 1: 1 }, map { 'duplicates': 'combine' })
      if(!vt.zero() && defined(1)) {
        if(!(arg(1) instanceof Value) || toOptions(arg(1), new MergeOptions(), cc.qc).
            get(MergeOptions.DUPLICATES) == Duplicates.COMBINE) {
          mt = MapType.get(mt.keyType, vt.union(Occ.ONE_OR_MORE));
        }
      }
      exprType.assign(mt);
    }
    return this;
  }
}
