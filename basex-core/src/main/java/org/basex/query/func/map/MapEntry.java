package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
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
public final class MapEntry extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Map.EMPTY.put(toAtomItem(exprs[0], qc), qc.value(exprs[1]), info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final SeqType kst = exprs[0].seqType(), st = exprs[1].seqType();
    final Type kt = kst.type instanceof NodeType ? AtomType.ATM : kst.type;
    if(kt instanceof AtomType) exprType.assign(MapType.get((AtomType) kt, st));
    return this;
  }
}
