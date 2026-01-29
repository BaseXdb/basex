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

    Value value;
    if(defined(2)) {
      value = map.getOrNull(key);
      if(value == null) value = arg(2).value(qc);
    } else {
      value = map.get(key);
    }
    return value;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0), key = arg(1);
    final boolean dflt = defined(2);
    if(map == XQMap.empty()) return dflt ? arg(2) : Empty.VALUE;

    final MapTypeInfo mti = MapTypeInfo.get(map).key(key);
    SeqType st = null;
    boolean notFound = false;
    if(mti.field != null) {
      // use optimized getter for records
      if(!mti.record.hasOptional()) return new RecordGet(info, map, mti.index).optimize(cc);
      // type of result (if it exists)
      st = mti.field.seqType();
    } else if(mti.validKey) {
      // map:get({ 'a': 1 }, 'b') → ()
      if(!mti.record.isExtensible()) notFound = true;
    }

    if(mti.mapType != null) {
      // map:get({ 1: 1 }, 'string') → ()
      if(mti.keyMismatch) notFound = true;
      // type of result (if it exists)
      else if(st == null) st = mti.mapType.valueType();
    }

    if(st != null) exprType.assign(dflt ? st.union(arg(2).seqType()) : st.union(Occ.ZERO));
    return notFound ? dflt ? arg(2) : Empty.VALUE : this;
  }
}
