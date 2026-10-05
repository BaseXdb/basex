package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MapGet extends MapFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final Item key = toAtomItem(arg(1), qc);

    final Value value = map.getOrNull(key);
    if(value != null) return value;
    if(defined(2)) return arg(2).value(qc);
    return Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0), key = arg(1);
    final boolean dflt = defined(2);
    if(map == XQMap.empty()) return dflt ? arg(2) : Empty.VALUE;

    final MapTypeInfo mti = MapTypeInfo.get(map).key(key);
    // use optimized getter for records
    if(mti.index != 0) return new ShapeGet(info, map, mti.index).optimize(cc);
    // map:get({ 'a': 1 }, 'b') → (), map:get({ 1: 1 }, 'string') → ()
    if(mti.validKey || mti.keyMismatch) {
      return cc.voidAndReturn(map, dflt ? arg(2) : Empty.VALUE, info);
    }
    // type of result
    if(mti.mapType != null) {
      final SeqType st = mti.mapType.valueType();
      exprType.assign(dflt ? st.union(arg(2).seqType()) : st.union(Occ.ZERO));
    }
    return this;
  }
}
