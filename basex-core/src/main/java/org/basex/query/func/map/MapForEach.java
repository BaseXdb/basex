package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.update.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Leo Woerteler
 */
public class MapForEach extends StandardFunc {
  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(exprs[0], qc);
    final FItem action = toFunction(exprs[1], 2, this instanceof UpdateMapForEach, qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    map.apply((key, value) -> vb.add(action.invoke(qc, info, key, value)));
    return vb.value(this);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = exprs[0];
    if(map == XQMap.empty()) return Empty.VALUE;

    final Type type = map.seqType().type;
    if(type instanceof MapType) {
      final MapType mtype = (MapType) type;
      final SeqType declType = mtype.argTypes[0].with(Occ.EXACTLY_ONE);
      exprs[1] = coerceFunc(exprs[1], cc, SeqType.ITEM_ZM, declType, mtype.declType);
    }

    final FuncType ft = exprs[1].funcType();
    if(ft != null) exprType.assign(ft.declType.type);

    return this;
  }
}
