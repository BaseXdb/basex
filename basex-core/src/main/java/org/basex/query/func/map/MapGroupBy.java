package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class MapGroupBy extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter input = exprs[0].iter(qc);
    final FItem key = toFunction(exprs[1], 1, qc);

    XQMap map = XQMap.empty();
    for(Item item; (item = qc.next(input)) != null;) {
      final Item k = key.invoke(qc, info, item).atomItem(qc, info);
      if(k != Empty.VALUE) {
        map = map.addAll(XQMap.entry(k, item, info), MergeDuplicates.COMBINE, qc, info);
      }
    }
    return map;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0], key = exprs[1];
    final SeqType st = input.seqType();
    if(st.zero()) return cc.merge(input, XQMap.empty(), info);

    AtomType ktype = null;
    final FuncType ft = key.funcType();
    if(ft != null) {
      ktype = ft.declType.type.atomic();
      if(ktype != null) exprs[1] = coerceFunc(exprs[1], cc, ktype.seqType(Occ.ZERO_OR_ONE),
          st.with(Occ.EXACTLY_ONE));
    }
    if(ktype == null) ktype = AtomType.ANY_ATOMIC_TYPE;;
    exprType.assign(MapType.get(ktype, st.with(Occ.ONE_OR_MORE)));
    return this;
  }
}
