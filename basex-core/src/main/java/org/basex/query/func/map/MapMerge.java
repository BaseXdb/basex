package org.basex.query.func.map;

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
 * @author BaseX Team 2005-23, BSD License
 * @author Leo Woerteler
 */
public final class MapMerge extends StandardFunc {
  /** Merge options. */
  public static final class MergeOptions extends Options {
    /** Handle duplicates. */
    public static final EnumOption<MergeDuplicates> DUPLICATES =
        new EnumOption<>("duplicates", MergeDuplicates.USE_FIRST);
  }

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter maps = arg(0).iter(qc);
    final MergeOptions options = toOptions(arg(1), new MergeOptions(), false, qc);

    final MergeDuplicates merge = options.get(MergeOptions.DUPLICATES);
    XQMap map = XQMap.empty();
    for(Item item; (item = qc.next(maps)) != null;) {
      map = map.addAll(toMap(item), merge, qc, info);
    }
    return map;
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
      final SeqType dt = mt.declType;
      // broaden type if values may be combined
      //   map:merge((1 to 2) ! map { 1: 1 }, map { 'duplicates': 'combine' })
      if(!dt.zero() && defined(1)) {
        if(!(arg(1) instanceof Value) || toOptions(arg(1), new MergeOptions(), false, cc.qc).
            get(MergeOptions.DUPLICATES) == MergeDuplicates.COMBINE) {
          mt = MapType.get(mt.keyType(), dt.union(Occ.ONE_OR_MORE));
        }
      }
      exprType.assign(mt);
    }

    return this;
  }
}
