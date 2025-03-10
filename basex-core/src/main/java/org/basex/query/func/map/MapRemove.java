package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class MapRemove extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    XQMap map = toMap(arg(0), qc);
    final Iter keys = arg(1).iter(qc);

    for(Item item; (item = qc.next(keys)) != null;) {
      map = map.remove(toAtomItem(item, qc));
    }
    return map;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0), key = arg(1);
    if(map == XQMap.empty()) return map;

    final Type type = map.seqType().type;
    Type tp = null;
    if(type instanceof RecordType) {
      // propagate record type
      final RecordType rt = (RecordType) type;
      if(key instanceof Item && key.seqType().type.isStringOrUntyped()) {
        final RecordField rf = rt.field(toToken(key, cc.qc));
        if(rf == null && !rt.isExtensible()) return map;
        if(rf == null || rf.isOptional()) tp = rt;
      }
    }
    if(tp == null && type instanceof MapType) {
      // create new map type (to drop potential record type assignment)
      final MapType mt = (MapType) type;
      tp = MapType.get(mt.keyType, mt.valueType);
    }
    if(tp != null) exprType.assign(tp);

    return this;
  }
}
