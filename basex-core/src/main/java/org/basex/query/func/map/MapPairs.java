package org.basex.query.func.map;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MapPairs extends MapEntries {
  @Override
  XQMap entry(final Item key, final Value value) throws QueryException {
    return XQMap.pair(key, value);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr map = arg(0);
    final Type type = map.seqType().type;
    if(type instanceof final MapType mt) {
      exprType.assign(cc.qc.shared.record(Str.KEY, mt.keyType().seqType(),
          Str.VALUE, mt.valueType()).seqType(Occ.ZERO_OR_MORE), map.structSize());
    }
    return this;
  }
}
