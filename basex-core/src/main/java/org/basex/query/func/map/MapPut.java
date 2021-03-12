package org.basex.query.func.map;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class MapPut extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQMap map = toMap(exprs[0], qc);
    final Item key = toAtomItem(exprs[1], qc);
    final Value value = exprs[2].value(qc);
    return map.put(key, value, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1], expr3 = exprs[2];
    if(expr1 == XQMap.EMPTY) return cc.function(_MAP_ENTRY, info, expr2, expr3);

    final Type type1 = expr1.seqType().type;
    if(type1 instanceof MapType) {
      Type type2 = expr2.seqType().type.atomic();
      if(type2 != null) {
        SeqType st = expr3.seqType();
        // merge types if input is expected to have at least one entry
        if(!(expr1 instanceof XQMap && ((XQMap) expr1).mapSize() == 0)) {
          final MapType mt = (MapType) type1;
          type2 = mt.keyType().union(type2);
          st = mt.declType.union(expr3.seqType());
        }
        exprType.assign(MapType.get((AtomType) type2, st));
      }
    }
    return this;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) {
    // do not simplify type of key
  }
}
