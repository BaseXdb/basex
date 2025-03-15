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
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class MapPut extends StandardFunc {
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQMap map = toMap(arg(0), qc);
    final Item key = toAtomItem(arg(1), qc);
    final Value value = arg(2).value(qc);

    return map.put(key, value);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr map = arg(0), key = arg(1), value = arg(2);
    if(map == XQMap.empty()) return cc.function(_MAP_ENTRY, info, key, value);

    final Type type = map.seqType().type;
    Type tp = null;
    if(type instanceof RecordType) {
      // propagate record type
      final RecordType rt = (RecordType) type;
      if(key instanceof Item) {
        final RecordField rf = key.seqType().type.isStringOrUntyped() ?
          rt.field(toToken(key, cc.qc)) : null;
        if(rf != null ? value.seqType().instanceOf(rf.seqType()) : rt.isExtensible()) tp = rt;
      }
    }
    if(tp == null && type instanceof MapType) {
      // create new map type (to drop potential record type assignment)
      final MapType mt = (MapType) type;
      final Type kt = key.seqType().type.atomic();
      if(kt != null) tp = mt.union(kt, value.seqType());
    }
    if(tp != null) exprType.assign(tp);

    return this;
  }
}
