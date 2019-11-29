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
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
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
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter maps = exprs[0].iter(qc);
    final MergeDuplicates merge = options(qc).get(MergeOptions.DUPLICATES);
    XQMap map = XQMap.EMPTY;
    for(Item item; (item = qc.next(maps)) != null;) map = map.addAll(toMap(item), merge, qc, info);
    return map;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Type type = exprs[0].seqType().type;
    if(type instanceof MapType) {
      // check if duplicates will be combined (if yes, adjust occurrence of return type)
      MapType mt = (MapType) type;
      // evaluate options (broaden type if values may be combined)
      if(exprs.length < 2 || !(exprs[1] instanceof Value) ||
        options(cc.qc).get(MergeOptions.DUPLICATES) == MergeDuplicates.COMBINE) {
        final SeqType st = mt.declType;
        mt = MapType.get(mt.keyType(), st.with(st.oneOrMore() ? Occ.ONE_MORE : Occ.ZERO_MORE));
      }
      exprType.assign(mt);
    }
    return this;
  }

  /**
   * Returns map options.
   * @param qc query context
   * @return options
   * @throws QueryException query exception
   */
  private MergeOptions options(final QueryContext qc) throws QueryException {
    final MergeOptions opts = new MergeOptions();
    if(exprs.length > 1) new FuncOptions(info).acceptUnknown().assign(toMap(exprs[1], qc), opts);
    return opts;
  }
}
