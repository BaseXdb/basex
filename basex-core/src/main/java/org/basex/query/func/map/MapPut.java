package org.basex.query.func.map;

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
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class MapPut extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Map map = toMap(exprs[0], qc);
    final Item key = toAtomItem(exprs[1], qc);
    final Value val = qc.value(exprs[2]);
    return map.put(key, val, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type t = exprs[0].seqType().type;
    if(t instanceof MapType) {
      final MapType mt = (MapType) t;
      final SeqType kst = exprs[1].seqType(), vt = mt.declType.union(exprs[2].seqType());
      final Type kt = kst.type instanceof NodeType ? AtomType.ATM : kst.type;
      final Type key = kt instanceof AtomType ? mt.keyType().union(kt) : AtomType.AAT;
      exprType.assign(MapType.get((AtomType) key, vt));
    }
    return this;
  }
}
