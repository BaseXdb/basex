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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class MapPairs extends MapEntries {
  @Override
  XQMap entry(final Item key, final Value value) throws QueryException {
    return XQMap.singleton(Str.KEY, key).put(Str.VALUE, value);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type tp = arg(0).seqType().type;
    if(tp instanceof MapType) {
      final MapType mt = (MapType) tp;
      exprType.assign(MapType.get(AtomType.STRING, mt.valueType.union(mt.keyType.seqType())));
    }
    return this;
  }
}
