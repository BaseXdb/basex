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
public final class MapOfPairs extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter pairs = arg(0).iter(qc);
    final FItem combine = toFunctionOrNull(arg(1), 2, qc);

    XQMap result = XQMap.empty();
    for(Item item; (item = qc.next(pairs)) != null;) {
      // extract key/value record entries
      final XQMap map = toRecord(item, Str.KEY, Str.VALUE);
      final Item key = toAtomItem(map.get(Str.KEY), qc);
      Value value = map.get(Str.VALUE);
      if(result.contains(key)) {
        final Value old = result.get(key);
        value = combine != null ? combine.invoke(qc, info, old, value) :
          ValueBuilder.concat(old, value, qc);
      }
      result = result.put(key, value);
    }
    return result;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type tp = arg(0).seqType().type;
    if(tp instanceof MapType) {
      final SeqType vt = ((MapType) tp).valueType;
      final AtomType kt = vt.type.atomic();
      exprType.assign(MapType.get(kt != null ? kt : AtomType.ANY_ATOMIC_TYPE, vt));
    }
    return this;
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
