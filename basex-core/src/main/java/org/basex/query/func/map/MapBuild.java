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
public final class MapBuild extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter input = exprs[0].iter(qc);
    final FItem key = defined(1) ? toFunction(exprs[1], 1, qc) : null;
    final FItem value = defined(2) ? toFunction(exprs[2], 1, qc) : null;
    final FItem duplicates = defined(3) ? toFunction(exprs[3], 2, qc) : null;

    XQMap map = XQMap.empty();
    for(Item item; (item = input.next()) != null;) {
      final Item k = (key != null ? key.invoke(qc, info, item) : item).atomItem(qc, info);
      if(!k.isEmpty()) {
        Value v = value != null ? value.invoke(qc, info, item) : item;
        if(map.contains(k, info)) {
          final Value o = map.get(k, info);
          v = duplicates != null ? duplicates.invoke(qc, info, o, v) :
            ValueBuilder.concat(o, v, qc);
        }
        map = map.put(k, v, info);
      }
    }
    return map;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0];
    final SeqType st = input.seqType();
    if(st.zero()) return cc.merge(input, XQMap.empty(), info);

    AtomType ktype = null;
    if(defined(1)) {
      final FuncType ft = exprs[1].funcType();
      if(ft != null) {
        ktype = ft.declType.type.atomic();
        if(ktype != null) exprs[1] = coerceFunc(exprs[1], cc, ktype.seqType(Occ.ZERO_OR_ONE),
            st.with(Occ.EXACTLY_ONE));
      }
    }
    if(ktype == null) ktype = AtomType.ANY_ATOMIC_TYPE;
    exprType.assign(MapType.get(ktype, st.with(Occ.ONE_OR_MORE)));
    return this;
  }
}
