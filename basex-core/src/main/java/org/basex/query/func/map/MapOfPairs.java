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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class MapOfPairs extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter pairs = arg(0).iter(qc);
    final FItem combine = defined(1) ? toFunction(arg(1), 2, qc) : null;

    XQMap result = XQMap.empty();
    for(Item item; (item = qc.next(pairs)) != null;) {
      // extract key/value record entries
      final XQMap map = toRecord(item, Str.KEY, Str.VALUE);
      final Item key = checkType(toItem(map.get(Str.KEY, info), qc), AtomType.ANY_ATOMIC_TYPE);
      Value value = map.get(Str.VALUE, info);
      if(result.contains(key, info)) {
        final Value old = result.get(key, info);
        value = combine != null ? eval(combine, qc, old, value) :
          ValueBuilder.concat(old, value, qc);
      }
      result = result.put(key, value, info);
    }
    return result;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final FuncType ft = arg(0).funcType();
    if(ft instanceof MapType) {
      final SeqType st = ft.declType;
      final AtomType kt = st.type.atomic();
      exprType.assign(MapType.get(kt != null ? kt : AtomType.ANY_ATOMIC_TYPE, st));
    }
    return this;
  }
}
