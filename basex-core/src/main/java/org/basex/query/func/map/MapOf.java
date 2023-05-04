package org.basex.query.func.map;

import static org.basex.query.QueryError.*;

import java.util.AbstractMap.*;
import java.util.Map.*;

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
public final class MapOf extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter pairs = arg(0).iter(qc);
    final FItem combine = defined(1) ? toFunction(arg(1), 2, qc) : null;

    XQMap result = XQMap.empty();
    for(Item item; (item = qc.next(pairs)) != null;) {
      final Entry<Item, Value> pair = toPair(item, qc);
      final Item key = pair.getKey();
      Value value = pair.getValue();
      if(result.contains(key, info)) {
        final Value old = result.get(key, info);
        value = combine != null ? combine.invoke(qc, info, old, value) :
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

  /**
   * Returns a map entry.
   * @param item item to check
   * @param qc query context
   * @return member
   * @throws QueryException query exception
   */
  private Entry<Item, Value> toPair(final Item item, final QueryContext qc)
      throws QueryException {
    final XQMap map = toMap(item);
    final Item key = toAtomItem(map.get(Str.KEY, info), qc);
    final Value value = map.get(Str.VALUE, info);
    if(map.mapSize() == 2 && (!key.isEmpty() || map.contains(Str.KEY, info)) &&
        (!value.isEmpty() || map.contains(Str.VALUE, info))) {
      return new SimpleEntry<>(key, value);
    }
    throw INVCONVERT_X_X_X.get(info, item.type, "record(value as item()*)", item);
  }
}
