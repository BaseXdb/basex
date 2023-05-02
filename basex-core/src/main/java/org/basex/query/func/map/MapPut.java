package org.basex.query.func.map;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Leo Woerteler
 */
public final class MapPut extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final Item key = toAtomItem(arg(1), qc);
    final Value value = arg(2).value(qc);

    return map.put(key, value, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0), key = arg(1), value = arg(2);
    if(map == XQMap.empty()) return cc.function(_MAP_ENTRY, info, key, value);

    final Type type = map.seqType().type;
    if(type instanceof MapType) {
      AtomType typeKey = key.seqType().type.atomic();
      if(typeKey != null) {
        SeqType st = value.seqType();
        // merge types if input is expected to have at least one entry
        if(!(map instanceof XQMap && ((XQMap) map).mapSize() == 0)) {
          final MapType mt = (MapType) type;
          typeKey = (AtomType) mt.keyType().union(typeKey);
          st = mt.declType.union(value.seqType());
        }
        exprType.assign(MapType.get(typeKey, st));
      }
    }
    return this;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    arg(1, arg -> arg.simplifyFor(Simplify.DATA, cc));
  }
}
