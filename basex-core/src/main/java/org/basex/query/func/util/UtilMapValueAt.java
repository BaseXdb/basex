package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.map.*;
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
public final class UtilMapValueAt extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final long index = toLong(arg(1), qc);

    final long size = map.structSize();
    if(index < 1 || index > size) return Empty.VALUE;

    final Value value = map.valueAt((int) index - 1);
    return value instanceof FuncItem && toBooleanOrFalse(arg(2), qc) ?
      ((FuncItem) value).toMethod(map) : value;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0), index = arg(1);
    if(map == XQMap.empty()) return Empty.VALUE;

    SeqType st = null;
    final MapCompilation mc = MapCompilation.get(map).index(index);
    if(mc.field == null) {
      // util:map-value-at({ 'a': 1 }, 2)  ->  ()
      if(mc.index != null) return Empty.VALUE;
    } else if(!mc.record.hasOptional()) {
      st = mc.field.seqType();
    }
    if(st == null && mc.mapType != null) st = mc.mapType.valueType().union(Occ.ZERO);

    // invalidate function type (%method annotation would need to be removed from type)
    if(st != null && !(st.mayBeFunction() && defined(2))) exprType.assign(st);
    return this;
  }
}
