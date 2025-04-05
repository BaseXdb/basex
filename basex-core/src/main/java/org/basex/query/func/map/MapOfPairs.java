package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
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
public final class MapOfPairs extends MapMerge {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final MergeOptions options = toOptions(arg(1), new MergeOptions(), qc);
    final ValueMerger merger = merger(options, qc, Duplicates.COMBINE);

    final MapBuilder builder = new MapBuilder(input.size());
    for(Item item; (item = qc.next(input)) != null;) {
      final XQMap pair = toRecord(item, SeqType.PAIR, qc);
      final Item key = toAtomItem(pair.get(Str.KEY), qc);
      final Value old = builder.get(key);
      final Value val = merger.merge(key, old, pair.get(Str.VALUE));
      if(val != null) builder.put(key, val);
    }
    return builder.map();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    prepareMerge(1, Duplicates.COMBINE, cc);

    final Type tp = arg(0).seqType().type;
    if(tp instanceof MapType) {
      final SeqType vt = ((MapType) tp).valueType();
      assignType(vt.type.atomic(), vt);
    }
    return this;
  }

  @Override
  public int hofIndex() {
    return defined(1) ? Integer.MAX_VALUE : -1;
  }
}
