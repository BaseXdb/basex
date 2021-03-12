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
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public class MapForEach extends StandardFunc {
  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(exprs[0], qc);
    final FItem func = checkArity(exprs[1], 2, this instanceof UpdateMapForEach, qc);
    return map.forEach(func, qc, info);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0];
    if(expr1 == XQMap.EMPTY) return Empty.VALUE;

    final Type type1 = expr1.seqType().type;
    if(type1 instanceof MapType) {
      final MapType mtype1 = (MapType) type1;
      final SeqType declType1 = mtype1.argTypes[0].with(Occ.EXACTLY_ONE);
      exprs[1] = coerceFunc(exprs[1], cc, SeqType.ITEM_ZM, declType1, mtype1.declType);
    }

    final boolean updating = this instanceof UpdateMapForEach;
    final FuncType ft = exprs[1].funcType();
    if(ft != null && !updating) exprType.assign(ft.declType.type);

    return this;
  }
}
